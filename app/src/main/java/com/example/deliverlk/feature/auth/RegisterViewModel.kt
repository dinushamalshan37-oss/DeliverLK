package com.example.deliverlk.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.core.util.Formatters
import com.example.deliverlk.domain.model.GoogleSignInResult
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: Role = Role.MANAGER,
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onPhoneChanged(value: String) {
        _uiState.value = _uiState.value.copy(phone = value, phoneError = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun onRoleSelected(role: Role) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun signInWithGoogle(context: Context, onResult: (GoogleSignInResult) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)
            val res = authRepository.signInWithGoogle(context)
            _uiState.value = _uiState.value.copy(isLoading = false)
            res.onSuccess { googleResult ->
                when (googleResult) {
                    is GoogleSignInResult.Cancelled -> {
                        // User cancelled / closed sheet: no error
                    }
                    else -> onResult(googleResult)
                }
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    generalError = ex.localizedMessage ?: "Google sign-in failed. Please try again."
                )
            }
        }
    }

    fun register(onSuccess: ((User) -> Unit)? = null) {
        val state = _uiState.value
        var hasError = false

        var nameErr: String? = null
        var phoneErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null

        if (state.name.isBlank()) {
            nameErr = "Full name is required"
            hasError = true
        }

        if (!Formatters.isValidPhone(state.phone)) {
            phoneErr = "Invalid Sri Lankan phone number (+94 7X XXX XXXX)"
            hasError = true
        }

        if (state.email.isBlank() || !state.email.contains("@")) {
            emailErr = "Invalid email address"
            hasError = true
        }

        if (state.password.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }

        if (state.password != state.confirmPassword) {
            confirmErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _uiState.value = _uiState.value.copy(
                nameError = nameErr,
                phoneError = phoneErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)
            val formattedPhone = Formatters.formatPhone(state.phone)
            val result = authRepository.register(
                name = state.name.trim(),
                phone = formattedPhone,
                email = state.email.trim(),
                password = state.password.trim(),
                role = state.selectedRole
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { user ->
                onSuccess?.invoke(user)
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(generalError = ex.localizedMessage ?: "Registration failed")
            }
        }
    }
}