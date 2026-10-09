package com.example.deliverlk.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.example.deliverlk.core.util.VehicleIconFactory
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AnimatedVehicleMarker(
    vehicle: Vehicle,
    isStale: Boolean = false,
    onClick: (Vehicle) -> Boolean = { false }
) {
    val context = LocalContext.current
    val effectiveStatus = if (isStale) VehicleStatus.OFFLINE else vehicle.status
    val icon = remember(effectiveStatus) {
        VehicleIconFactory.getVehicleIcon(context, effectiveStatus)
    }

    val targetLat = vehicle.latitude
    val targetLng = vehicle.longitude
    val targetHeading = vehicle.heading.toFloat()

    val animLat = remember { Animatable(targetLat.toFloat()) }
    val animLng = remember { Animatable(targetLng.toFloat()) }
    val animHeading = remember { Animatable(targetHeading) }

    LaunchedEffect(targetLat, targetLng) {
        val startLat = animLat.value.toDouble()
        val startLng = animLng.value.toDouble()

        val dist = calculateDistanceMeters(startLat, startLng, targetLat, targetLng)
        if (dist > 500.0) {
            animLat.snapTo(targetLat.toFloat())
            animLng.snapTo(targetLng.toFloat())
        } else {
            animLat.animateTo(targetLat.toFloat(), tween(1000))
            animLng.animateTo(targetLng.toFloat(), tween(1000))
        }
    }

    LaunchedEffect(targetHeading) {
        val currentHeading = animHeading.value
        var diff = (targetHeading - currentHeading) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f

        animHeading.animateTo(currentHeading + diff, tween(1000))
    }

    val currentLatLng = LatLng(animLat.value.toDouble(), animLng.value.toDouble())
    val snippetText = if (isStale) {
        "Last seen ${vehicle.lastUpdated}"
    } else {
        "${vehicle.driverName} • ${vehicle.speedKmH} km/h"
    }

    Marker(
        state = MarkerState(position = currentLatLng),
        title = vehicle.plateNumber,
        snippet = snippetText,
        icon = icon,
        rotation = animHeading.value,
        flat = true,
        anchor = Offset(0.5f, 0.5f),
        onClick = { onClick(vehicle) }
    )
}

private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radiusEarthMeters = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return radiusEarthMeters * c
}
