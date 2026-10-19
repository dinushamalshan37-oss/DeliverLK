package com.example.deliverlk.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingsRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language_tag")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val modeStr = prefs[themeKey] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(modeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val languageTag: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            prefs[languageKey] ?: "en"
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = tag
        }
    }
}