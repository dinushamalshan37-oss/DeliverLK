package com.example.deliverlk.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deliverlk.R
import com.example.deliverlk.core.util.Formatters
import com.example.deliverlk.data.settings.ThemeMode
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryPriority
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.example.deliverlk.ui.theme.DeliverLKTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun DeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    proofStorage: com.example.deliverlk.domain.repository.ProofStorage? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = delivery.trackingCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (delivery.createdBy == "CUSTOMER") {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusChip(
                            statusName = stringResource(R.string.customer_order_badge),
                            backgroundColor = MaterialTheme.colorScheme.tertiary,
                            textColor = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
                DeliveryStatusChip(status = delivery.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = delivery.customerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = delivery.address,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (delivery.status == DeliveryStatus.DELIVERED && proofStorage != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    ProofPhoto(
                        deliveryId = delivery.id,
                        proofId = delivery.proofPhotoUri ?: "final",
                        proofStorage = proofStorage,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.eta_mins_fmt, delivery.etaMinutes),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = Formatters.formatLkr(delivery.payoutLkr),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.plateNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.driver_name_fmt, vehicle.driverName),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val updatedText = if (vehicle.lastUpdated.startsWith("Last seen") || vehicle.lastUpdated == "Just now") {
                    vehicle.lastUpdated
                } else {
                    stringResource(R.string.updated_time_fmt, vehicle.lastUpdated)
                }
                Text(
                    text = stringResource(R.string.vehicle_speed_updated_fmt, vehicle.speedKmH, updatedText),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            VehicleStatusChip(status = vehicle.status)
        }
    }
}

@Composable
fun MapPlaceholder(
    latitude: Double,
    longitude: Double,
    title: String,
    modifier: Modifier = Modifier,
    zoomLevel: Float = 13f
) {
    val pos = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pos, zoomLevel)
    }

    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = pos),
                title = title
            )
        }
    }
}

@Preview(showBackground = true, name = "Cards Light Mode")
@Composable
fun CardsLightPreview() {
    val sampleDelivery = Delivery(
        id = "del_01",
        trackingCode = "DLK-COL-101",
        customerName = "Nimali Fernando",
        customerPhone = "+94 76 555 1234",
        address = "No. 45, Galle Road, Colombo 03",
        latitude = 6.9271,
        longitude = 79.8612,
        itemsDescription = "Electronics Box",
        specialInstructions = "",
        priority = DeliveryPriority.HIGH,
        assignedDriverId = "drv_01",
        assignedDriverName = "Saman Kumara",
        status = DeliveryStatus.OUT_FOR_DELIVERY,
        etaMinutes = 15,
        distanceKm = 3.2,
        payoutLkr = 650.0
    )

    val sampleVehicle = Vehicle(
        id = "veh_01",
        plateNumber = "WP CAB-1234",
        driverId = "drv_01",
        driverName = "Saman Kumara",
        latitude = 6.9271,
        longitude = 79.8612,
        speedKmH = 45,
        deliveriesRemaining = 3,
        status = VehicleStatus.MOVING,
        lastUpdated = "Just now"
    )

    DeliverLKTheme(themeMode = ThemeMode.LIGHT) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                DeliveryCard(delivery = sampleDelivery, onClick = {})
                Spacer(modifier = Modifier.height(8.dp))
                VehicleCard(vehicle = sampleVehicle, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Cards Dark Mode")
@Composable
fun CardsDarkPreview() {
    val sampleDelivery = Delivery(
        id = "del_01",
        trackingCode = "DLK-COL-101",
        customerName = "Nimali Fernando",
        customerPhone = "+94 76 555 1234",
        address = "No. 45, Galle Road, Colombo 03",
        latitude = 6.9271,
        longitude = 79.8612,
        itemsDescription = "Electronics Box",
        specialInstructions = "",
        priority = DeliveryPriority.HIGH,
        assignedDriverId = "drv_01",
        assignedDriverName = "Saman Kumara",
        status = DeliveryStatus.OUT_FOR_DELIVERY,
        etaMinutes = 15,
        distanceKm = 3.2,
        payoutLkr = 650.0,
        createdBy = "CUSTOMER"
    )

    val sampleVehicle = Vehicle(
        id = "veh_01",
        plateNumber = "WP CAB-1234",
        driverId = "drv_01",
        driverName = "Saman Kumara",
        latitude = 6.9271,
        longitude = 79.8612,
        speedKmH = 45,
        deliveriesRemaining = 3,
        status = VehicleStatus.MOVING,
        lastUpdated = "Just now"
    )

    DeliverLKTheme(themeMode = ThemeMode.DARK) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                DeliveryCard(delivery = sampleDelivery, onClick = {})
                Spacer(modifier = Modifier.height(8.dp))
                VehicleCard(vehicle = sampleVehicle, onClick = {})
            }
        }
    }
}