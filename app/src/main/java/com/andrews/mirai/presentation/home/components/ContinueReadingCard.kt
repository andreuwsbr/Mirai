package com.andrews.mirai.presentation.home.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ContinueReadingCard(
    title: String,
    chapter: String,
    coverModel: Any?,
    currentPage: Int,
    totalPages: Int,
    onClick: () -> Unit
) {
    val progress =
        if (totalPages > 0) {
            (
                    currentPage.toFloat() /
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
            )
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(22.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {
        Row(
            modifier =
                Modifier.padding(18.dp),
            horizontalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = coverModel,
                contentDescription =
                    "Capa de $title",
                contentScale =
                    ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = 74.dp,
                        height = 108.dp
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
                            .primary
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = title,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold,
                    maxLines = 2
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
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
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
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        if (totalPages > 0) {
                            "Página $currentPage de $totalPages"
                        } else {
                            "Página $currentPage"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                )
            }
        }
    }
}