package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.model.VehicleStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*

data class ManagerDashboardUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val filterStatus: VehicleStatus? = null,
    val selectedVehicle: Vehicle? = null,
    val activeVehiclesCount: Int = 0,
    val deliveriesTodayCount: Int = 0,
    val completedCount: Int = 0,
    val delayedCount: Int = 0,
    val isLoading: Boolean = false
)

class ManagerDashboardViewModel(
    private val fleetRepository: FleetRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<VehicleStatus?>(null)
    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)

    val uiState: StateFlow<ManagerDashboardUiState> = combine(
        fleetRepository.vehicles,
        deliveryRepository.deliveries,
        _filterStatus,
        _selectedVehicle
    ) { vehicles, deliveries, filter, selected ->
        val filteredVehicles = if (filter == null) vehicles else vehicles.filter { it.status == filter }
        val activeCount = vehicles.count { it.status != VehicleStatus.OFFLINE }
        val todayCount = deliveries.size
        val completed = deliveries.count { it.status == DeliveryStatus.DELIVERED }
        val delayed = deliveries.count { it.status == DeliveryStatus.FAILED || it.priority.name == "URGENT" }

        ManagerDashboardUiState(
            vehicles = filteredVehicles,
            filterStatus = filter,
            selectedVehicle = selected,
            activeVehiclesCount = activeCount,
            deliveriesTodayCount = todayCount,
            completedCount = completed,
            delayedCount = delayed,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManagerDashboardUiState(isLoading = true)
    )

    fun setFilter(status: VehicleStatus?) {
        _filterStatus.value = status
    }

    fun selectVehicle(vehicle: Vehicle?) {
        _selectedVehicle.value = vehicle
    }
}
