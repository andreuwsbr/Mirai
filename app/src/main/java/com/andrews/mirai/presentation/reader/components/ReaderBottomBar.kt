package com.andrews.mirai.presentation.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    hasPreviousChapter: Boolean,
    hasNextChapter: Boolean,
    onPageSelected: (Int) -> Unit,
    onPreviousChapterClick: () -> Unit,
    onNextChapterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE6111725))
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TextButton(
                enabled = hasPreviousChapter,
                onClick = onPreviousChapterClick
            ) {
                Text(
                    text = "← Anterior",
                    color = if (hasPreviousChapter) {
                        Color.White
                    } else {
                        Color.Gray
                    }
                )
            }

            TextButton(
                enabled = hasNextChapter,
                onClick = onNextChapterClick
            ) {
                Text(
                    text = "Próximo →",
                    color = if (hasNextChapter) {
                        Color.White
                    } else {
                        Color.Gray
                    }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentPage + 1}",
                color = Color.White
            )

            Slider(
                value = currentPage.toFloat(),
                onValueChange = { value ->
                    onPageSelected(value.toInt())
                },
                valueRange = 0f..
                        (totalPages - 1)
                            .coerceAtLeast(0)
                            .toFloat(),
                steps = (totalPages - 2)
                    .coerceAtLeast(0),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )

            Text(
                text = "$totalPages",
                color = Color.White
            )

            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Settings,
                    contentDescription =
                        "Configurações",
                    tint = Color.White
                )
            }
        }

        LinearProgressIndicator(
            progress = {
                if (totalPages > 0) {
                    (currentPage + 1f) / totalPages
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}