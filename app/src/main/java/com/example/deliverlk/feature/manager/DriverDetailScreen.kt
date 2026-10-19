package com.example.deliverlk.feature.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import com.example.deliverlk.core.designsystem.AvatarInitials
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.DeliveryCard
import com.example.deliverlk.core.designsystem.InfoRow

@Composable
fun DriverDetailScreen(
    viewModel: DriverDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDeliveryDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.driver_details),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarInitials(name = uiState.driverName, size = 64.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.driverName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.vehicle != null) {
                            Text(
                                text = "Vehicle: ${uiState.vehicle?.plateNumber} (${uiState.vehicle?.status})",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Performance summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(stringResource(R.string.safety_score), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.safetyScore}/100", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(stringResource(R.string.completion_pct), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.completionRatePercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Text(stringResource(R.string.speed_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.vehicle?.speedKmH ?: 0} km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Safety Record List
            item {
                Text(
                    text = stringResource(R.string.safety_record),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.safetyEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = stringResource(R.string.clean_safety_record),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(uiState.safetyEvents, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.type.name.replace("_", " "),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = event.timestamp,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.locationText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Today's Deliveries Section
            item {
                Text(
                    text = stringResource(R.string.todays_deliveries),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.deliveries, key = { it.id }) { delivery ->
                DeliveryCard(
                    delivery = delivery,
                    onClick = { onNavigateToDeliveryDetail(delivery.id) }
                )
            }
        }
    }
}