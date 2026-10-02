package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerLiveTrackingUiState(
    val delivery: Delivery? = null,
    val driverVehicle: Vehicle? = null,
    val liveEtaMinutes: Int = 15,
    val liveDistanceKm: Double = 3.2,
    val driverLatLng: LatLng = LatLng(6.9271, 79.8612),
    val destinationLatLng: LatLng = LatLng(6.9080, 79.8530),
    val isLoading: Boolean = false
)

class CustomerLiveTrackingViewModel(
    private val trackingCode: String,
    private val deliveryRepository: DeliveryRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _liveEta = MutableStateFlow(15)
    private val _liveDist = MutableStateFlow(3.2)

    val uiState: StateFlow<CustomerLiveTrackingUiState> = combine(
        deliveryRepository.getDeliveryByTrackingCode(trackingCode),
        fleetRepository.vehicles,
        _liveEta,
        _liveDist
    ) { delivery, vehicles, eta, dist ->
        val v = vehicles.find { it.driverId == delivery?.assignedDriverId }
        val driverPos = if (v != null) LatLng(v.latitude, v.longitude) else LatLng(6.9271, 79.8612)
        val destPos = if (delivery != null) LatLng(delivery.latitude, delivery.longitude) else LatLng(6.9080, 79.8530)

        CustomerLiveTrackingUiState(
            delivery = delivery,
            driverVehicle = v,
            liveEtaMinutes = eta,
            liveDistanceKm = dist,
            driverLatLng = driverPos,
            destinationLatLng = destPos,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerLiveTrackingUiState(isLoading = true)
    )

    init {
        // Countdown ETA and distance timer to simulate live approaching driver
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _liveEta.update { (it - 1).coerceAtLeast(1) }
                _liveDist.update { (it - 0.2).coerceAtLeast(0.1) }
            }
        }
    }
}
