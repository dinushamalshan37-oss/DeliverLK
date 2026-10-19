package com.example.deliverlk.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChooseRoleUiState(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val selectedRole: Role = Role.MANAGER,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

class ChooseRoleViewModel(
    uid: String,
    name: String,
    email: String,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChooseRoleUiState(
            uid = uid,
            name = name,
            email = email
        )
    )
    val uiState: StateFlow<ChooseRoleUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            phoneError = null,
            generalError = null
        )
    }

    fun onRoleSelected(role: Role) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun completeSetup(onSuccess: (User) -> Unit) {
        val state = _uiState.value
        val cleanPhone = state.phone.trim()

        val phoneErr = validatePhone(cleanPhone)
        if (phoneErr != null) {
            _uiState.value = state.copy(phoneError = phoneErr)
            return
        }

        _uiState.value = state.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            val result = authRepository.completeGoogleRegistration(
                uid = state.uid,
                name = state.name,
                email = state.email,
                phone = cleanPhone,
                role = state.selectedRole
            )

            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = ex.localizedMessage ?: "Failed to complete setup."
                )
            }
        }
    }

    private fun validatePhone(phone: String): String? {
        val clean = phone.replace("\\s+".toRegex(), "")
        if (clean.isBlank()) return "Phone number is required."
        val isSriLankanFormat = clean.startsWith("+94") || clean.startsWith("0") || clean.startsWith("94")
        if (!isSriLankanFormat) return "Phone number must be a valid Sri Lankan number (+94)."
        val digitsOnly = clean.filter { it.isDigit() }
        if (digitsOnly.length < 9 || digitsOnly.length > 12) {
            return "Invalid phone number length."
        }
        return null
    }
}