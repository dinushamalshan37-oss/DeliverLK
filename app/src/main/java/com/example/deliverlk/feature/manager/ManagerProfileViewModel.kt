package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.data.settings.SettingsRepository
import com.example.deliverlk.data.settings.ThemeMode
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManagerProfileUiState(
    val user: User? = null,
    val notificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class ManagerProfileViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerProfileUiState())
    val uiState: StateFlow<ManagerProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { usr ->
                _uiState.value = _uiState.value.copy(user = usr)
            }
        }
        viewModelScope.launch {
            settingsRepository?.themeMode?.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }

    fun toggleNotifications() {
        _uiState.value = _uiState.value.copy(notificationsEnabled = !_uiState.value.notificationsEnabled)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository?.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun openLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = true)
    }

    fun closeLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = false)
    }

    fun openThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }

    fun closeThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    fun openEditProfileDialog() {
        _uiState.value = _uiState.value.copy(showEditProfileDialog = true)
    }

    fun closeEditProfileDialog() {
        _uiState.value = _uiState.value.copy(showEditProfileDialog = false)
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun updateProfile(name: String, phone: String, company: String) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(name, phone, company)
            closeEditProfileDialog()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (result.isSuccess) "Profile updated successfully!" else "Failed to update profile."
            )
        }
    }

    fun openLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun closeLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            closeLogoutDialog()
            onSuccess()
        }
    }
}
