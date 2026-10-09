package com.example.deliverlk.di

import android.content.Context
import com.example.deliverlk.BuildConfig
import com.example.deliverlk.data.fake.FakeAuthRepository
import com.example.deliverlk.data.fake.FakeDeliveryRepository
import com.example.deliverlk.data.fake.FakeFleetRepository
import com.example.deliverlk.data.firebase.FirebaseAuthRepository
import com.example.deliverlk.data.firebase.FirestoreDeliveryRepository
import com.example.deliverlk.data.firebase.FirestoreFleetRepository
import com.example.deliverlk.data.settings.SettingsRepository
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppContainer(context: Context) {

    val useFirebase: Boolean = BuildConfig.USE_FIREBASE

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val authRepository: AuthRepository by lazy {
        if (useFirebase) {
            FirebaseAuthRepository()
        } else {
            FakeAuthRepository()
        }
    }

    val fleetRepository: FleetRepository by lazy {
        if (useFirebase) {
            FirestoreFleetRepository()
        } else {
            FakeFleetRepository()
        }
    }

    val deliveryRepository: DeliveryRepository by lazy {
        if (useFirebase) {
            FirestoreDeliveryRepository()
        } else {
            FakeDeliveryRepository()
        }
    }

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }
}
