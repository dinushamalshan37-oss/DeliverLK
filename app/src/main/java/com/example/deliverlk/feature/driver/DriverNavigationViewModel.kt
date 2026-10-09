package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class DriverNavigationUiState(
    val delivery: Delivery? = null,
    val currentSpeedKmH: Int = 54,
    val isOverSpeedLimit: Boolean = false,
    val nextInstruction: String = "Turn left onto Galle Road in 450 m",
    val distanceRemainingKm: Double = 3.2,
    val etaMinutesRemaining: Int = 12,
    val routePolyline: List<LatLng> = emptyList(),
    val isLoading: Boolean = false
)

class DriverNavigationViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverNavigationUiState())
    val uiState: StateFlow<DriverNavigationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deliveryRepository.getDeliveryById(deliveryId).collect { del ->
                val polyline = if (del != null) listOf(
                    LatLng(6.9271, 79.8612),
                    LatLng(6.9180, 79.8580),
                    LatLng(6.9100, 79.8550),
                    LatLng(del.latitude, del.longitude)
                ) else emptyList()

                _uiState.update {
                    it.copy(
                        delivery = del,
                        routePolyline = polyline,
                        distanceRemainingKm = del?.distanceKm ?: 3.2,
                        etaMinutesRemaining = del?.etaMinutes ?: 12
                    )
                }
            }
        }

        // Simulate fluctuating live speed
        viewModelScope.launch {
            while (true) {
                delay(2000)
                val newSpeed = Random.nextInt(48, 68)
                _uiState.update {
                    it.copy(
                        currentSpeedKmH = newSpeed,
                        isOverSpeedLimit = newSpeed > 60
                    )
                }
            }
        }
    }
}
