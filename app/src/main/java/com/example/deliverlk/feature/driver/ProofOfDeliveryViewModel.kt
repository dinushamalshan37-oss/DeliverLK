package com.example.deliverlk.feature.driver

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.core.util.ImageCompressor
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.ProofStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProofOfDeliveryUiState(
    val delivery: Delivery? = null,
    val capturedPhotoUri: Uri? = null,
    val recipientName: String = "",
    val note: String = "",
    val showConfirmDialog: Boolean = false,
    val isSuccessAnimation: Boolean = false,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val isError: Boolean = false
)

class ProofOfDeliveryViewModel(
    private val deliveryId: String,
    private val deliveryRepository: DeliveryRepository,
    private val proofStorage: ProofStorage? = null
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

    fun onPhotoCaptured(uri: Uri?) {
        _uiState.update { it.copy(capturedPhotoUri = uri, isError = false) }
    }

    fun clearPhoto() {
        _uiState.update { it.copy(capturedPhotoUri = null) }
    }

    fun onRecipientNameChanged(valStr: String) = _uiState.update { it.copy(recipientName = valStr) }
    fun onNoteChanged(valStr: String) = _uiState.update { it.copy(note = valStr) }

    fun openConfirmDialog() = _uiState.update { it.copy(showConfirmDialog = true) }
    fun closeConfirmDialog() = _uiState.update { it.copy(showConfirmDialog = false) }
    fun dismissSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun markCompleteWithContext(context: Context, onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            closeConfirmDialog()
            _uiState.update { it.copy(isLoading = true, isError = false) }

            val photoUri = state.capturedPhotoUri
            var proofId: String? = null

            if (photoUri != null && proofStorage != null) {
                val imageBytes = ImageCompressor.compressImage(context, photoUri)
                if (imageBytes != null) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "driver_me"
                    val uploadRes = proofStorage.saveProofImage(deliveryId, "final", imageBytes, uid)
                    uploadRes.onSuccess { id ->
                        proofId = id
                    }.onFailure { ex ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isError = true,
                                snackbarMessage = ex.localizedMessage ?: "Failed to upload photo. Try again or complete without photo."
                            )
                        }
                        return@launch
                    }
                }
            }

            val completeRes = deliveryRepository.updateDeliveryStatus(
                id = deliveryId,
                status = DeliveryStatus.DELIVERED,
                proofPhotoUri = proofId ?: "final",
                recipientName = state.recipientName.ifBlank { "Self" },
                note = state.note
            )

            completeRes.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccessAnimation = true) }
                delay(1200)
                onSuccess()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        snackbarMessage = ex.localizedMessage ?: "Failed to complete delivery."
                    )
                }
            }
        }
    }

    fun markCompleteWithoutPhoto(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }
            val completeRes = deliveryRepository.updateDeliveryStatus(
                id = deliveryId,
                status = DeliveryStatus.DELIVERED,
                proofPhotoUri = null,
                recipientName = state.recipientName.ifBlank { "Self" },
                note = state.note
            )

            completeRes.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccessAnimation = true) }
                delay(1200)
                onSuccess()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        snackbarMessage = ex.localizedMessage ?: "Failed to complete delivery."
                    )
                }
            }
        }
    }
}