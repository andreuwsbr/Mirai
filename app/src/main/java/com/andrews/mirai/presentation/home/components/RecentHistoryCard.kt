package com.andrews.mirai.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.presentation.components.createMiraiImageModel
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem
import java.util.concurrent.TimeUnit

@Composable
fun RecentHistoryCard(
    item: ReadingHistoryItem,
    coverModel: Any?,
    onDetailsClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context =
        LocalContext.current

    val resolvedCoverModel =
        createMiraiImageModel(
            context = context,
            model = coverModel
        )

    Surface(
        modifier = modifier
            .width(164.dp)
            .clickable(
                onClick = onDetailsClick
            ),
        shape =
            RoundedCornerShape(18.dp),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column {
            AsyncImage(
                model =
                    resolvedCoverModel,
                contentDescription =
                    "Capa de ${item.mangaTitle}",
                contentScale =
                    ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
                    .padding(12.dp),
                verticalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text =
                            item.mangaTitle,
                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            item.chapterName,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            formatRecentReadingTime(
                                item.readAt
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        maxLines = 1
                    )
                }

                FilledTonalButton(
                    onClick =
                        onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    contentPadding =
                        PaddingValues(
                            horizontal = 10.dp,
                            vertical = 0.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text(
                        text = "Continuar",
                        maxLines = 1,
                        overflow =
                            TextOverflow.Clip
                    )
                }
            }
        }
    }
}

private fun formatRecentReadingTime(
    timestamp: Long
): String {
    if (timestamp <= 0L) {
        return "Lido recentemente"
    }

    val elapsedMillis =
        (
                System.currentTimeMillis() -
                        timestamp
                )
            .coerceAtLeast(0L)

    val minutes =
        TimeUnit.MILLISECONDS
            .toMinutes(elapsedMillis)

    val hours =
        TimeUnit.MILLISECONDS
            .toHours(elapsedMillis)

    val days =
        TimeUnit.MILLISECONDS
            .toDays(elapsedMillis)

    return when {
        minutes < 1L -> {
            "Agora"
        }

        minutes < 60L -> {
            "Há $minutes min"
        }

        hours < 24L -> {
            if (hours == 1L) {
                "Há 1 hora"
            } else {
                "Há $hours horas"
            }
        }

        days == 1L -> {
            "Ontem"
        }

        days < 30L -> {
            "Há $days dias"
        }

        else -> {
            "Lido recentemente"
        }
    }
}