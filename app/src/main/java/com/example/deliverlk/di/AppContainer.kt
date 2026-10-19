package com.example.deliverlk.di

import android.content.Context
import com.example.deliverlk.BuildConfig
import com.example.deliverlk.data.firebase.FirebaseAuthRepository
import com.example.deliverlk.data.firebase.FirebaseProofStorage
import com.example.deliverlk.data.firebase.FirestoreDeliveryRepository
import com.example.deliverlk.data.firebase.FirestoreFleetRepository
import com.example.deliverlk.data.firebase.FirestoreProofStorage
import com.example.deliverlk.data.local.SqliteAuthRepository
import com.example.deliverlk.data.local.SqliteDeliveryRepository
import com.example.deliverlk.data.local.SqliteFleetRepository
import com.example.deliverlk.data.local.SqliteProofStorage
import com.example.deliverlk.data.settings.SettingsRepository
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import com.example.deliverlk.domain.repository.ProofStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ProofBackend {
    FIRESTORE,
    STORAGE
}

class AppContainer(context: Context) {

    val useFirebase: Boolean = BuildConfig.USE_FIREBASE
    val proofBackend: ProofBackend = ProofBackend.FIRESTORE

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val proofStorage: ProofStorage by lazy {
        if (useFirebase) {
            try {
                when (proofBackend) {
                    ProofBackend.FIRESTORE -> FirestoreProofStorage(context)
                    ProofBackend.STORAGE -> FirebaseProofStorage(context)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                SqliteProofStorage()
            }
        } else {
            SqliteProofStorage()
        }
    }

    val authRepository: AuthRepository by lazy {
        if (useFirebase) {
            try {
                FirebaseAuthRepository()
            } catch (e: Throwable) {
                e.printStackTrace()
                SqliteAuthRepository(context)
            }
        } else {
            SqliteAuthRepository(context)
        }
    }

    val fleetRepository: FleetRepository by lazy {
        if (useFirebase) {
            try {
                FirestoreFleetRepository()
            } catch (e: Throwable) {
                e.printStackTrace()
                SqliteFleetRepository(context)
            }
        } else {
            SqliteFleetRepository(context)
        }
    }

    val deliveryRepository: DeliveryRepository by lazy {
        if (useFirebase) {
            try {
                FirestoreDeliveryRepository()
            } catch (e: Throwable) {
                e.printStackTrace()
                SqliteDeliveryRepository(context)
            }
        } else {
            SqliteDeliveryRepository(context)
        }
    }

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }
}
