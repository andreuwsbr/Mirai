package com.andrews.mirai.presentation.home.components

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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.presentation.components.createMiraiImageModel

@Composable
fun ContinueReadingCard(
    title: String,
    chapter: String,
    sourceName: String,
    coverModel: Any?,
    currentPage: Int,
    totalPages: Int,
    onContinueClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val context =
        LocalContext.current

    val resolvedCoverModel =
        createMiraiImageModel(
            context = context,
            model = coverModel
        )

    val safeCurrentPage =
        if (totalPages > 0) {
            currentPage.coerceIn(
                minimumValue = 1,
                maximumValue = totalPages
            )
        } else {
            currentPage.coerceAtLeast(1)
        }

    val progress =
        if (totalPages > 0) {
            (
                    safeCurrentPage.toFloat() /
                            totalPages.toFloat()
                    ).coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
        } else {
            0f
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp
            ),
        shape =
            RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {
        Row(
            modifier =
                Modifier.padding(16.dp),
            horizontalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model =
                    resolvedCoverModel,
                contentDescription =
                    "Capa de $title",
                contentScale =
                    ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = 82.dp,
                        height = 120.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "CONTINUE LENDO",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text = title,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = chapter,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = sourceName,
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
                    modifier =
                        Modifier.height(12.dp)
                )

                LinearProgressIndicator(
                    progress = {
                        progress
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        if (totalPages > 0) {
                            "Página $safeCurrentPage de $totalPages"
                        } else {
                            "Página $safeCurrentPage"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    bottom = 14.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick =
                    onDetailsClick
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Info,
                    contentDescription = null
                )

                Text(
                    text = "Detalhes"
                )
            }

            Button(
                onClick =
                    onContinueClick
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.PlayArrow,
                    contentDescription = null
                )

                Text(
                    text = "Continuar"
                )
            }
        }
    }
}