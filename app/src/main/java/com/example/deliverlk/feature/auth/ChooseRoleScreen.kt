package com.example.deliverlk.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.deliverlk.core.designsystem.RoleCard
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.domain.model.User

@Composable
fun ChooseRoleScreen(
    viewModel: ChooseRoleViewModel,
    onSetupCompleted: (User) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.choose_your_role),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.choose_your_role),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.complete_profile_desc),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Role Selection Cards
            Text(
                text = stringResource(R.string.select_role),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            RoleCard(
                title = stringResource(R.string.role_manager),
                description = stringResource(R.string.role_manager_desc),
                icon = Icons.Default.BusinessCenter,
                selected = uiState.selectedRole == Role.MANAGER,
                onClick = { viewModel.onRoleSelected(Role.MANAGER) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoleCard(
                title = stringResource(R.string.role_driver),
                description = stringResource(R.string.role_driver_desc),
                icon = Icons.Default.LocalShipping,
                selected = uiState.selectedRole == Role.DRIVER,
                onClick = { viewModel.onRoleSelected(Role.DRIVER) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoleCard(
                title = stringResource(R.string.role_customer),
                description = stringResource(R.string.role_customer_desc),
                icon = Icons.Default.Person,
                selected = uiState.selectedRole == Role.CUSTOMER,
                onClick = { viewModel.onRoleSelected(Role.CUSTOMER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChanged,
                label = stringResource(R.string.phone_number),
                placeholder = "+94 77 123 4567",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                errorText = uiState.phoneError
            )

            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.finish_setup),
                onClick = { viewModel.completeSetup(onSetupCompleted) },
                isLoading = uiState.isLoading
            )
        }
    }
}