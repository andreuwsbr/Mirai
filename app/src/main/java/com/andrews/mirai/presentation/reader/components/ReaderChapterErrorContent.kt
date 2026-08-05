package com.andrews.mirai.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ReaderChapterErrorContent(
    message: String,
    foregroundColor: Color,
    retrying: Boolean,
    onRetryClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    32.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text =
                "Não foi possível carregar o capítulo",
            color =
                foregroundColor,
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            textAlign =
                TextAlign.Center
        )

        Text(
            text = message,
            modifier =
                Modifier.padding(
                    top = 12.dp
                ),
            color =
                foregroundColor.copy(
                    alpha = 0.75f
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            textAlign =
                TextAlign.Center
        )

        Button(
            enabled = !retrying,
            onClick = onRetryClick,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Refresh,
                contentDescription = null
            )

            Text(
                text =
                    if (retrying) {
                        "Recarregando..."
                    } else {
                        "Recarregar capítulo"
                    },
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            )
        }
    }
}