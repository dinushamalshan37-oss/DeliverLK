package com.example.deliverlk.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*

@Composable
fun CustomerOrdersScreen(
    viewModel: CustomerOrdersViewModel,
    onNavigateToNewOrder: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.my_orders),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewOrder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_order))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Track by code field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.trackingCodeInput,
                    onValueChange = viewModel::onTrackingInputChanged,
                    placeholder = { Text(stringResource(R.string.enter_tracking_code)) },
                    isError = uiState.searchError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.trackOrderByCode(onFound = onNavigateToOrderDetail) },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }

            if (uiState.searchError != null) {
                Text(
                    text = uiState.searchError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OrderTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            val title = when (tab) {
                                OrderTab.ACTIVE -> stringResource(R.string.active)
                                OrderTab.DELIVERED -> stringResource(R.string.status_delivered)
                                OrderTab.CANCELLED -> stringResource(R.string.status_cancelled)
                                OrderTab.ALL -> stringResource(R.string.all)
                            }
                            Text(title, fontWeight = FontWeight.Bold)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                LoadingState()
            } else if (uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyState(message = stringResource(R.string.empty_data))
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = stringResource(R.string.place_first_order),
                            onClick = onNavigateToNewOrder,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.orders, key = { it.id }) { delivery ->
                        DeliveryCard(
                            delivery = delivery,
                            onClick = { onNavigateToOrderDetail(delivery.id) }
                        )
                    }
                }
            }
        }
    }
}