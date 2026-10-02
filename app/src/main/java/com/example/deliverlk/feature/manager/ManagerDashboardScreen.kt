package com.example.deliverlk.feature.manager

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.example.deliverlk.core.designsystem.SecondaryButton
import com.example.deliverlk.core.designsystem.StatCard
import com.example.deliverlk.core.designsystem.VehicleStatusChip
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    viewModel: ManagerDashboardViewModel,
    onNavigateToCreateDelivery: () -> Unit,
    onNavigateToFleetList: () -> Unit,
    onNavigateToDriverDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colomboPos = LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(colomboPos, 12f)
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedVehicle) {
        showBottomSheet = uiState.selectedVehicle != null
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = "Fleet Dashboard",
                actions = {
                    TextButton(onClick = onNavigateToFleetList) {
                        Text(stringResource(R.string.view_all_vehicles), fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDelivery,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.assign_delivery))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                StatCard(
                    title = stringResource(R.string.active_vehicles),
                    value = uiState.activeVehiclesCount.toString(),
                    icon = Icons.Default.DirectionsCar
                )
                StatCard(
                    title = stringResource(R.string.deliveries_today),
                    value = uiState.deliveriesTodayCount.toString(),
                    icon = Icons.Default.LocalShipping
                )
                StatCard(
                    title = stringResource(R.string.completed),
                    value = uiState.completedCount.toString(),
                    icon = Icons.Default.CheckCircle
                )
                StatCard(
                    title = stringResource(R.string.delayed),
                    value = uiState.delayedCount.toString(),
                    icon = Icons.Default.Warning
                )
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterStatus == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text(stringResource(R.string.all)) }
                )
                FilterChip(
                    selected = uiState.filterStatus == VehicleStatus.MOVING,
                    onClick = { viewModel.setFilter(VehicleStatus.MOVING) },
                    label = { Text(stringResource(R.string.moving)) }
                )
                FilterChip(
                    selected = uiState.filterStatus == VehicleStatus.IDLE,
                    onClick = { viewModel.setFilter(VehicleStatus.IDLE) },
                    label = { Text(stringResource(R.string.idle)) }
                )
                FilterChip(
                    selected = uiState.filterStatus == VehicleStatus.OFFLINE,
                    onClick = { viewModel.setFilter(VehicleStatus.OFFLINE) },
                    label = { Text(stringResource(R.string.offline)) }
                )
            }

            // Live Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    uiState.vehicles.forEach { vehicle ->
                        Marker(
                            state = MarkerState(position = LatLng(vehicle.latitude, vehicle.longitude)),
                            title = vehicle.plateNumber,
                            snippet = "${vehicle.driverName} • ${vehicle.speedKmH} km/h",
                            onClick = {
                                viewModel.selectVehicle(vehicle)
                                true
                            }
                        )
                    }
                }
            }
        }

        // Vehicle Bottom Sheet
        if (showBottomSheet && uiState.selectedVehicle != null) {
            val selected = uiState.selectedVehicle!!
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.selectVehicle(null)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selected.plateNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Driver: ${selected.driverName}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        VehicleStatusChip(status = selected.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Speed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selected.speedKmH} km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Remaining", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selected.deliveriesRemaining} stops", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Last Updated", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selected.lastUpdated, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.view_driver),
                            onClick = {
                                showBottomSheet = false
                                viewModel.selectVehicle(null)
                                onNavigateToDriverDetail(selected.driverId)
                            },
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryButton(
                            text = stringResource(R.string.call_driver),
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+94771234567"))
                                context.startActivity(intent)
                            },
                            icon = Icons.Default.Call,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
