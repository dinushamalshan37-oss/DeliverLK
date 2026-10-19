package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*

data class DriverDetailUiState(
    val driverId: String = "",
    val driverName: String = "Saman Kumara",
    val vehicle: Vehicle? = null,
    val deliveries: List<Delivery> = emptyList(),
    val safetyEvents: List<SafetyEvent> = emptyList(),
    val safetyScore: Int = 92,
    val completionRatePercentage: Int = 100,
    val isLoading: Boolean = false
)

class DriverDetailViewModel(
    private val driverId: String,
    private val fleetRepository: FleetRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    val uiState: StateFlow<DriverDetailUiState> = combine(
        fleetRepository.vehicles,
        deliveryRepository.getDeliveriesForDriver(driverId),
        fleetRepository.getSafetyEventsForDriver(driverId),
        fleetRepository.getDriverSafetyScore(driverId)
    ) { vehicles, deliveries, safetyEvents, safetyScore ->
        val vehicle = vehicles.find { it.driverId == driverId }
        val name = vehicle?.driverName ?: "Courier Driver"
        val totalCount = deliveries.size
        val completedCount = deliveries.count { it.status == DeliveryStatus.DELIVERED }
        val rate = if (totalCount > 0) (completedCount * 100) / totalCount else 100

        DriverDetailUiState(
            driverId = driverId,
            driverName = name,
            vehicle = vehicle,
            deliveries = deliveries,
            safetyEvents = safetyEvents,
            safetyScore = safetyScore,
            completionRatePercentage = rate,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverDetailUiState(isLoading = true)
    )
}
