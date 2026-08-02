package com.andrews.mirai.presentation.auth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier

@Composable
fun CloudSyncDialog(
    isSyncing: Boolean,
    errorMessage: String?,
    onSyncClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSyncing) {
                onSkipClick()
            }
        },
        title = {
            Text(
                text = "Sincronizar seus dados?"
            )
        },
        text = {
            Text(
                text =
                    errorMessage
                        ?: "O Mirai encontrou favoritos e histórico " +
                        "salvos neste dispositivo. Deseja enviá-los " +
                        "para sua conta e mesclar com os dados da nuvem?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSyncClick,
                enabled = !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sincronizar"
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onSkipClick,
                enabled = !isSyncing
            ) {
                Text(
                    text = "Agora não"
                )
            }
        }
    )
}