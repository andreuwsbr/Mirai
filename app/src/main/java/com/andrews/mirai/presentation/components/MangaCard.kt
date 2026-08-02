package com.andrews.mirai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.domain.model.Manga

@Composable
fun MangaCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick
            )
            .padding(
                bottom = 4.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        if (!manga.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = manga.coverUrl,
                contentDescription =
                    "Capa de ${manga.title}",
                contentScale =
                    ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = manga.title
                        .take(1)
                        .uppercase(),
                    style =
                        MaterialTheme
                            .typography
                            .displayMedium
                )
            }
        }

        Text(
            text = manga.title,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 40.dp
                )
                .padding(
                    horizontal = 2.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis
        )

        Text(
            text = manga.type.displayName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 2.dp,
                    vertical = 1.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            maxLines = 1,
            overflow =
                TextOverflow.Clip
        )
    }
}