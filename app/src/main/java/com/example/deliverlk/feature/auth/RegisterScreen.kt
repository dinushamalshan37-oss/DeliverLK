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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.LabeledTextField
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.example.deliverlk.core.designsystem.RoleCard
import com.example.deliverlk.domain.model.Role
import com.example.deliverlk.ui.theme.DeliverLKTheme

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRoleSelected: (Role) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Join DeliverLK fleet platform",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Role Selection Cards
        Text(
            text = stringResource(R.string.select_role),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        RoleCard(
            title = stringResource(R.string.role_manager),
            description = stringResource(R.string.role_manager_desc),
            icon = Icons.Default.BusinessCenter,
            selected = uiState.selectedRole == Role.MANAGER,
            onClick = { onRoleSelected(Role.MANAGER) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoleCard(
            title = stringResource(R.string.role_driver),
            description = stringResource(R.string.role_driver_desc),
            icon = Icons.Default.LocalShipping,
            selected = uiState.selectedRole == Role.DRIVER,
            onClick = { onRoleSelected(Role.DRIVER) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoleCard(
            title = stringResource(R.string.role_customer),
            description = stringResource(R.string.role_customer_desc),
            icon = Icons.Default.Person,
            selected = uiState.selectedRole == Role.CUSTOMER,
            onClick = { onRoleSelected(Role.CUSTOMER) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = stringResource(R.string.full_name),
            placeholder = "e.g. Kasun Perera",
            errorText = uiState.nameError
        )

        LabeledTextField(
            value = uiState.phone,
            onValueChange = onPhoneChanged,
            label = stringResource(R.string.phone_number),
            placeholder = "+94 77 123 4567",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            errorText = uiState.phoneError
        )

        LabeledTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.email),
            placeholder = "e.g. kasun@deliverlk.lk",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            errorText = uiState.emailError
        )

        LabeledTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = stringResource(R.string.password),
            placeholder = "••••••••",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            errorText = uiState.passwordError
        )

        LabeledTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = stringResource(R.string.confirm_password),
            placeholder = "••••••••",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            errorText = uiState.confirmPasswordError
        )

        if (uiState.generalError != null) {
            Text(
                text = uiState.generalError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.register),
            onClick = onRegisterClick,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = stringResource(R.string.already_have_account),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.register),
                onBackClick = onNavigateToLogin
            )
        }
    ) { innerPadding ->
        RegisterContent(
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onPhoneChanged = viewModel::onPhoneChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onRoleSelected = viewModel::onRoleSelected,
            onRegisterClick = { viewModel.register { onRegisterSuccess() } },
            onNavigateToLogin = onNavigateToLogin,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun RegisterContentPreviewHelper() {
    RegisterContent(
        uiState = RegisterUiState(),
        onNameChanged = {},
        onPhoneChanged = {},
        onEmailChanged = {},
        onPasswordChanged = {},
        onConfirmPasswordChanged = {},
        onRoleSelected = {},
        onRegisterClick = {},
        onNavigateToLogin = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    DeliverLKTheme {
        RegisterContentPreviewHelper()
    }
}
