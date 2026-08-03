package com.andrews.mirai.presentation.downloads.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.presentation.downloads.DownloadedChapterUiModel
import java.util.Locale

@Composable
fun DownloadedChapterItem(
    chapter: DownloadedChapterUiModel,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(alpha = 0.45f),
        shape =
            MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    top = 10.dp,
                    end = 6.dp,
                    bottom = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.name,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall
                )

                Text(
                    text = chapterSubtitle(
                        chapter
                    ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                if (
                    chapter.status !=
                    DownloadStatus.COMPLETED
                ) {
                    Text(
                        text = statusText(
                            chapter
                        ),
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            if (
                                chapter.status ==
                                DownloadStatus.FAILED
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            }
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription =
                        "Excluir capítulo"
                )
            }
        }
    }
}

private fun chapterSubtitle(
    chapter: DownloadedChapterUiModel
): String {
    val parts =
        buildList {
            if (chapter.totalPages > 0) {
                add(
                    "${chapter.totalPages} páginas"
                )
            }

            if (chapter.sizeBytes > 0L) {
                add(
                    formatFileSize(
                        chapter.sizeBytes
                    )
                )
            }
        }

    return if (parts.isEmpty()) {
        "Sem informações de tamanho"
    } else {
        parts.joinToString(
            separator = " • "
        )
    }
}

private fun statusText(
    chapter: DownloadedChapterUiModel
): String {
    return when (chapter.status) {
        DownloadStatus.QUEUED ->
            "Aguardando download"

        DownloadStatus.DOWNLOADING ->
            "Baixando ${chapter.progressPercent.coerceIn(0, 100)}%"

        DownloadStatus.COMPLETED ->
            "Disponível offline"

        DownloadStatus.FAILED ->
            "Falha no download"

        DownloadStatus.PAUSED ->
            "Download pausado"
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