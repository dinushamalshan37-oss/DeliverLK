package com.example.deliverlk.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapPickerScreen(
    onLocationSelected: (LatLng) -> Unit,
    onNavigateBack: () -> Unit
) {
    val initialPos = LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 14f)
    }

    var selectedLatLng by remember { mutableStateOf(initialPos) }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.pick_on_map),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLatLng = latLng
                }
            ) {
                Marker(
                    state = MarkerState(position = selectedLatLng),
                    title = "Selected Location"
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        onLocationSelected(selectedLatLng)
                        onNavigateBack()
                    }
                )
            }
        }
    }
}