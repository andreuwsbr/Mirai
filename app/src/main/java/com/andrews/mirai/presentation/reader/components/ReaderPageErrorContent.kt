package com.andrews.mirai.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ReaderPageErrorContent(
    pageNumber: Int,
    message: String,
    retrying: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(
                min = 260.dp
            )
            .padding(
                horizontal = 28.dp,
                vertical = 36.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text =
                "Não foi possível carregar a página $pageNumber",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            textAlign =
                TextAlign.Center
        )

        Text(
            text = message,
            modifier =
                Modifier.padding(
                    top = 10.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            textAlign =
                TextAlign.Center
        )

        Button(
            enabled = !retrying,
            onClick = onRetryClick,
            modifier =
                Modifier.padding(
                    top = 20.dp
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
                        "Tentando novamente..."
                    } else {
                        "Tentar novamente"
                    },
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            )
        }
    }
}