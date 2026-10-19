package com.example.deliverlk.feature.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.ui.theme.SuccessGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfDeliveryScreen(
    viewModel: ProofOfDeliveryViewModel,
    onNavigateBack: () -> Unit,
    onDeliveryCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.onPhotoCaptured(tempPhotoUri)
        }
    }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoCaptured(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "images/pod_${System.currentTimeMillis()}.jpg")
            photoFile.parentFile?.mkdirs()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempPhotoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            showPermissionDialog = true
        }
    }

    fun triggerCameraCapture() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val photoFile = File(context.cacheDir, "images/pod_${System.currentTimeMillis()}.jpg")
            photoFile.parentFile?.mkdirs()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempPhotoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun triggerGalleryPick() {
        pickVisualMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Scaffold(
        topBar = {
            DeliverLkTopBar(
                title = stringResource(R.string.proof_of_delivery),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        text = stringResource(R.string.delivery_complete),
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

                // Camera Box / Preview UI
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
                                    .height(200.dp)
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
                                    Text(stringResource(R.string.camera_preview_placeholder), fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = uiState.capturedPhotoUri,
                                    contentDescription = stringResource(R.string.proof_of_delivery),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.capturedPhotoUri == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PrimaryButton(
                                    text = stringResource(R.string.capture_photo),
                                    onClick = { triggerCameraCapture() },
                                    icon = Icons.Default.CameraAlt,
                                    modifier = Modifier.weight(1f)
                                )

                                SecondaryButton(
                                    text = stringResource(R.string.upload_gallery),
                                    onClick = { triggerGalleryPick() },
                                    icon = Icons.Default.PhotoLibrary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SecondaryButton(
                                    text = stringResource(R.string.retake_photo),
                                    onClick = { triggerCameraCapture() },
                                    icon = Icons.Default.Refresh,
                                    modifier = Modifier.weight(1f)
                                )

                                SecondaryButton(
                                    text = stringResource(R.string.change_photo),
                                    onClick = { triggerGalleryPick() },
                                    icon = Icons.Default.PhotoLibrary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
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

                if (uiState.isError) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            text = stringResource(R.string.retry),
                            onClick = viewModel::openConfirmDialog,
                            isLoading = uiState.isLoading
                        )
                        SecondaryButton(
                            text = stringResource(R.string.complete_without_photo),
                            onClick = { viewModel.markCompleteWithoutPhoto(onSuccess = onDeliveryCompleted) },
                            enabled = !uiState.isLoading
                        )
                    }
                } else {
                    PrimaryButton(
                        text = stringResource(R.string.mark_complete),
                        onClick = viewModel::openConfirmDialog,
                        isLoading = uiState.isLoading
                    )
                }
            }
        }

        if (uiState.showConfirmDialog) {
            ConfirmDialog(
                title = stringResource(R.string.confirm_delivery_title),
                message = stringResource(R.string.confirm_delivery_msg, delivery?.trackingCode ?: ""),
                onConfirm = { viewModel.markCompleteWithContext(context, onSuccess = onDeliveryCompleted) },
                onDismiss = viewModel::closeConfirmDialog
            )
        }

        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text(stringResource(R.string.camera_permission_required), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.camera_permission_required)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${context.packageName}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}