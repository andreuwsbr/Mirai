package com.andrews.mirai.presentation.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrews.mirai.presentation.downloads.components.DownloadedMangaCard
import java.util.Locale

@Composable
fun DownloadsScreen(
    onBackClick: () -> Unit,
    viewModel: DownloadsViewModel = viewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val expandedMangas =
        remember {
            mutableStateMapOf<String, Boolean>()
        }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DownloadsTopBar(
            hasDownloads =
                uiState.mangas.isNotEmpty(),
            onBackClick = onBackClick,
            onDeleteAllClick = {
                viewModel.requestDeleteAll()
            }
        )

        HorizontalDivider()

        when {
            uiState.isLoading -> {
                DownloadsLoadingContent()
            }

            uiState.mangas.isEmpty() -> {
                DownloadsEmptyContent()
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 28.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DownloadsStorageSummary(
                            totalSizeBytes =
                                uiState.totalSizeBytes,
                            totalCompletedChapters =
                                uiState.totalCompletedChapters,
                            totalMangas =
                                uiState.mangas.size
                        )
                    }

                    items(
                        items = uiState.mangas,
                        key = { manga ->
                            "${manga.sourceId}:${manga.mangaId}"
                        }
                    ) { manga ->
                        val mangaKey =
                            "${manga.sourceId}:${manga.mangaId}"

                        val expanded =
                            expandedMangas[
                                mangaKey
                            ] ?: false

                        DownloadedMangaCard(
                            manga = manga,
                            expanded = expanded,
                            onExpandClick = {
                                expandedMangas[
                                    mangaKey
                                ] = !expanded
                            },
                            onDeleteMangaClick = {
                                viewModel.requestDeleteManga(
                                    manga
                                )
                            },
                            onDeleteChapterClick = {
                                    chapterId ->

                                val chapter =
                                    manga.chapters
                                        .firstOrNull {
                                                item ->
                                            item.chapterId ==
                                                    chapterId
                                        }

                                if (chapter != null) {
                                    viewModel
                                        .requestDeleteChapter(
                                            chapter
                                        )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    DownloadsDialogs(
        uiState = uiState,
        onDismiss = {
            viewModel.dismissDialog()
        },
        onConfirmDeleteChapter = {
            viewModel.confirmDeleteChapter()
        },
        onConfirmDeleteManga = {
            viewModel.confirmDeleteManga()
        },
        onConfirmDeleteAll = {
            viewModel.confirmDeleteAll()
        },
        onDismissError = {
            viewModel.clearError()
        }
    )
}

@Composable
private fun DownloadsTopBar(
    hasDownloads: Boolean,
    onBackClick: () -> Unit,
    onDeleteAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Outlined
                        .ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Text(
            text = "Gerenciar downloads",
            modifier = Modifier.weight(1f),
            style =
                MaterialTheme
                    .typography
                    .titleLarge
        )

        IconButton(
            onClick = onDeleteAllClick,
            enabled = hasDownloads
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.DeleteSweep,
                contentDescription =
                    "Excluir todos os downloads"
            )
        }
    }
}

@Composable
private fun DownloadsStorageSummary(
    totalSizeBytes: Long,
    totalCompletedChapters: Int,
    totalMangas: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
                .copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Storage,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Text(
                    text = "Armazenamento offline",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = formatFileSize(
                    totalSizeBytes
                ),
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "$totalMangas obra(s) • " +
                            "$totalCompletedChapters capítulo(s)",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DownloadsEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.DownloadDone,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Nenhum download",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "Os capítulos baixados aparecerão aqui.",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DownloadsDialogs(
    uiState: DownloadsUiState,
    onDismiss: () -> Unit,
    onConfirmDeleteChapter: () -> Unit,
    onConfirmDeleteManga: () -> Unit,
    onConfirmDeleteAll: () -> Unit,
    onDismissError: () -> Unit
) {
    uiState.chapterPendingDeletion
        ?.let { chapter ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Excluir capítulo?"
                    )
                },
                text = {
                    Text(
                        text =
                            "\"${chapter.name}\" será " +
                                    "removido do armazenamento offline."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick =
                            onConfirmDeleteChapter
                    ) {
                        Text(
                            text = "Excluir"
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancelar"
                        )
                    }
                }
            )
        }

    uiState.mangaPendingDeletion
        ?.let { manga ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Excluir obra?"
                    )
                },
                text = {
                    Text(
                        text =
                            "Todos os capítulos baixados de " +
                                    "\"${manga.title}\" serão excluídos."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick =
                            onConfirmDeleteManga
                    ) {
                        Text(
                            text = "Excluir"
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancelar"
                        )
                    }
                }
            )
        }

    if (
        uiState.showDeleteAllConfirmation
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Excluir todos os downloads?"
                )
            },
            text = {
                Text(
                    text =
                        "Todas as obras e capítulos salvos " +
                                "offline serão removidos. " +
                                "Favoritos e histórico não serão apagados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick =
                        onConfirmDeleteAll
                ) {
                    Text(
                        text = "Excluir tudo"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "Cancelar"
                    )
                }
            }
        )
    }

    uiState.errorMessage
        ?.let { message ->
            AlertDialog(
                onDismissRequest =
                    onDismissError,
                title = {
                    Text(
                        text = "Não foi possível concluir"
                    )
                },
                text = {
                    Text(
                        text = message
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick =
                            onDismissError
                    ) {
                        Text(
                            text = "Entendi"
                        )
                    }
                }
            )
        }
}

private fun formatFileSize(
    sizeBytes: Long
): String {
    val safeSize =
        sizeBytes.coerceAtLeast(0L)

    val kilobyte =
        1024.0

    val megabyte =
        kilobyte * 1024.0

    val gigabyte =
        megabyte * 1024.0

    return when {
        safeSize >= gigabyte -> {
            String.format(
                Locale.getDefault(),
                "%.2f GB",
                safeSize / gigabyte
            )
        }

        safeSize >= megabyte -> {
            String.format(
                Locale.getDefault(),
                "%.1f MB",
                safeSize / megabyte
            )
        }

        safeSize >= kilobyte -> {
            String.format(
                Locale.getDefault(),
                "%.0f KB",
                safeSize / kilobyte
            )
        }

        else -> {
            "$safeSize B"
        }
    }
}