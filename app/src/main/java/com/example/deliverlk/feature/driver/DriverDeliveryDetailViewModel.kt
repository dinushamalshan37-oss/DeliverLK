package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DriverDeliveryDetailUiState(
    val delivery: Delivery? = null,
    val isReportProblemOpen: Boolean = false,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

class DriverDeliveryDetailViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _isReportOpen = MutableStateFlow(false)
    private val _snackbarMsg = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DriverDeliveryDetailUiState> = combine(
        deliveryRepository.getDeliveryById(deliveryId),
        _isReportOpen,
        _snackbarMsg
    ) { delivery, isReportOpen, msg ->
        DriverDeliveryDetailUiState(
            delivery = delivery,
            isReportProblemOpen = isReportOpen,
            isLoading = false,
            snackbarMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverDeliveryDetailUiState(isLoading = true)
    )

    fun openReportProblem() { _isReportOpen.value = true }
    fun closeReportProblem() { _isReportOpen.value = false }
    fun dismissSnackbar() { _snackbarMsg.value = null }

    fun reportProblem(reason: String) {
        viewModelScope.launch {
            closeReportProblem()
            deliveryRepository.updateDeliveryStatus(
                id = deliveryId,
                status = DeliveryStatus.FAILED,
                note = "Reported problem: $reason"
            )
            _snackbarMsg.value = "Issue reported: $reason"
        }
    }
}
