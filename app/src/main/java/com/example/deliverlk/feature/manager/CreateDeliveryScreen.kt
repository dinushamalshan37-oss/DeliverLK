package com.example.deliverlk.feature.manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.LabeledTextField
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.example.deliverlk.core.designsystem.SecondaryButton
import com.example.deliverlk.domain.model.DeliveryPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeliveryScreen(
    viewModel: CreateDeliveryViewModel,
    onNavigateBack: () -> Unit
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
                title = stringResource(R.string.create_delivery),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Optimization Banner Button
            SecondaryButton(
                text = stringResource(R.string.suggest_optimal_order),
                onClick = viewModel::suggestOptimalOrder,
                icon = Icons.Default.AutoAwesome,
                enabled = !uiState.isOptimizing
            )

            if (uiState.isOptimizing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = uiState.customerName,
                onValueChange = viewModel::onCustomerNameChanged,
                label = stringResource(R.string.customer_name),
                placeholder = "e.g. Nimali Fernando",
                errorText = uiState.customerNameError
            )

            LabeledTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChanged,
                label = stringResource(R.string.phone_number),
                placeholder = "+94 76 555 1234",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                errorText = uiState.phoneError
            )

            LabeledTextField(
                value = uiState.customerEmail,
                onValueChange = viewModel::onCustomerEmailChanged,
                label = stringResource(R.string.email),
                placeholder = "customer@deliverlk.lk (optional)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorText = uiState.emailError
            )

            LabeledTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChanged,
                label = stringResource(R.string.delivery_address),
                placeholder = "No. 45, Galle Road, Colombo 03",
                errorText = uiState.addressError
            )

            LabeledTextField(
                value = uiState.itemsDescription,
                onValueChange = viewModel::onItemsDescChanged,
                label = stringResource(R.string.items_description),
                placeholder = "e.g. Electronics Box (2.5 kg)"
            )

            LabeledTextField(
                value = uiState.specialInstructions,
                onValueChange = viewModel::onSpecialInstructionsChanged,
                label = stringResource(R.string.special_instructions),
                placeholder = "Fragile. Ring doorbell twice.",
                singleLine = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Priority Dropdown / Selector
            Text(
                text = stringResource(R.string.priority),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeliveryPriority.entries.forEach { prio ->
                    FilterChip(
                        selected = uiState.priority == prio,
                        onClick = { viewModel.onPrioritySelected(prio) },
                        label = { Text(prio.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Driver Picker Field
            Text(
                text = stringResource(R.string.assign_driver_label),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.openDriverPicker() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.selectedDriver?.driverName ?: stringResource(R.string.select_driver_optional),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.create_delivery),
                onClick = { viewModel.createDelivery(onSuccess = onNavigateBack) },
                isLoading = uiState.isLoading
            )
        }

        // Driver Picker Bottom Sheet
        if (uiState.isDriverPickerOpen) {
            ModalBottomSheet(onDismissRequest = viewModel::closeDriverPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.select_driver_for_delivery),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(uiState.availableDrivers, key = { it.id }) { driver ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectDriver(driver) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(driver.driverName, fontWeight = FontWeight.Bold)
                                        Text("${driver.plateNumber} • ${driver.status.name}", fontSize = 12.sp)
                                    }
                                    Text(stringResource(R.string.stops_remaining, driver.deliveriesRemaining), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}