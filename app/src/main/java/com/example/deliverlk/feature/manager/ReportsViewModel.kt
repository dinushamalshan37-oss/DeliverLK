package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*

enum class ReportPeriod {
    TODAY,
    SEVEN_DAYS,
    THIRTY_DAYS
}

data class DriverRank(
    val name: String,
    val completedDeliveries: Int,
    val score: Int
)

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.SEVEN_DAYS,
    val totalDeliveries: Int = 0,
    val completionRatePercentage: Int = 0,
    val avgDeliveryTimeMins: Int = 28,
    val driverRanks: List<DriverRank> = emptyList(),
    val dailyTrendData: List<Pair<String, Int>> = emptyList(), // Day label to delivery count
    val isLoading: Boolean = false
)

class ReportsViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.SEVEN_DAYS)

    val uiState: StateFlow<ReportsUiState> = combine(
        deliveryRepository.deliveries,
        fleetRepository.vehicles,
        _selectedPeriod
    ) { deliveries, vehicles, period ->
        val total = deliveries.size
        val completed = deliveries.count { it.status == DeliveryStatus.DELIVERED }
        val rate = if (total > 0) (completed * 100) / total else 100

        val ranks = vehicles.mapIndexed { index, v ->
            DriverRank(
                name = v.driverName,
                completedDeliveries = (8 - index).coerceAtLeast(1),
                score = (98 - index * 3).coerceIn(70, 100)
            )
        }.sortedByDescending { it.completedDeliveries }

        val dailyTrend = listOf(
            "Mon" to 14,
            "Tue" to 18,
            "Wed" to 16,
            "Thu" to 22,
            "Fri" to 25,
            "Sat" to 28,
            "Sun" to 12
        )

        ReportsUiState(
            selectedPeriod = period,
            totalDeliveries = total,
            completionRatePercentage = rate,
            avgDeliveryTimeMins = 28,
            driverRanks = ranks,
            dailyTrendData = dailyTrend,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
    }
}
