package com.andrews.mirai.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MangaCard

@Composable
fun MangaHorizontalRow(
    mangas: List<Manga>,
    modifier: Modifier = Modifier,
    onMangaClick: (Manga) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val horizontalPadding = 20.dp
        val itemSpacing = 12.dp

        val cardWidth =
            (maxWidth - 56.dp) / 2.45f

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding
            ),
            horizontalArrangement =
                Arrangement.spacedBy(itemSpacing)
        ) {
            items(
                items = mangas,
                key = { manga ->
                    manga.id
                }
            ) { manga ->
                MangaCard(
                    manga = manga,
                    modifier = Modifier.width(
                        cardWidth
                    ),
                    onClick = {
                        onMangaClick(manga)
                    }
                )
            }
        }
    }
}