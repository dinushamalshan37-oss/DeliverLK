package com.example.deliverlk.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.data.fake.FakeData
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isForgotPasswordOpen: Boolean = false,
    val resetMessage: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun fillDemoCredentials(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            password = "123456",
            error = null
        )
        login()
    }

    fun openForgotPassword() {
        _uiState.value = _uiState.value.copy(isForgotPasswordOpen = true, resetMessage = null)
    }

    fun closeForgotPassword() {
        _uiState.value = _uiState.value.copy(isForgotPasswordOpen = false)
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
            _uiState.value = _uiState.value.copy(
                isForgotPasswordOpen = false,
                resetMessage = "Password reset instructions sent to $email"
            )
        }
    }

    fun login(onSuccess: ((User) -> Unit)? = null) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email address")
            return
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { user ->
                onSuccess?.invoke(user)
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(error = ex.localizedMessage ?: "Login failed")
            }
        }
    }
}
