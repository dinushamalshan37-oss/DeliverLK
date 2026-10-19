package com.example.deliverlk.feature.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.model.DeliveryStatus
import com.example.deliverlk.domain.repository.ProofStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderDetailScreen(
    viewModel: CustomerOrderDetailViewModel,
    onNavigateToLiveTracking: (String) -> Unit,
    onNavigateBack: () -> Unit,
    proofStorage: ProofStorage? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
    val context = LocalContext.current
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
                title = delivery?.trackingCode ?: stringResource(R.string.delivery_detail_title),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading || delivery == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = delivery.trackingCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            DeliveryStatusChip(status = delivery.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (delivery.pickupAddress.isNotBlank()) {
                            InfoRow(label = stringResource(R.string.pickup_address), value = delivery.pickupAddress)
                        }
                        InfoRow(label = stringResource(R.string.dropoff_address), value = delivery.address)
                        InfoRow(label = stringResource(R.string.recipient_name), value = delivery.receiverName)
                        InfoRow(label = stringResource(R.string.phone_number), value = delivery.receiverPhone)
                        InfoRow(label = stringResource(R.string.items_description), value = delivery.itemsDescription)
                        if (delivery.packageWeightKg != null) {
                            InfoRow(label = "Package Weight", value = "${delivery.packageWeightKg} kg")
                        }
                        InfoRow(label = stringResource(R.string.priority), value = delivery.priority.name)
                        if (delivery.specialInstructions.isNotBlank()) {
                            InfoRow(label = stringResource(R.string.special_instructions), value = delivery.specialInstructions)
                        }
                    }
                }

                // Driver Card if assigned
                if (delivery.assignedDriverName != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Assigned Driver: ${delivery.assignedDriverName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Status: ${delivery.status.label()}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (delivery.customerPhone.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${delivery.customerPhone}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = stringResource(R.string.call_driver), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Live Tracking Button if picked up or out for delivery
                if (delivery.status in listOf(DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY)) {
                    PrimaryButton(
                        text = stringResource(R.string.live_tracking),
                        onClick = { onNavigateToLiveTracking(delivery.trackingCode) },
                        icon = Icons.Default.Navigation
                    )
                }

                // Timeline Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ct_tracking_timeline), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val steps = listOf(
                            DeliveryStatus.PENDING to stringResource(R.string.status_pending),
                            DeliveryStatus.ASSIGNED to stringResource(R.string.status_assigned),
                            DeliveryStatus.PICKED_UP to stringResource(R.string.status_picked_up),
                            DeliveryStatus.OUT_FOR_DELIVERY to stringResource(R.string.status_out_for_delivery),
                            DeliveryStatus.DELIVERED to stringResource(R.string.status_delivered)
                        )

                        steps.forEachIndexed { index, (status, label) ->
                            val isReached = delivery.status.ordinal >= status.ordinal
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isReached) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isReached) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text("${index + 1}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = label,
                                    fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 13.dp, top = 4.dp, bottom = 4.dp)
                                        .width(2.dp)
                                        .height(24.dp)
                                        .background(
                                            if (delivery.status.ordinal > status.ordinal) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                )
                            }
                        }
                    }
                }

                // Proof photo if delivered
                if (delivery.status == DeliveryStatus.DELIVERED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.proof_of_delivery), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            ProofPhoto(
                                deliveryId = delivery.id,
                                proofId = delivery.proofPhotoUri ?: "final",
                                proofStorage = proofStorage,
                                fallbackPhotoUri = delivery.proofPhotoUri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }

                // Cancel Order Button if PENDING
                if (delivery.status == DeliveryStatus.PENDING) {
                    OutlinedButton(
                        onClick = viewModel::openCancelDialog,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = stringResource(R.string.cancel_order), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }

        if (uiState.showCancelDialog) {
            AlertDialog(
                onDismissRequest = viewModel::closeCancelDialog,
                title = { Text(stringResource(R.string.cancel_order), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.confirm_cancel_order)) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.cancelOrder(onSuccess = onNavigateBack) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeCancelDialog) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}