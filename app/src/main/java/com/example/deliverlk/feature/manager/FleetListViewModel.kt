package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Vehicle
import com.example.deliverlk.domain.repository.FleetRepository
import kotlinx.coroutines.flow.*

data class FleetListUiState(
    val searchQuery: String = "",
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false
)

class FleetListViewModel(
    private val fleetRepository: FleetRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<FleetListUiState> = combine(
        fleetRepository.vehicles,
        _searchQuery
    ) { list, query ->
        val filtered = if (query.isBlank()) list else list.filter {
            it.plateNumber.contains(query, ignoreCase = true) ||
            it.driverName.contains(query, ignoreCase = true)
        }
        FleetListUiState(
            searchQuery = query,
            vehicles = filtered,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
