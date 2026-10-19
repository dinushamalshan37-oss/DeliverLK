package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*

data class CustomerDeliveredUiState(
    val delivery: Delivery? = null,
    val showIssueDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)

class CustomerDeliveredViewModel(
    private val trackingCode: String,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _showIssue = MutableStateFlow(false)
    private val _snackbarMsg = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CustomerDeliveredUiState> = combine(
        deliveryRepository.getDeliveryByTrackingCode(trackingCode),
        _showIssue,
        _snackbarMsg
    ) { delivery, showIssue, msg ->
        CustomerDeliveredUiState(
            delivery = delivery,
            showIssueDialog = showIssue,
            snackbarMessage = msg,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerDeliveredUiState(isLoading = true)
    )

    fun openIssueDialog() { _showIssue.value = true }
    fun closeIssueDialog() { _showIssue.value = false }
    fun dismissSnackbar() { _snackbarMsg.value = null }

    fun submitIssue(issueText: String) {
        closeIssueDialog()
        _snackbarMsg.value = "Issue report submitted. Support team will contact you shortly."
    }
}
