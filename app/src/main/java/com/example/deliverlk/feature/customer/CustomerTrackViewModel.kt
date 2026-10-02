package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*

data class CustomerTrackUiState(
    val trackingCodeInput: String = "",
    val recentOrders: List<Delivery> = emptyList(),
    val searchError: String? = null,
    val isLoading: Boolean = false
)

class CustomerTrackViewModel(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _trackingInput = MutableStateFlow("")
    private val _searchError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CustomerTrackUiState> = combine(
        deliveryRepository.deliveries,
        _trackingInput,
        _searchError
    ) { deliveries, input, err ->
        CustomerTrackUiState(
            trackingCodeInput = input,
            recentOrders = deliveries,
            searchError = err,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerTrackUiState(isLoading = true)
    )

    fun onTrackingInputChanged(input: String) {
        _trackingInput.value = input
        _searchError.value = null
    }

    fun trackOrder(onFound: (String) -> Unit) {
        val code = _trackingInput.value.trim()
        if (code.isBlank()) {
            _searchError.value = "Please enter a tracking code"
            return
        }
        onFound(code)
    }
}
