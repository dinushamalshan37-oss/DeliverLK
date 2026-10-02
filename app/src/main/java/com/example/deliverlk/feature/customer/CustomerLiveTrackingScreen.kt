package com.example.deliverlk.feature.customer

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.model.DeliveryStatus
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.driverLatLng, 13f)
    }

    if (delivery?.status == DeliveryStatus.DELIVERED) {
        onNavigateToDelivered(delivery.trackingCode)
    }

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
                // Live Map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = uiState.driverLatLng),
                            title = "Driver: ${delivery.assignedDriverName ?: "Courier"}"
                        )
                        Marker(
                            state = MarkerState(position = uiState.destinationLatLng),
                            title = "Your Location"
                        )
                    }
                }

                // Driver & Live ETA Details Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ETA COUNTDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${uiState.liveEtaMinutes} mins away", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Distance: ${uiState.liveDistanceKm} km", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }

                            DeliveryStatusChip(status = delivery.status)
                        }
                    }

                    // Driver info
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
                                Text(delivery.assignedDriverName ?: "Saman Kumara", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Vehicle: ${uiState.driverVehicle?.plateNumber ?: "WP CAB-1234"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            PrimaryButton(
                                text = "Call Driver",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+94771234567"))
                                    context.startActivity(intent)
                                },
                                icon = Icons.Default.Call,
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }

                    // Status timeline
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tracking Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            val steps = listOf(
                                DeliveryStatus.PICKED_UP to "Picked Up",
                                DeliveryStatus.OUT_FOR_DELIVERY to "Out for Delivery",
                                DeliveryStatus.DELIVERED to "Delivered"
                            )

                            steps.forEachIndexed { idx, (st, label) ->
                                val reached = delivery.status.ordinal >= st.ordinal
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (reached) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(label, fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal)
                                }
                                if (idx < steps.size - 1) {
                                    Box(modifier = Modifier.padding(start = 11.dp).width(2.dp).height(20.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
