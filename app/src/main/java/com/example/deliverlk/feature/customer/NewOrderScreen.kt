package com.example.deliverlk.feature.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.model.DeliveryPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    viewModel: NewOrderViewModel,
    onNavigateToMapPicker: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                title = stringResource(R.string.new_order),
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabeledTextField(
                value = uiState.receiverName,
                onValueChange = viewModel::onReceiverNameChanged,
                label = stringResource(R.string.recipient_name),
                placeholder = "e.g. Nimali Fernando",
                errorText = uiState.receiverNameError
            )

            LabeledTextField(
                value = uiState.receiverPhone,
                onValueChange = viewModel::onReceiverPhoneChanged,
                label = stringResource(R.string.phone_number),
                placeholder = "+94 76 555 1234",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                errorText = uiState.phoneError
            )

            // Pickup Address + Map button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabeledTextField(
                    value = uiState.pickupAddress,
                    onValueChange = viewModel::onPickupAddressChanged,
                    label = stringResource(R.string.pickup_address),
                    placeholder = "No. 12, Main Street, Colombo 01",
                    errorText = uiState.pickupError,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onNavigateToMapPicker(true) },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = stringResource(R.string.pick_on_map), tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Dropoff Address + Map button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabeledTextField(
                    value = uiState.dropoffAddress,
                    onValueChange = viewModel::onDropoffAddressChanged,
                    label = stringResource(R.string.dropoff_address),
                    placeholder = "No. 45, Galle Road, Colombo 03",
                    errorText = uiState.dropoffError,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onNavigateToMapPicker(false) },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = stringResource(R.string.pick_on_map), tint = MaterialTheme.colorScheme.primary)
                }
            }

            LabeledTextField(
                value = uiState.packageDescription,
                onValueChange = viewModel::onPackageDescChanged,
                label = stringResource(R.string.items_description),
                placeholder = "e.g. Electronics Box (2.5 kg)"
            )

            LabeledTextField(
                value = uiState.packageWeightKgStr,
                onValueChange = viewModel::onWeightChanged,
                label = stringResource(R.string.package_weight_kg),
                placeholder = "e.g. 1.5",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            LabeledTextField(
                value = uiState.instructions,
                onValueChange = viewModel::onInstructionsChanged,
                label = stringResource(R.string.special_instructions),
                placeholder = "Fragile. Ring doorbell twice.",
                singleLine = false
            )

            // Priority
            Text(
                text = stringResource(R.string.priority),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeliveryPriority.entries.forEach { prio ->
                    FilterChip(
                        selected = uiState.priority == prio,
                        onClick = { viewModel.onPrioritySelected(prio) },
                        label = { Text(prio.label()) }
                    )
                }
            }

            // Company Picker if multiple
            if (uiState.companies.size > 1) {
                Text(
                    text = "Company",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.companies.forEach { company ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onCompanySelected(company) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedCompany?.id == company.id,
                            onClick = { viewModel.onCompanySelected(company) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(company.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(R.string.place_order),
                onClick = { viewModel.placeOrder(context, onSuccess = onOrderSuccess) },
                isLoading = uiState.isLoading
            )
        }
    }
}