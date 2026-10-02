package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isDarkMode: Boolean = false,
    val showLogoutDialog: Boolean = false
)

class DriverProfileViewModel(
    private val appContainer: AppContainer,
    private val authRepository: AuthRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _showLogout = MutableStateFlow(false)
    private val _isDarkMode = MutableStateFlow(false)

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
        _isDarkMode,
        _showLogout
    ) { (user, vehicle, eventsAndScore), offlineMode, darkMode, showLogout ->
        val (events, score) = eventsAndScore
        DriverProfileUiState(
            user = user,
            vehicle = vehicle,
            safetyScore = score,
            safetyEvents = events,
            isOfflineMode = offlineMode,
            isDarkMode = darkMode,
            showLogoutDialog = showLogout
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverProfileUiState()
    )

    fun toggleOfflineDebug() {
        appContainer.toggleOfflineMode()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

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
