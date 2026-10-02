package com.example.deliverlk.feature.manager

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.DeliveryCard
import com.example.deliverlk.core.designsystem.EmptyState
import com.example.deliverlk.domain.model.DeliveryStatus

@Composable
fun ManagerDeliveriesScreen(
    viewModel: ManagerDeliveriesViewModel,
    onNavigateToCreateDelivery: () -> Unit,
    onNavigateToDeliveryDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DeliverLkTopBar(title = stringResource(R.string.deliveries))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDelivery,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_delivery))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Status Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.statusFilter == null,
                    onClick = { viewModel.onStatusFilterChanged(null) },
                    label = { Text(stringResource(R.string.all)) }
                )
                FilterChip(
                    selected = uiState.statusFilter == DeliveryStatus.PENDING,
                    onClick = { viewModel.onStatusFilterChanged(DeliveryStatus.PENDING) },
                    label = { Text(stringResource(R.string.status_pending)) }
                )
                FilterChip(
                    selected = uiState.statusFilter == DeliveryStatus.ASSIGNED,
                    onClick = { viewModel.onStatusFilterChanged(DeliveryStatus.ASSIGNED) },
                    label = { Text(stringResource(R.string.status_assigned)) }
                )
                FilterChip(
                    selected = uiState.statusFilter == DeliveryStatus.OUT_FOR_DELIVERY,
                    onClick = { viewModel.onStatusFilterChanged(DeliveryStatus.OUT_FOR_DELIVERY) },
                    label = { Text(stringResource(R.string.status_out_for_delivery)) }
                )
                FilterChip(
                    selected = uiState.statusFilter == DeliveryStatus.DELIVERED,
                    onClick = { viewModel.onStatusFilterChanged(DeliveryStatus.DELIVERED) },
                    label = { Text(stringResource(R.string.status_delivered)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.deliveries.isEmpty()) {
                EmptyState(message = stringResource(R.string.empty_data))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.deliveries, key = { it.id }) { delivery ->
                        DeliveryCard(
                            delivery = delivery,
                            onClick = { onNavigateToDeliveryDetail(delivery.id) }
                        )
                    }
                }
            }
        }
    }
}
