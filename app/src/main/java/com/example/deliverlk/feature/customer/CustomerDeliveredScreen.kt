package com.example.deliverlk.feature.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReportProblem
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
fun CustomerDeliveredScreen(
    viewModel: CustomerDeliveredViewModel,
    onNavigateBack: () -> Unit
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
                title = delivery?.trackingCode ?: "Delivered",
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

            Text("Package Delivered Successfully!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Delivered at ${delivery?.deliveryTime ?: "10:30 AM"}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Proof of delivery card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Proof of Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Delivery Photo Confirmation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Recipient: ${delivery?.recipientName ?: "Self"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SecondaryButton(
                text = stringResource(R.string.report_issue),
                onClick = viewModel::openIssueDialog,
                icon = Icons.Default.ReportProblem
            )
        }

        if (uiState.showIssueDialog) {
            AlertDialog(
                onDismissRequest = viewModel::closeIssueDialog,
                title = { Text("Report Issue", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Please describe your issue with this delivery.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = issueNote,
                            onValueChange = { issueNote = it },
                            placeholder = { Text("Describe problem...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.submitIssue(issueNote) }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeIssueDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
