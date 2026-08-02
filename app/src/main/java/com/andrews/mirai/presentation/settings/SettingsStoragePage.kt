package com.andrews.mirai.presentation.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import java.io.File
import java.util.Locale

@Composable
internal fun StorageSettingsPage(
    onBackClick: () -> Unit
) {
    val context =
        LocalContext.current.applicationContext

    val progressStore = remember(context) {
        ReadingProgressStore(context)
    }

    var cacheSize by remember {
        mutableStateOf(
            calculateCacheSize(context)
        )
    }

    var showCacheDialog by remember {
        mutableStateOf(false)
    }

    var showHistoryDialog by remember {
        mutableStateOf(false)
    }

    SettingsSubpage(
        title = "Armazenamento",
        onBackClick = onBackClick
    ) {
        SettingsSectionTitle("Cache")

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title = "Limpar cache",
            subtitle = "Espaço utilizado: $cacheSize",
            onClick = {
                showCacheDialog = true
            }
        )

        SettingsSectionTitle("Histórico")

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title = "Limpar histórico de leitura",
            subtitle = "Remove a lista de capítulos acessados",
            onClick = {
                showHistoryDialog = true
            }
        )
    }

    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = {
                showCacheDialog = false
            },
            title = {
                Text("Limpar cache?")
            },
            text = {
                Text(
                    "As imagens temporárias serão removidas. " +
                            "Seus favoritos e seu histórico não serão apagados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearApplicationCache(context)

                        cacheSize =
                            calculateCacheSize(context)

                        showCacheDialog = false

                        Toast.makeText(
                            context,
                            "Cache limpo com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCacheDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showHistoryDialog = false
            },
            title = {
                Text("Limpar histórico?")
            },
            text = {
                Text(
                    "A lista de leituras recentes será removida. " +
                            "Os favoritos não serão afetados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressStore.clearHistory()

                        showHistoryDialog = false

                        Toast.makeText(
                            context,
                            "Histórico removido.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showHistoryDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun calculateCacheSize(
    context: Context
): String {
    return formatBytes(
        directorySize(context.cacheDir)
    )
}

private fun directorySize(
    file: File?
): Long {
    if (file == null || !file.exists()) {
        return 0L
    }

    if (file.isFile) {
        return file.length()
    }

    return file.listFiles()
        ?.sumOf { child ->
            directorySize(child)
        }
        ?: 0L
}

private fun clearApplicationCache(
    context: Context
) {
    context.cacheDir
        .listFiles()
        ?.forEach { file ->
            runCatching {
                file.deleteRecursively()
            }
        }
}

private fun formatBytes(
    bytes: Long
): String {
    if (bytes <= 0L) {
        return "0 B"
    }

    val units = listOf(
        "B",
        "KB",
        "MB",
        "GB"
    )

    var value = bytes.toDouble()
    var unitIndex = 0

    while (
        value >= 1024.0 &&
        unitIndex < units.lastIndex
    ) {
        value /= 1024.0
        unitIndex++
    }

    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        value,
        units[unitIndex]
    )
}