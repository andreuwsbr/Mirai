package com.andrews.mirai.presentation.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrews.mirai.data.download.DownloadRepository
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import java.io.File
import java.util.Locale

@Composable
internal fun StorageSettingsPage(
    onBackClick: () -> Unit,
    onManageDownloadsClick: () -> Unit
) {
    val context =
        LocalContext.current.applicationContext

    val progressStore =
        remember(context) {
            ReadingProgressStore(
                context
            )
        }

    val downloadRepository =
        remember(context) {
            DownloadRepository(
                context
            )
        }

    val downloadsSizeBytes by
    downloadRepository
        .observeTotalSizeBytes()
        .collectAsStateWithLifecycle(
            initialValue = 0L
        )

    var cacheSize by remember {
        mutableStateOf(
            calculateCacheSize(
                context
            )
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
        SettingsSectionTitle(
            title = "Downloads"
        )

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.DownloadDone,
                    contentDescription = null
                )
            },
            title = "Gerenciar downloads",
            subtitle =
                "Espaço utilizado: ${
                    formatBytes(downloadsSizeBytes)
                }",
            onClick =
                onManageDownloadsClick
        )

        SettingsSectionTitle(
            title = "Cache"
        )

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title = "Limpar cache",
            subtitle =
                "Espaço utilizado: $cacheSize",
            onClick = {
                showCacheDialog = true
            }
        )

        SettingsSectionTitle(
            title = "Histórico"
        )

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title =
                "Limpar histórico de leitura",
            subtitle =
                "Remove a lista de capítulos acessados",
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
                Text(
                    text = "Limpar cache?"
                )
            },
            text = {
                Text(
                    text =
                        "As imagens temporárias serão removidas. " +
                                "Os capítulos baixados, favoritos e " +
                                "histórico não serão apagados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearApplicationCache(
                            context
                        )

                        cacheSize =
                            calculateCacheSize(
                                context
                            )

                        showCacheDialog =
                            false

                        Toast.makeText(
                            context,
                            "Cache limpo com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(
                        text = "Limpar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCacheDialog =
                            false
                    }
                ) {
                    Text(
                        text = "Cancelar"
                    )
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
                Text(
                    text = "Limpar histórico?"
                )
            },
            text = {
                Text(
                    text =
                        "A lista de leituras recentes será removida. " +
                                "Os favoritos e downloads não serão afetados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressStore
                            .clearHistory()

                        showHistoryDialog =
                            false

                        Toast.makeText(
                            context,
                            "Histórico removido.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(
                        text = "Limpar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showHistoryDialog =
                            false
                    }
                ) {
                    Text(
                        text = "Cancelar"
                    )
                }
            }
        )
    }
}

private fun calculateCacheSize(
    context: Context
): String {
    return formatBytes(
        directorySize(
            context.cacheDir
        )
    )
}

private fun directorySize(
    file: File?
): Long {
    if (
        file == null ||
        !file.exists()
    ) {
        return 0L
    }

    if (file.isFile) {
        return file.length()
    }

    return file
        .listFiles()
        ?.sumOf { child ->
            directorySize(
                child
            )
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

    val units =
        listOf(
            "B",
            "KB",
            "MB",
            "GB"
        )

    var value =
        bytes.toDouble()

    var unitIndex =
        0

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