package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerProfileUiState(
    val user: User? = null,
    val showLogoutDialog: Boolean = false
)

class CustomerProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { usr ->
                _uiState.value = _uiState.value.copy(user = usr)
            }
        }
    }

    fun openLogoutDialog() { _uiState.value = _uiState.value.copy(showLogoutDialog = true) }
    fun closeLogoutDialog() { _uiState.value = _uiState.value.copy(showLogoutDialog = false) }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            closeLogoutDialog()
            onSuccess()
        }
    }
}
