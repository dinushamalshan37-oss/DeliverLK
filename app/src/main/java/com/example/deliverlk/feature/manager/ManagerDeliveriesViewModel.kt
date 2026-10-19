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
    val onlyWithProof: Boolean = false,
    val deliveries: List<Delivery> = emptyList(),
    val isLoading: Boolean = false
)

class ManagerDeliveriesViewModel(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<DeliveryStatus?>(null)
    private val _onlyWithProof = MutableStateFlow(false)

    val uiState: StateFlow<ManagerDeliveriesUiState> = combine(
        deliveryRepository.deliveries,
        _searchQuery,
        _statusFilter,
        _onlyWithProof
    ) { list, query, filter, withProof ->
        val filtered = list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.trackingCode.contains(query, ignoreCase = true) ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.address.contains(query, ignoreCase = true)
            val matchesFilter = if (withProof) {
                item.status == DeliveryStatus.DELIVERED && !item.proofPhotoUri.isNullOrBlank()
            } else {
                filter == null || item.status == filter
            }
            matchesQuery && matchesFilter
        }
        ManagerDeliveriesUiState(
            searchQuery = query,
            statusFilter = filter,
            onlyWithProof = withProof,
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
        _onlyWithProof.value = false
        _statusFilter.value = status
    }

    fun toggleWithProofFilter() {
        _onlyWithProof.value = !_onlyWithProof.value
        if (_onlyWithProof.value) {
            _statusFilter.value = null
        }
    }
}