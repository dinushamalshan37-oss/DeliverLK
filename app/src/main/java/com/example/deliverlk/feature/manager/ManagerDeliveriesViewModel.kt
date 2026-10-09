package com.example.deliverlk.feature.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliverlk.domain.model.Delivery
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.*

data class ManagerDeliveriesUiState(
    val searchQuery: String = "",
    val statusFilter: DeliveryStatus? = null,
    val deliveries: List<Delivery> = emptyList(),
    val isLoading: Boolean = false
)

class ManagerDeliveriesViewModel(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<DeliveryStatus?>(null)

    val uiState: StateFlow<ManagerDeliveriesUiState> = combine(
        deliveryRepository.deliveries,
        _searchQuery,
        _statusFilter
    ) { list, query, filter ->
        val filtered = list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.trackingCode.contains(query, ignoreCase = true) ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.address.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || item.status == filter
            matchesQuery && matchesFilter
        }
        ManagerDeliveriesUiState(
            searchQuery = query,
            statusFilter = filter,
            deliveries = filtered,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManagerDeliveriesUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: DeliveryStatus?) {
        _statusFilter.value = status
    }
}
