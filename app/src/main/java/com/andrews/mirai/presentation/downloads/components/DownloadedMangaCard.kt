package com.andrews.mirai.presentation.downloads.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.andrews.mirai.presentation.downloads.DownloadedMangaUiModel
import java.io.File
import java.util.Locale

@Composable
fun DownloadedMangaCard(
    manga: DownloadedMangaUiModel,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onDeleteMangaClick: () -> Unit,
    onDeleteChapterClick:
        (chapterId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onExpandClick
                    )
                    .padding(14.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter =
                        rememberAsyncImagePainter(
                            model =
                                coverModel(
                                    manga
                                )
                        ),
                    contentDescription =
                        "Capa de ${manga.title}",
                    modifier = Modifier
                        .size(
                            width = 72.dp,
                            height = 96.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        ),
                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = manga.title,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "${manga.completedChaptersCount} capítulo(s)",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            formatFileSize(
                                manga.sizeBytes
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
                }

                IconButton(
                    onClick = onDeleteMangaClick
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.DeleteOutline,
                        contentDescription =
                            "Excluir obra"
                    )
                }

                Icon(
                    imageVector =
                        if (expanded) {
                            Icons.Outlined.ExpandLess
                        } else {
                            Icons.Outlined.ExpandMore
                        },
                    contentDescription =
                        if (expanded) {
                            "Recolher capítulos"
                        } else {
                            "Mostrar capítulos"
                        }
                )
            }

            if (expanded) {
                HorizontalDivider()

                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        top = 12.dp,
                        end = 14.dp,
                        bottom = 14.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    manga.chapters.forEach {
                            chapter ->

                        DownloadedChapterItem(
                            chapter = chapter,
                            onDeleteClick = {
                                onDeleteChapterClick(
                                    chapter.chapterId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun coverModel(
    manga: DownloadedMangaUiModel
): Any? {
    val localPath =
        manga.coverLocalPath

    if (!localPath.isNullOrBlank()) {
        val localFile =
            File(localPath)

        if (
            localFile.exists() &&
            localFile.length() > 0L
        ) {
            return localFile
        }
    }

    return manga.coverUrl
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