package com.example.deliverlk.feature.manager

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.deliverlk.core.designsystem.AnimatedVehicleMarker
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.EmptyState
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.example.deliverlk.core.designsystem.SecondaryButton
import com.example.deliverlk.core.designsystem.StatCard
import com.example.deliverlk.core.designsystem.VehicleStatusChip
import com.example.deliverlk.domain.model.VehicleStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
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
    val isDarkTheme = isSystemInDarkTheme()

    val colomboPos = LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(colomboPos, 12f)
    }

    val mapProperties = remember(isDarkTheme) {
        MapProperties(
            mapStyleOptions = if (isDarkTheme) {
                try {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                } catch (e: Exception) {
                    null
                }
            } else null
        )
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var hasCenteredMap by remember { mutableStateOf(false) }

    fun centerMapOnVehicles() {
        if (uiState.vehicles.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            uiState.vehicles.forEach { v ->
                builder.include(LatLng(v.latitude, v.longitude))
            }
            try {
                val bounds = builder.build()
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } catch (e: Exception) {
                val first = uiState.vehicles.first()
                cameraPositionState.move(CameraUpdateFactory.newLatLng(LatLng(first.latitude, first.longitude)))
            }
        }
    }

    LaunchedEffect(uiState.vehicles) {
        if (!hasCenteredMap && uiState.vehicles.isNotEmpty()) {
            centerMapOnVehicles()
            hasCenteredMap = true
        }
    }

    LaunchedEffect(uiState.selectedVehicle) {
        showBottomSheet = uiState.selectedVehicle != null
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.fleet_dashboard),
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
                if (uiState.vehicles.isEmpty()) {
                    EmptyState(
                        message = stringResource(R.string.no_vehicles_found),
                        icon = Icons.Default.DirectionsCar
                    )
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties
                    ) {
                        uiState.vehicles.forEach { vehicle ->
                            AnimatedVehicleMarker(
                                vehicle = vehicle,
                                onClick = {
                                    viewModel.selectVehicle(it)
                                    true
                                }
                            )
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { centerMapOnVehicles() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map")
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
                            Text(stringResource(R.string.speed_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selected.speedKmH} km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.remaining_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selected.deliveriesRemaining} stops", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.last_updated_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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