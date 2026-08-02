package com.andrews.mirai.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onContinueReading: (
        chapter: Chapter,
        sourceId: String
    ) -> Unit = { _, _ -> }
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val progressStore =
        remember(applicationContext) {
            ReadingProgressStore(
                applicationContext
            )
        }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    var showClearConfirmation by remember {
        mutableStateOf(false)
    }

    val history = remember(refreshKey) {
        progressStore
            .getHistory()
            .sortedByDescending { item ->
                item.readAt
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Histórico",
            subtitle =
                if (history.isEmpty()) {
                    "Continue exatamente de onde parou"
                } else {
                    "${history.size} obra(s) no histórico"
                }
        )

        if (history.isEmpty()) {
            EmptyHistory(
                modifier = Modifier.fillMaxSize()
            )

            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                ),
            horizontalArrangement =
                Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    showClearConfirmation = true
                }
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Delete,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Limpar histórico"
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = history,
                key = { item ->
                    "${item.sourceId}|${item.mangaId}"
                }
            ) { item ->
                HistoryCard(
                    item = item,
                    onContinueClick = {
                        val chapter = Chapter(
                            id = item.chapterId,
                            mangaId = item.mangaId,
                            name = item.chapterName,
                            number =
                                extractChapterNumber(
                                    item.chapterName
                                ),
                            url = item.chapterId
                        )

                        onContinueReading(
                            chapter,
                            item.sourceId
                        )
                    },
                    onRemoveClick = {
                        progressStore
                            .removeHistoryItem(
                                mangaId =
                                    item.mangaId,
                                sourceId =
                                    item.sourceId
                            )

                        refreshKey++
                    }
                )
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showClearConfirmation = false
            },
            title = {
                Text(
                    text = "Limpar histórico?"
                )
            },
            text = {
                Text(
                    text =
                        "Todas as obras serão removidas do histórico. " +
                                "O progresso salvo dos capítulos será mantido."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressStore.clearHistory()
                        showClearConfirmation = false
                        refreshKey++
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
                        showClearConfirmation = false
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

@Composable
private fun HistoryCard(
    item: ReadingHistoryItem,
    onContinueClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val safeTotalPages =
        item.totalPages.coerceAtLeast(0)

    val currentPage =
        if (safeTotalPages > 0) {
            (item.pageIndex + 1).coerceIn(
                minimumValue = 1,
                maximumValue = safeTotalPages
            )
        } else {
            item.pageIndex + 1
        }

    val progress =
        if (safeTotalPages > 0) {
            currentPage.toFloat() /
                    safeTotalPages.toFloat()
        } else {
            0f
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            AsyncImage(
                model = item.mangaCoverUrl,
                contentDescription =
                    "Capa de ${item.mangaTitle}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(82.dp)
                    .height(118.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.Top
                ) {
                    Text(
                        text = item.mangaTitle,
                        modifier =
                            Modifier.weight(1f),
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = onRemoveClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.Delete,
                            contentDescription =
                                "Remover do histórico"
                        )
                    }
                }

                Text(
                    text = item.chapterName,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        if (safeTotalPages > 0) {
                            "Página $currentPage de $safeTotalPages"
                        } else {
                            "Página $currentPage"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                if (safeTotalPages > 0) {
                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            progress.coerceIn(
                                minimumValue = 0f,
                                maximumValue = 1f
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        formatReadDate(
                            item.readAt
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Button(
                    onClick = onContinueClick,
                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {
                    Text(
                        text = "Continuar"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.History,
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
                text =
                    "Nenhuma leitura registrada.",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "As obras que você ler aparecerão aqui.",
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

private fun formatReadDate(
    timestamp: Long
): String {
    if (timestamp <= 0L) {
        return "Data não informada"
    }

    val formatter = SimpleDateFormat(
        "dd/MM/yyyy • HH:mm",
        Locale.getDefault()
    )

    return formatter.format(
        Date(timestamp)
    )
}

private fun extractChapterNumber(
    chapterName: String
): Double {
    return Regex(
        pattern = """\d+(?:[.,]\d+)?"""
    )
        .find(chapterName)
        ?.value
        ?.replace(",", ".")
        ?.toDoubleOrNull()
        ?: 0.0
}