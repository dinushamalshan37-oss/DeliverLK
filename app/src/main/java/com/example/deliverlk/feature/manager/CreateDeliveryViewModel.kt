package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.core.util.Formatters
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryPriority
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateDeliveryUiState(
    val customerName: String = "",
    val phone: String = "",
    val customerEmail: String = "",
    val address: String = "",
    val itemsDescription: String = "",
    val specialInstructions: String = "",
    val priority: DeliveryPriority = DeliveryPriority.MEDIUM,
    val selectedDriver: Vehicle? = null,
    val availableDrivers: List<Vehicle> = emptyList(),
    val isDriverPickerOpen: Boolean = false,
    val isOptimizing: Boolean = false,
    val isLoading: Boolean = false,
    val customerNameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val addressError: String? = null,
    val snackbarMessage: String? = null
)

class CreateDeliveryViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateDeliveryUiState())
    val uiState: StateFlow<CreateDeliveryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fleetRepository.vehicles.collect { vehicles ->
                _uiState.update { it.copy(availableDrivers = vehicles) }
            }
        }
    }

    fun onCustomerNameChanged(valStr: String) = _uiState.update { it.copy(customerName = valStr, customerNameError = null) }
    fun onPhoneChanged(valStr: String) = _uiState.update { it.copy(phone = valStr, phoneError = null) }
    fun onCustomerEmailChanged(valStr: String) = _uiState.update { it.copy(customerEmail = valStr, emailError = null) }
    fun onAddressChanged(valStr: String) = _uiState.update { it.copy(address = valStr, addressError = null) }
    fun onItemsDescChanged(valStr: String) = _uiState.update { it.copy(itemsDescription = valStr) }
    fun onSpecialInstructionsChanged(valStr: String) = _uiState.update { it.copy(specialInstructions = valStr) }
    fun onPrioritySelected(priority: DeliveryPriority) = _uiState.update { it.copy(priority = priority) }

    fun openDriverPicker() = _uiState.update { it.copy(isDriverPickerOpen = true) }
    fun closeDriverPicker() = _uiState.update { it.copy(isDriverPickerOpen = false) }
    fun selectDriver(driver: Vehicle) = _uiState.update { it.copy(selectedDriver = driver, isDriverPickerOpen = false) }

    fun dismissSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun suggestOptimalOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true) }
            deliveryRepository.suggestOptimalOrder()
            _uiState.update {
                it.copy(
                    isOptimizing = false,
                    snackbarMessage = "Route stops reordered for optimal efficiency!"
                )
            }
        }
    }

    fun createDelivery(onSuccess: () -> Unit) {
        val state = _uiState.value
        var hasErr = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var emailErr: String? = null
        var addrErr: String? = null

        if (state.customerName.isBlank()) {
            nameErr = "Customer name is required"
            hasErr = true
        }

        if (!Formatters.isValidPhone(state.phone)) {
            phoneErr = "Valid Sri Lankan phone number required (+94 7X XXX XXXX)"
            hasErr = true
        }

        val cleanEmail = state.customerEmail.trim().lowercase()
        if (cleanEmail.isNotBlank() && (!cleanEmail.contains("@") || !cleanEmail.contains("."))) {
            emailErr = "Invalid email address"
            hasErr = true
        }

        if (state.address.isBlank()) {
            addrErr = "Delivery address is required"
            hasErr = true
        }

        if (hasErr) {
            _uiState.update {
                it.copy(
                    customerNameError = nameErr,
                    phoneError = phoneErr,
                    emailError = emailErr,
                    addressError = addrErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val trackingNum = "DLK-COL-${(100..999).random()}"
            val newDelivery = Delivery(
                id = "del_${System.currentTimeMillis()}",
                trackingCode = trackingNum,
                customerName = state.customerName.trim(),
                customerPhone = Formatters.formatPhone(state.phone),
                address = state.address.trim(),
                latitude = 6.9271,
                longitude = 79.8612,
                itemsDescription = state.itemsDescription.ifBlank { "General Parcel" },
                specialInstructions = state.specialInstructions,
                priority = state.priority,
                assignedDriverId = state.selectedDriver?.driverId,
                assignedDriverName = state.selectedDriver?.driverName,
                status = if (state.selectedDriver != null) DeliveryStatus.ASSIGNED else DeliveryStatus.PENDING,
                payoutLkr = (400..900).random().toDouble(),
                customerEmail = cleanEmail,
                createdBy = "MANAGER",
                receiverName = state.customerName.trim(),
                receiverPhone = Formatters.formatPhone(state.phone),
                pickupAddress = state.address.trim()
            )

            deliveryRepository.createDelivery(newDelivery)
            _uiState.update { it.copy(isLoading = false, snackbarMessage = "Delivery $trackingNum created successfully!") }
            onSuccess()
        }
    }
}