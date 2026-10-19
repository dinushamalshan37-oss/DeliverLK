package com.example.deliverlk

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.deliverlk.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DeliverLkApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)

        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                val savedLanguage = appContainer.settingsRepository.languageTag.first()
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLanguage) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLanguage))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}