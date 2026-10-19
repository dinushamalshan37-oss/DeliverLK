package com.example.deliverlk.feature.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
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
import com.example.deliverlk.core.designsystem.AvatarInitials
import com.example.deliverlk.core.designsystem.ConfirmDialog
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.LanguagePickerDialog
import com.example.deliverlk.core.designsystem.SecondaryButton
import com.example.deliverlk.core.designsystem.ThemePickerDialog

@Composable
fun CustomerProfileScreen(
    viewModel: CustomerProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.profile),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarInitials(name = user?.name ?: "Nimali Fernando", size = 72.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user?.name ?: "Nimali Fernando", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "customer@deliverlk.lk", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user?.phone ?: "+94 76 555 1234", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Settings Card (Theme & Language Pickers)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openThemeDialog() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.dark_mode), fontSize = 14.sp)
                        }
                        Text(
                            text = uiState.themeMode.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Language Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openLanguageDialog() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.language), fontSize = 14.sp)
                        }
                        Text(
                            text = stringResource(R.string.select_language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SecondaryButton(
                text = stringResource(R.string.logout),
                onClick = viewModel::openLogoutDialog,
                icon = Icons.Default.ExitToApp
            )
        }

        if (uiState.showLanguageDialog) {
            LanguagePickerDialog(
                settingsRepository = viewModel.settingsRepository,
                onDismiss = viewModel::closeLanguageDialog
            )
        }

        if (uiState.showThemeDialog) {
            ThemePickerDialog(
                currentMode = uiState.themeMode,
                onModeSelected = { viewModel.setThemeMode(it) },
                onDismiss = viewModel::closeThemeDialog
            )
        }

        if (uiState.showLogoutDialog) {
            ConfirmDialog(
                title = stringResource(R.string.logout),
                message = stringResource(R.string.confirm_logout),
                onConfirm = { viewModel.logout(onLogoutSuccess) },
                onDismiss = viewModel::closeLogoutDialog
            )
        }
    }
}