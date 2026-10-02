package com.example.deliverlk.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.LabeledTextField
import com.example.deliverlk.core.designsystem.PrimaryButton
import com.example.deliverlk.ui.theme.DeliverLKTheme

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onDemoLoginClick: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Log in to your fleet account",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.resetMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = uiState.resetMessage,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        LabeledTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.email),
            placeholder = "e.g. manager@deliverlk.lk",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        LabeledTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = stringResource(R.string.password),
            placeholder = "••••••••",
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = stringResource(R.string.login),
            onClick = onLoginClick,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Demo login chips section
        Text(
            text = stringResource(R.string.demo_login),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = false,
                onClick = { onDemoLoginClick("manager@deliverlk.lk") },
                label = { Text(stringResource(R.string.demo_manager), fontSize = 12.sp) }
            )
            FilterChip(
                selected = false,
                onClick = { onDemoLoginClick("driver@deliverlk.lk") },
                label = { Text(stringResource(R.string.demo_driver), fontSize = 12.sp) }
            )
            FilterChip(
                selected = false,
                onClick = { onDemoLoginClick("customer@deliverlk.lk") },
                label = { Text(stringResource(R.string.demo_customer), fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onRegisterClick) {
            Text(
                text = stringResource(R.string.dont_have_account),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold { innerPadding ->
        LoginContent(
            uiState = uiState,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onLoginClick = { viewModel.login { onLoginSuccess() } },
            onDemoLoginClick = { email -> viewModel.fillDemoCredentials(email) },
            onForgotPasswordClick = viewModel::openForgotPassword,
            onRegisterClick = onNavigateToRegister,
            modifier = Modifier.padding(innerPadding)
        )

        if (uiState.isForgotPasswordOpen) {
            ForgotPasswordDialog(
                initialEmail = uiState.email,
                onDismiss = viewModel::closeForgotPassword,
                onSendReset = viewModel::sendPasswordReset
            )
        }
    }
}

@Composable
private fun LoginContentPreviewHelper() {
    LoginContent(
        uiState = LoginUiState(),
        onEmailChanged = {},
        onPasswordChanged = {},
        onTogglePasswordVisibility = {},
        onLoginClick = {},
        onDemoLoginClick = {},
        onForgotPasswordClick = {},
        onRegisterClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DeliverLKTheme {
        LoginContentPreviewHelper()
    }
}
