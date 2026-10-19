package com.example.deliverlk.feature.manager

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.BuildConfig
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.AvatarInitials
import com.example.deliverlk.core.designsystem.ConfirmDialog
import com.example.deliverlk.core.designsystem.DeliverLkTopBar
import com.example.deliverlk.core.designsystem.LanguagePickerDialog
import com.example.deliverlk.core.designsystem.SecondaryButton
import com.example.deliverlk.core.designsystem.ThemePickerDialog

@Composable
fun ManagerProfileScreen(
    viewModel: ManagerProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    Scaffold(
        topBar = {
            DeliverLkTopBar(title = stringResource(R.string.profile))
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarInitials(name = user?.name ?: "Fleet Manager", size = 64.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user?.name ?: "Kasun Perera", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "manager@deliverlk.lk", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user?.company ?: "DeliverLK Logistics Sri Lanka", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.notifications), fontSize = 14.sp)
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Theme Selector Row
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

                    // Language Selector Row
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

            if (BuildConfig.DEBUG) {
                OutlinedButton(
                    onClick = { viewModel.seedDemoData() },
                    enabled = !uiState.isSeeding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSeeding) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seeding Firestore...")
                    } else {
                        Text("Seed Demo Data to Firestore")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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