package com.example.deliverlk.feature.driver

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.core.util.Formatters

@Composable
fun DriverEarningsScreen(
    viewModel: DriverEarningsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(title = stringResource(R.string.earnings))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Earnings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.this_week_earnings),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatLkr(uiState.thisWeekTotalLkr),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Daily Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Earnings Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val primaryColor = MaterialTheme.colorScheme.primary
                        val daily = uiState.dailyEarnings
                        val maxVal = (daily.maxOfOrNull { it.amountLkr } ?: 5000.0).toFloat()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            if (daily.isNotEmpty()) {
                                val barWidth = size.width / (daily.size * 2)
                                daily.forEachIndexed { index, earning ->
                                    val barHeight = ((earning.amountLkr / maxVal) * (size.height - 20.dp.toPx())).toFloat()
                                    val x = index * barWidth * 2 + barWidth / 2
                                    val y = size.height - barHeight

                                    drawRoundRect(
                                        color = primaryColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pending Payout & Request Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.pending_payout), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.formatLkr(uiState.pendingPayoutLkr), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(onClick = viewModel::openRequestPayoutDialog) {
                            Text(stringResource(R.string.request_payout))
                        }
                    }
                }
            }

            // Completed Deliveries List
            item {
                Text("Completed Payouts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(uiState.completedDeliveries, key = { it.id }) { delivery ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(delivery.trackingCode, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(delivery.customerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = Formatters.formatLkr(delivery.payoutLkr),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Request Payout Dialog
        if (uiState.showRequestPayoutDialog) {
            AlertDialog(
                onDismissRequest = viewModel::closeRequestPayoutDialog,
                title = { Text("Request Payout", fontWeight = FontWeight.Bold) },
                text = { Text("Request instant transfer of ${Formatters.formatLkr(uiState.pendingPayoutLkr)} to your linked Sri Lankan bank account?") },
                confirmButton = {
                    Button(onClick = viewModel::requestPayout) {
                        Text("Transfer Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeRequestPayoutDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
