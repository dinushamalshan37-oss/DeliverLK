package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ManagerDeliveryDetailUiState(
    val delivery: Delivery? = null,
    val availableDrivers: List<Vehicle> = emptyList(),
    val isReassignPickerOpen: Boolean = false,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

class ManagerDeliveryDetailViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _isReassignOpen = MutableStateFlow(false)
    private val _snackbarMsg = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ManagerDeliveryDetailUiState> = combine(
        deliveryRepository.getDeliveryById(deliveryId),
        fleetRepository.vehicles,
        _isReassignOpen,
        _snackbarMsg
    ) { delivery, drivers, isReassignOpen, msg ->
        ManagerDeliveryDetailUiState(
            delivery = delivery,
            availableDrivers = drivers,
            isReassignPickerOpen = isReassignOpen,
            isLoading = false,
            snackbarMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManagerDeliveryDetailUiState(isLoading = true)
    )

    fun openReassignPicker() { _isReassignOpen.value = true }
    fun closeReassignPicker() { _isReassignOpen.value = false }
    fun dismissSnackbar() { _snackbarMsg.value = null }

    fun reassignDriver(driver: Vehicle) {
        viewModelScope.launch {
            closeReassignPicker()
            deliveryRepository.assignDriver(deliveryId, driver.driverId, driver.driverName)
            _snackbarMsg.value = "Delivery reassigned to ${driver.driverName}"
        }
    }
}
