package com.example.deliverlk.feature.customer

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.core.util.Formatters
import com.example.deliverlk.domain.model.Company
import com.example.deliverlk.domain.model.DeliveryPriority
import com.example.deliverlk.domain.model.NewOrderRequest
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class NewOrderUiState(
    val receiverName: String = "",
    val receiverPhone: String = "",
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val packageDescription: String = "",
    val packageWeightKgStr: String = "",
    val instructions: String = "",
    val priority: DeliveryPriority = DeliveryPriority.MEDIUM,
    val companies: List<Company> = emptyList(),
    val selectedCompany: Company? = null,
    val pickupLocation: GeoPoint? = null,
    val dropoffLocation: GeoPoint? = null,
    val isLoading: Boolean = false,
    val receiverNameError: String? = null,
    val phoneError: String? = null,
    val pickupError: String? = null,
    val dropoffError: String? = null,
    val snackbarMessage: String? = null
)

class NewOrderViewModel(
    private val authRepository: AuthRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewOrderUiState())
    val uiState: StateFlow<NewOrderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deliveryRepository.getCompanies().onSuccess { list ->
                _uiState.update { it.copy(companies = list, selectedCompany = list.firstOrNull()) }
            }
            val user = authRepository.currentUser.value
            if (user != null) {
                _uiState.update {
                    it.copy(
                        receiverName = user.name,
                        receiverPhone = user.phone
                    )
                }
            }
        }
    }

    fun onReceiverNameChanged(v: String) = _uiState.update { it.copy(receiverName = v, receiverNameError = null) }
    fun onReceiverPhoneChanged(v: String) = _uiState.update { it.copy(receiverPhone = v, phoneError = null) }
    fun onPickupAddressChanged(v: String) = _uiState.update { it.copy(pickupAddress = v, pickupError = null) }
    fun onDropoffAddressChanged(v: String) = _uiState.update { it.copy(dropoffAddress = v, dropoffError = null) }
    fun onPackageDescChanged(v: String) = _uiState.update { it.copy(packageDescription = v) }
    fun onWeightChanged(v: String) = _uiState.update { it.copy(packageWeightKgStr = v) }
    fun onInstructionsChanged(v: String) = _uiState.update { it.copy(instructions = v) }
    fun onPrioritySelected(p: DeliveryPriority) = _uiState.update { it.copy(priority = p) }
    fun onCompanySelected(c: Company) = _uiState.update { it.copy(selectedCompany = c) }

    fun setPickupLocation(loc: GeoPoint, address: String) = _uiState.update { it.copy(pickupLocation = loc, pickupAddress = address) }
    fun setDropoffLocation(loc: GeoPoint, address: String) = _uiState.update { it.copy(dropoffLocation = loc, dropoffAddress = address) }

    fun dismissSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private suspend fun geocodeAddress(context: Context, address: String): GeoPoint? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    val r = results[0]
                    GeoPoint(r.latitude, r.longitude)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun placeOrder(context: Context, onSuccess: (String) -> Unit) {
        val state = _uiState.value
        var hasErr = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var pickErr: String? = null
        var dropErr: String? = null

        if (state.receiverName.isBlank()) {
            nameErr = "Receiver name is required"
            hasErr = true
        }
        if (!Formatters.isValidPhone(state.receiverPhone)) {
            phoneErr = "Valid Sri Lankan phone number required (+94 7X XXX XXXX)"
            hasErr = true
        }
        if (state.pickupAddress.isBlank()) {
            pickErr = "Pickup address is required"
            hasErr = true
        }
        if (state.dropoffAddress.isBlank()) {
            dropErr = "Drop-off address is required"
            hasErr = true
        }

        if (hasErr) {
            _uiState.update {
                it.copy(
                    receiverNameError = nameErr,
                    phoneError = phoneErr,
                    pickupError = pickErr,
                    dropoffError = dropErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.currentUser.value
            val email = user?.email?.trim()?.lowercase() ?: "customer@deliverlk.lk"
            val uid = user?.id ?: "cust_01"

            val dropLoc = state.dropoffLocation ?: geocodeAddress(context, state.dropoffAddress) ?: GeoPoint(6.9271, 79.8612)
            val pickLoc = state.pickupLocation ?: geocodeAddress(context, state.pickupAddress)

            val weight = state.packageWeightKgStr.toDoubleOrNull()

            val request = NewOrderRequest(
                customerEmail = email,
                customerUid = uid,
                companyId = state.selectedCompany?.id ?: "dlk_company_01",
                pickupAddress = state.pickupAddress.trim(),
                pickupLocation = pickLoc,
                dropoffAddress = state.dropoffAddress.trim(),
                dropoffLocation = dropLoc,
                receiverName = state.receiverName.trim(),
                receiverPhone = Formatters.formatPhone(state.receiverPhone),
                packageDescription = state.packageDescription.ifBlank { "General Parcel" },
                packageWeightKg = weight,
                instructions = state.instructions.trim(),
                priority = state.priority
            )

            val result = deliveryRepository.placeOrder(request)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { delivery ->
                onSuccess(delivery.id)
            }.onFailure { ex ->
                _uiState.update { it.copy(snackbarMessage = ex.localizedMessage ?: "Failed to place order.") }
            }
        }
    }
}
