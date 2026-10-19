package com.example.deliverlk.feature.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ManagerDeliveryDetailScreen(
    viewModel: ManagerDeliveryDetailViewModel,
    onNavigateBack: () -> Unit,
    proofStorage: ProofStorage? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
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
        if (delivery == null) {
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
                // Customer & Order Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = delivery.trackingCode,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (delivery.createdBy == "CUSTOMER") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(
                                        statusName = stringResource(R.string.customer_order_badge),
                                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                                        textColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            DeliveryStatusChip(status = delivery.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(label = stringResource(R.string.customer_name), value = delivery.customerName)
                        InfoRow(label = stringResource(R.string.phone_number), value = delivery.customerPhone)
                        if (delivery.customerEmail.isNotBlank()) {
                            InfoRow(label = stringResource(R.string.email), value = delivery.customerEmail)
                        }
                        if (delivery.pickupAddress.isNotBlank()) {
                            InfoRow(label = "Pickup Address", value = delivery.pickupAddress)
                        }
                        InfoRow(label = stringResource(R.string.delivery_address), value = delivery.address)
                        if (delivery.receiverName.isNotBlank() && delivery.receiverName != delivery.customerName) {
                            InfoRow(label = "Receiver Name", value = delivery.receiverName)
                        }
                        if (delivery.receiverPhone.isNotBlank() && delivery.receiverPhone != delivery.customerPhone) {
                            InfoRow(label = "Receiver Phone", value = delivery.receiverPhone)
                        }
                        InfoRow(label = stringResource(R.string.items_description), value = delivery.itemsDescription)
                        InfoRow(label = stringResource(R.string.priority), value = delivery.priority.name)
                        if (delivery.specialInstructions.isNotBlank()) {
                            InfoRow(label = stringResource(R.string.special_instructions), value = delivery.specialInstructions)
                        }
                    }
                }

                // Driver Assignment Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.assigned_driver), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            TextButton(onClick = viewModel::openReassignPicker) {
                                Text(stringResource(R.string.reassign))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = delivery.assignedDriverName ?: stringResource(R.string.ct_driver_unassigned),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Vertical Stepper Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.delivery_status_timeline), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val steps = listOf(
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

                                Column {
                                    Text(
                                        text = label,
                                        fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
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

                // Proof of Delivery Photo Placeholder (when delivered)
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
                            if (!delivery.recipientName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.recipient_label, delivery.recipientName), fontSize = 13.sp)
                            }
                            if (!delivery.note.isNullOrBlank()) {
                                Text("Note: ${delivery.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Reassign Driver Picker
        if (uiState.isReassignPickerOpen) {
            ModalBottomSheet(onDismissRequest = viewModel::closeReassignPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.reassign_driver_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        items(uiState.availableDrivers, key = { it.id }) { driver ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.reassignDriver(driver) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(driver.driverName, fontWeight = FontWeight.Bold)
                                    Text("${driver.plateNumber} (${driver.status.name})", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}