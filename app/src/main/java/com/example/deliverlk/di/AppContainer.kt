package com.example.deliverlk.di

import com.example.deliverlk.data.fake.FakeAuthRepository
import com.example.deliverlk.data.fake.FakeDeliveryRepository
import com.example.deliverlk.data.fake.FakeFleetRepository
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppContainer {

    val authRepository: AuthRepository by lazy {
        FakeAuthRepository()
    }

    val fleetRepository: FleetRepository by lazy {
        FakeFleetRepository()
    }

    val deliveryRepository: DeliveryRepository by lazy {
        FakeDeliveryRepository()
    }

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }
}
