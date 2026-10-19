package com.example.deliverlk.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.LabeledTextField

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSendReset: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.reset_password), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = stringResource(R.string.reset_password_desc))
                Spacer(modifier = Modifier.height(12.dp))
                LabeledTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.email),
                    placeholder = "e.g. user@deliverlk.lk"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendReset(email) },
                enabled = email.isNotBlank()
            ) {
                Text(text = stringResource(R.string.send_reset_link))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}