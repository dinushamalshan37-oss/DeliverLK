package com.example.deliverlk.feature.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDeliveryDetailScreen(
    viewModel: DriverDeliveryDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNavigation: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = delivery?.trackingCode ?: stringResource(R.string.delivery_info_title),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (delivery == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer & Phone Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(delivery.customerName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(delivery.customerPhone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${delivery.customerPhone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = stringResource(R.string.call_customer), tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(label = stringResource(R.string.delivery_address_label), value = delivery.address)
                        InfoRow(label = stringResource(R.string.item_details_label), value = delivery.itemsDescription)
                    }
                }

                // Special Instructions Card
                if (delivery.specialInstructions.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.special_instructions_label), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(delivery.specialInstructions, fontSize = 14.sp)
                        }
                    }
                }

                // Map Preview
                Text(stringResource(R.string.map_preview), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                MapPlaceholder(
                    latitude = delivery.latitude,
                    longitude = delivery.longitude,
                    title = delivery.customerName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                PrimaryButton(
                    text = stringResource(R.string.start_navigation),
                    onClick = { onNavigateToNavigation(delivery.id) },
                    icon = Icons.Default.Navigation
                )

                SecondaryButton(
                    text = stringResource(R.string.report_problem),
                    onClick = viewModel::openReportProblem,
                    icon = Icons.Default.ReportProblem
                )
            }
        }

        // Report Problem Bottom Sheet
        if (uiState.isReportProblemOpen) {
            ModalBottomSheet(onDismissRequest = viewModel::closeReportProblem) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.report_problem_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    val reasons = listOf(
                        stringResource(R.string.reason_customer_unavailable),
                        stringResource(R.string.reason_wrong_address),
                        stringResource(R.string.reason_customer_refused),
                        stringResource(R.string.reason_damaged_package),
                        stringResource(R.string.reason_other)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 260.dp)
                    ) {
                        items(reasons) { reason ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.reportProblem(reason) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(reason, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}