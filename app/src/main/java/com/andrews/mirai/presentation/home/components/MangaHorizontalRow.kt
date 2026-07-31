package com.andrews.mirai.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MangaCard

@Composable
fun MangaHorizontalRow(
    mangas: List<Manga>,
    modifier: Modifier = Modifier,
    onMangaClick: (Manga) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = mangas,
            key = { it.id }
        ) { manga ->

            MangaCard(
                manga = manga,
                modifier = Modifier.width(130.dp),
                onClick = {
                    onMangaClick(manga)
                }
            )
        }
    }
}