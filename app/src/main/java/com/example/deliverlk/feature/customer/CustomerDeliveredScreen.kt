package com.example.deliverlk.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.*
import com.example.deliverlk.domain.repository.ProofStorage
import com.example.deliverlk.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDeliveredScreen(
    viewModel: CustomerDeliveredViewModel,
    onNavigateBack: () -> Unit,
    proofStorage: ProofStorage? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delivery = uiState.delivery
    var issueNote by remember { mutableStateOf("") }
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
                title = stringResource(R.string.delivery_complete),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(80.dp)
            )

            Text(stringResource(R.string.package_delivered_success), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.delivered_at, delivery?.deliveryTime ?: "10:30 AM"), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Proof of delivery card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.proof_of_delivery), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (delivery != null) {
                        ProofPhoto(
                            deliveryId = delivery.id,
                            proofId = delivery.proofPhotoUri ?: "final",
                            proofStorage = proofStorage,
                            fallbackPhotoUri = delivery.proofPhotoUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            // Report issue card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.report_issue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = issueNote,
                        onValueChange = { issueNote = it },
                        placeholder = { Text(stringResource(R.string.describe_issue_desc)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = stringResource(R.string.submit),
                        onClick = { viewModel.submitIssue(issueNote) },
                        icon = Icons.Default.ReportProblem
                    )
                }
            }

            PrimaryButton(
                text = stringResource(R.string.back),
                onClick = onNavigateBack
            )
        }
    }
}