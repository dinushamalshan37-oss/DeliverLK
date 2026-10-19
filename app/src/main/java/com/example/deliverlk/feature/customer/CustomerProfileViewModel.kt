package com.example.deliverlk.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.data.settings.SettingsRepository
import com.example.deliverlk.data.settings.ThemeMode
import com.example.deliverlk.domain.model.User
import com.example.deliverlk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerProfileUiState(
    val user: User? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showThemeDialog: Boolean = false
)

class CustomerProfileViewModel(
    private val authRepository: AuthRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _showLogout = MutableStateFlow(false)
    private val _showLanguage = MutableStateFlow(false)
    private val _showTheme = MutableStateFlow(false)

    val uiState: StateFlow<CustomerProfileUiState> = combine(
        authRepository.currentUser,
        settingsRepository.themeMode,
        combine(_showLogout, _showLanguage, _showTheme) { logout, lang, theme -> Triple(logout, lang, theme) }
    ) { user, themeMode, (logout, lang, theme) ->
        CustomerProfileUiState(
            user = user,
            themeMode = themeMode,
            showLogoutDialog = logout,
            showLanguageDialog = lang,
            showThemeDialog = theme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerProfileUiState()
    )

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