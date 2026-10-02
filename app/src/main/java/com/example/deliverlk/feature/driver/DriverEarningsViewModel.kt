package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.Earning
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DriverEarningsUiState(
    val thisWeekTotalLkr: Double = 22850.0,
    val pendingPayoutLkr: Double = 6400.0,
    val dailyEarnings: List<Earning> = emptyList(),
    val completedDeliveries: List<Delivery> = emptyList(),
    val showRequestPayoutDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)

class DriverEarningsViewModel(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    private val _snackbarMsg = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DriverEarningsUiState> = combine(
        deliveryRepository.getDriverEarnings("drv_01"),
        deliveryRepository.getDeliveriesForDriver("drv_01"),
        _showDialog,
        _snackbarMsg
    ) { earnings, deliveries, showDialog, msg ->
        val total = earnings.sumOf { it.amountLkr }
        DriverEarningsUiState(
            thisWeekTotalLkr = total,
            pendingPayoutLkr = 6400.0,
            dailyEarnings = earnings,
            completedDeliveries = deliveries,
            showRequestPayoutDialog = showDialog,
            snackbarMessage = msg,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverEarningsUiState(isLoading = true)
    )

    fun openRequestPayoutDialog() { _showDialog.value = true }
    fun closeRequestPayoutDialog() { _showDialog.value = false }
    fun dismissSnackbar() { _snackbarMsg.value = null }

    fun requestPayout() {
        viewModelScope.launch {
            closeRequestPayoutDialog()
            _snackbarMsg.value = "Payout request submitted! Funds will transfer within 24 hours."
        }
    }
}
