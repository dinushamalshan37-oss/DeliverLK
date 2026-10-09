package com.example.deliverlk.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun CustomerTrackScreen(
    viewModel: CustomerTrackViewModel,
    onNavigateToLiveTracking: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.track_order),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = uiState.trackingCodeInput,
                onValueChange = viewModel::onTrackingInputChanged,
                label = stringResource(R.string.enter_tracking_code),
                placeholder = "e.g. DLK-COL-101",
                errorText = uiState.searchError
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = stringResource(R.string.track),
                onClick = { viewModel.trackOrder(onFound = onNavigateToLiveTracking) },
                icon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.recent_orders), fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.recentOrders, key = { it.id }) { delivery ->
                    DeliveryCard(
                        delivery = delivery,
                        onClick = { onNavigateToLiveTracking(delivery.trackingCode) }
                    )
                }
            }
        }
    }
}
