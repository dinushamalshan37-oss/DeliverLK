package com.example.deliverlk.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.data.fake.FakeData
import com.example.deliverlk.di.AppContainer
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.AuthRepository
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*

data class DriverRouteUiState(
    val driverName: String = "Saman Kumara",
    val isOnlineDuty: Boolean = true,
    val isOfflineMode: Boolean = false,
    val deliveries: List<Delivery> = emptyList(),
    val totalStops: Int = 0,
    val completedStops: Int = 0,
    val progressFraction: Float = 0f,
    val isLoading: Boolean = false
)

class DriverRouteViewModel(
    private val appContainer: AppContainer,
    private val authRepository: AuthRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _isOnlineDuty = MutableStateFlow(true)

    val uiState: StateFlow<DriverRouteUiState> = combine(
        authRepository.currentUser,
        deliveryRepository.getDeliveriesForDriver("drv_01"),
        _isOnlineDuty,
        appContainer.isOfflineMode
    ) { user, list, online, offlineMode ->
        val driverName = user?.name ?: "Saman Kumara"
        val total = list.size
        val completed = list.count { it.status == DeliveryStatus.DELIVERED }
        val frac = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        DriverRouteUiState(
            driverName = driverName,
            isOnlineDuty = online,
            isOfflineMode = offlineMode,
            deliveries = list,
            totalStops = total,
            completedStops = completed,
            progressFraction = frac,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverRouteUiState(isLoading = true)
    )

    fun toggleOnlineDuty() {
        _isOnlineDuty.value = !_isOnlineDuty.value
    }
}
