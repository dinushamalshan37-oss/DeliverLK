package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.data.settings.SettingsRepository
import com.example.deliverlk.data.settings.ThemeMode
import com.example.deliverlk.di.AppContainer
import com.example.deliverlk.domain.model.SafetyEvent
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DriverProfileUiState(
    val user: User? = null,
    val vehicle: Vehicle? = null,
    val safetyScore: Int = 88,
    val safetyEvents: List<SafetyEvent> = emptyList(),
    val isOfflineMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class DriverProfileViewModel(
    private val appContainer: AppContainer,
    private val authRepository: AuthRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val settingsRepository: SettingsRepository = appContainer.settingsRepository

    private val _uiState = MutableStateFlow(DriverProfileUiState())
    val uiState: StateFlow<DriverProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
        viewModelScope.launch {
            fleetRepository.vehicles.collect { vehicles ->
                val v = vehicles.find { it.driverId == "drv_01" }
                _uiState.update { it.copy(vehicle = v) }
            }
        }
        viewModelScope.launch {
            fleetRepository.getDriverSafetyScore("drv_01").collect { score ->
                _uiState.update { it.copy(safetyScore = score) }
            }
        }
        viewModelScope.launch {
            fleetRepository.getSafetyEventsForDriver("drv_01").collect { events ->
                _uiState.update { it.copy(safetyEvents = events) }
            }
        }
        viewModelScope.launch {
            appContainer.isOfflineMode.collect { offline ->
                _uiState.update { it.copy(isOfflineMode = offline) }
            }
        }
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun toggleOfflineDebug() {
        appContainer.toggleOfflineMode()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun openEditProfileDialog() {
        _uiState.update { it.copy(showEditProfileDialog = true) }
    }

    fun closeEditProfileDialog() {
        _uiState.update { it.copy(showEditProfileDialog = false) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun updateProfile(name: String, phone: String, plateNumber: String) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(name, phone, plateNumber)
            closeEditProfileDialog()
            _uiState.update { it.copy(snackbarMessage = if (result.isSuccess) "Driver profile updated!" else "Failed to update profile.") }
        }
    }

    fun openLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun closeLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun openThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = true) }
    }

    fun closeThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    fun openLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun closeLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            closeLogoutDialog()
            onSuccess()
        }
    }
}
