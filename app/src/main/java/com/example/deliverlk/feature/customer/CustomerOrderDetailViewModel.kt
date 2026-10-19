package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerOrderDetailUiState(
    val delivery: Delivery? = null,
    val isLoading: Boolean = true,
    val showCancelDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class CustomerOrderDetailViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerOrderDetailUiState())
    val uiState: StateFlow<CustomerOrderDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deliveryRepository.observeOrder(deliveryId).collect { delivery ->
                _uiState.update { it.copy(delivery = delivery, isLoading = false) }
            }
        }
    }

    fun openCancelDialog() = _uiState.update { it.copy(showCancelDialog = true) }
    fun closeCancelDialog() = _uiState.update { it.copy(showCancelDialog = false) }
    fun dismissSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun cancelOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = deliveryRepository.cancelMyOrder(deliveryId)
            closeCancelDialog()
            result.onSuccess {
                _uiState.update { it.copy(snackbarMessage = "Order cancelled successfully.") }
                onSuccess()
            }.onFailure { ex ->
                _uiState.update { it.copy(snackbarMessage = ex.localizedMessage ?: "Failed to cancel order.") }
            }
        }
    }
}