package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProofOfDeliveryUiState(
    val delivery: Delivery? = null,
    val capturedPhotoUri: String? = null,
    val recipientName: String = "",
    val note: String = "",
    val showConfirmDialog: Boolean = false,
    val isSuccessAnimation: Boolean = false,
    val isLoading: Boolean = false
)

class ProofOfDeliveryViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProofOfDeliveryUiState())
    val uiState: StateFlow<ProofOfDeliveryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deliveryRepository.getDeliveryById(deliveryId).collect { del ->
                _uiState.update { it.copy(delivery = del) }
            }
        }
    }

    fun onCapturePhoto() {
        // Simulate camera capture
        val mockUri = "content://media/external/images/pod_${System.currentTimeMillis()}.jpg"
        _uiState.update { it.copy(capturedPhotoUri = mockUri) }
    }

    fun onUploadGallery() {
        val mockUri = "content://media/external/images/gallery_${System.currentTimeMillis()}.jpg"
        _uiState.update { it.copy(capturedPhotoUri = mockUri) }
    }

    fun onRecipientNameChanged(valStr: String) = _uiState.update { it.copy(recipientName = valStr) }
    fun onNoteChanged(valStr: String) = _uiState.update { it.copy(note = valStr) }

    fun openConfirmDialog() = _uiState.update { it.copy(showConfirmDialog = true) }
    fun closeConfirmDialog() = _uiState.update { it.copy(showConfirmDialog = false) }

    fun markComplete(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            closeConfirmDialog()
            _uiState.update { it.copy(isLoading = true) }

            deliveryRepository.updateDeliveryStatus(
                id = deliveryId,
                status = DeliveryStatus.DELIVERED,
                proofPhotoUri = state.capturedPhotoUri ?: "default_proof.jpg",
                recipientName = state.recipientName.ifBlank { "Self" },
                note = state.note
            )

            _uiState.update { it.copy(isLoading = false, isSuccessAnimation = true) }
            delay(1200) // Show checkmark animation before exiting
            onSuccess()
        }
    }
}
