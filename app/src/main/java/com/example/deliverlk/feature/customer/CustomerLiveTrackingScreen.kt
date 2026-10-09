package com.example.deliverlk.feature.customer

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.model.DeliveryStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@Composable
fun CustomerLiveTrackingScreen(
    viewModel: CustomerLiveTrackingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDelivered: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
    val context = LocalContext.current

    // Follows the app's own Light/Dark setting (not only the phone's system setting)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.driverLatLng, 13f)
    }

    val mapProperties = remember(isDarkTheme) {
        MapProperties(
            mapStyleOptions = if (isDarkTheme) {
                runCatching {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                }.getOrNull()
            } else null
        )
    }

    // Keep the camera on the vehicle as it moves
    LaunchedEffect(uiState.driverLatLng) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(uiState.driverLatLng))
    }

    // Navigate once when the delivery is completed (never during composition)
    LaunchedEffect(delivery?.status) {
        val d = delivery
        if (d != null && d.status == DeliveryStatus.DELIVERED) {
            onNavigateToDelivered(d.trackingCode)
        }
    }

    val destinationTitle = stringResource(R.string.ct_destination)

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.live_tracking),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (delivery == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Live map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties
                    ) {
                        // Only draw the vehicle when real vehicle data exists
                        uiState.driverVehicle?.let { vehicle ->
                            AnimatedVehicleMarker(
                                vehicle = vehicle.copy(
                                    latitude = uiState.driverLatLng.latitude,
                                    longitude = uiState.driverLatLng.longitude
                                )
                            )
                        }

                        Marker(
                            state = MarkerState(position = uiState.destinationLatLng),
                            title = destinationTitle
                        )
                    }

                    if (uiState.driverVehicle == null) {
                        Text(
                            text = stringResource(R.string.ct_waiting_location),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Driver, ETA and timeline
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live ETA card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.ct_eta_countdown),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.ct_mins_away, uiState.liveEtaMinutes),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = stringResource(R.string.ct_distance_km, uiState.liveDistanceKm),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            DeliveryStatusChip(status = delivery.status)
                        }
                    }

                    // Driver info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = delivery.assignedDriverName
                                        ?: stringResource(R.string.ct_driver_unassigned),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                uiState.driverVehicle?.let { vehicle ->
                                    Text(
                                        text = stringResource(R.string.ct_vehicle, vehicle.plateNumber),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            PrimaryButton(
                                text = stringResource(R.string.ct_call_driver),
                                onClick = {
                                    // TODO(backend): replace with the driver's real phone number
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        "tel:+94771234567".toUri()
                                    )
                                    context.startActivity(intent)
                                },
                                icon = Icons.Default.Call,
                                modifier = Modifier.widthIn(min = 140.dp)
                            )
                        }
                    }

                    // Status timeline
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.ct_tracking_timeline),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val steps = listOf(
                                DeliveryStatus.PICKED_UP to stringResource(R.string.ct_step_picked_up),
                                DeliveryStatus.OUT_FOR_DELIVERY to stringResource(R.string.ct_step_out_for_delivery),
                                DeliveryStatus.DELIVERED to stringResource(R.string.ct_step_delivered)
                            )

                            steps.forEachIndexed { idx, (status, label) ->
                                val reached = delivery.status.ordinal >= status.ordinal
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (reached) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (reached) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (idx < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 11.dp)
                                            .width(2.dp)
                                            .height(20.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}