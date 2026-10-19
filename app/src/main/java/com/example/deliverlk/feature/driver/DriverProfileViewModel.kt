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
    val showThemeDialog: Boolean = false
)

class DriverProfileViewModel(
    private val appContainer: AppContainer,
    private val authRepository: AuthRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    val settingsRepository: SettingsRepository = appContainer.settingsRepository

    private val _showLogout = MutableStateFlow(false)
    private val _showLanguage = MutableStateFlow(false)
    private val _showTheme = MutableStateFlow(false)

    val uiState: StateFlow<DriverProfileUiState> = combine(
        combine(
            authRepository.currentUser,
            fleetRepository.vehicles,
            fleetRepository.getSafetyEventsForDriver("drv_01"),
            fleetRepository.getDriverSafetyScore("drv_01")
        ) { user, vehicles, events, score ->
            val v = vehicles.find { it.driverId == "drv_01" }
            Triple(user, v, events to score)
        },
        appContainer.isOfflineMode,
        settingsRepository.themeMode,
        combine(_showLogout, _showLanguage, _showTheme) { logout, lang, theme -> Triple(logout, lang, theme) }
    ) { (user, vehicle, eventsAndScore), offlineMode, themeMode, (logout, lang, theme) ->
        val (events, score) = eventsAndScore
        DriverProfileUiState(
            user = user,
            vehicle = vehicle,
            safetyScore = score,
            safetyEvents = events,
            isOfflineMode = offlineMode,
            themeMode = themeMode,
            showLogoutDialog = logout,
            showLanguageDialog = lang,
            showThemeDialog = theme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverProfileUiState()
    )

    fun toggleOfflineDebug() {
        appContainer.toggleOfflineMode()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun openLanguageDialog() { _showLanguage.value = true }
    fun closeLanguageDialog() { _showLanguage.value = false }

    fun openThemeDialog() { _showTheme.value = true }
    fun closeThemeDialog() { _showTheme.value = false }

    fun openLogoutDialog() { _showLogout.value = true }
    fun closeLogoutDialog() { _showLogout.value = false }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            closeLogoutDialog()
            onSuccess()
        }
    }
}
