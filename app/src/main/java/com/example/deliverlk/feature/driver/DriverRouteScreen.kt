package com.example.deliverlk.feature.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.model.DeliveryStatus

@Composable
fun DriverRouteScreen(
    viewModel: DriverRouteViewModel,
    onNavigateToDeliveryDetail: (String) -> Unit,
    onNavigateToNavigationScreen: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DeliverLkTopBar(title = stringResource(R.string.route))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OfflineBanner(visible = uiState.isOfflineMode)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting & Progress Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.greeting_ayubowan, uiState.driverName), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.todays_progress), fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.isOnlineDuty) stringResource(R.string.online) else stringResource(R.string.offline),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Switch(
                                        checked = uiState.isOnlineDuty,
                                        onCheckedChange = { viewModel.toggleOnlineDuty() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.stops_delivered_count, uiState.completedStops, uiState.totalStops),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { uiState.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }

                // Route Stops Section
                item {
                    Text(stringResource(R.string.delivery_stops_checklist), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                itemsIndexed(uiState.deliveries, key = { _, item -> item.id }) { index, delivery ->
                    val isNextStop = delivery.status != DeliveryStatus.DELIVERED &&
                            uiState.deliveries.take(index).all { it.status == DeliveryStatus.DELIVERED }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDeliveryDetail(delivery.id) }
                            .then(
                                if (isNextStop) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                else Modifier
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNextStop) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isNextStop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", fontWeight = FontWeight.Bold, color = if (isNextStop) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(delivery.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                DeliveryStatusChip(status = delivery.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(delivery.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Items: ${delivery.itemsDescription}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (delivery.status != DeliveryStatus.DELIVERED) {
                                Spacer(modifier = Modifier.height(12.dp))
                                PrimaryButton(
                                    text = if (isNextStop) stringResource(R.string.start_navigation) else stringResource(R.string.view_stop),
                                    onClick = {
                                        if (isNextStop) onNavigateToNavigationScreen(delivery.id)
                                        else onNavigateToDeliveryDetail(delivery.id)
                                    },
                                    icon = Icons.Default.Navigation
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}