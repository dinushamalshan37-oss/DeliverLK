package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class OrderTab {
    ACTIVE, DELIVERED, CANCELLED, ALL
}

data class CustomerOrdersUiState(
    val orders: List<Delivery> = emptyList(),
    val selectedTab: OrderTab = OrderTab.ACTIVE,
    val trackingCodeInput: String = "",
    val searchError: String? = null,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

class CustomerOrdersViewModel(
    private val authRepository: AuthRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerOrdersUiState(isLoading = true))
    val uiState: StateFlow<CustomerOrdersUiState> = _uiState.asStateFlow()

    private var allOrders: List<Delivery> = emptyList()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.value
            val email = user?.email ?: "customer@deliverlk.lk"
            deliveryRepository.observeMyOrders(email).collect { list ->
                allOrders = list
                filterAndEmit()
            }
        }
    }

    fun onTabSelected(tab: OrderTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        filterAndEmit()
    }

    fun onTrackingInputChanged(input: String) {
        _uiState.update { it.copy(trackingCodeInput = input, searchError = null) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun trackOrderByCode(onFound: (String) -> Unit) {
        val code = _uiState.value.trackingCodeInput.trim()
        if (code.isBlank()) return
        val found = allOrders.find { it.trackingCode.equals(code, ignoreCase = true) || it.id.equals(code, ignoreCase = true) }
        if (found != null) {
            onFound(found.id)
        } else {
            _uiState.update { it.copy(searchError = "Order not found or access denied.") }
        }
    }

    private fun filterAndEmit() {
        val tab = _uiState.value.selectedTab
        val filtered = when (tab) {
            OrderTab.ACTIVE -> allOrders.filter { it.status in listOf(DeliveryStatus.PENDING, DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY) }
            OrderTab.DELIVERED -> allOrders.filter { it.status == DeliveryStatus.DELIVERED }
            OrderTab.CANCELLED -> allOrders.filter { it.status == DeliveryStatus.CANCELLED }
            OrderTab.ALL -> allOrders
        }
        _uiState.update { it.copy(orders = filtered, isLoading = false) }
    }
}