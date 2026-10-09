package com.example.deliverlk.feature.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.example.deliverlk.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfDeliveryScreen(
    viewModel: ProofOfDeliveryViewModel,
    onNavigateBack: () -> Unit,
    onDeliveryCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.proof_of_delivery),
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (uiState.isSuccessAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Delivery Complete!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(delivery?.customerName ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(delivery?.address ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Camera Box Placeholder UI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.capturedPhotoUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Camera Preview Placeholder", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Photo Captured Successfully", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PrimaryButton(
                                text = stringResource(R.string.capture_photo),
                                onClick = viewModel::onCapturePhoto,
                                icon = Icons.Default.CameraAlt,
                                modifier = Modifier.weight(1f)
                            )

                            SecondaryButton(
                                text = stringResource(R.string.upload_gallery),
                                onClick = viewModel::onUploadGallery,
                                icon = Icons.Default.PhotoLibrary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                LabeledTextField(
                    value = uiState.recipientName,
                    onValueChange = viewModel::onRecipientNameChanged,
                    label = stringResource(R.string.recipient_name),
                    placeholder = "e.g. Self / Security / Family Member"
                )

                LabeledTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChanged,
                    label = stringResource(R.string.note),
                    placeholder = "Optional notes regarding drop off...",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = stringResource(R.string.mark_complete),
                    onClick = viewModel::openConfirmDialog,
                    isLoading = uiState.isLoading
                )
            }
        }

        if (uiState.showConfirmDialog) {
            ConfirmDialog(
                title = "Confirm Delivery Completion",
                message = "Are you sure you want to mark order ${delivery?.trackingCode} as delivered?",
                onConfirm = { viewModel.markComplete(onSuccess = onDeliveryCompleted) },
                onDismiss = viewModel::closeConfirmDialog
            )
        }
    }
}
