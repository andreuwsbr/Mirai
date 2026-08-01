package com.andrews.mirai.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MangaCard
import com.andrews.mirai.presentation.components.MiraiHeader

@Composable
fun LibraryScreen(
    onMangaClick: (Manga) -> Unit
) {
    val favorites by FavoriteStore
        .favorites
        .collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Biblioteca",
            subtitle = if (favorites.isEmpty()) {
                "Suas obras favoritas"
            } else {
                "${favorites.size} obra(s) favorita(s)"
            }
        )

        if (favorites.isEmpty()) {
            EmptyLibrary(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp),
                verticalArrangement =
                    Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = favorites,
                    key = { manga ->
                        manga.id
                    }
                ) { manga ->
                    FavoriteMangaCard(
                        manga = manga,
                        onMangaClick = {
                            onMangaClick(manga)
                        },
                        onRemoveFavorite = {
                            FavoriteStore.toggleFavorite(
                                manga
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteMangaCard(
    manga: Manga,
    onMangaClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Box {
        MangaCard(
            manga = manga,
            onClick = onMangaClick
        )

        FilledIconButton(
            onClick = onRemoveFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Filled.Favorite,
                contentDescription =
                    "Remover ${manga.title} dos favoritos",
                tint =
                    MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun EmptyLibrary(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Text(
                text =
                    "Sua biblioteca ainda está vazia.",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                modifier = Modifier.padding(
                    top = 12.dp
                )
            )

            Text(
                text =
                    "Favorite uma obra para encontrá-la aqui.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                modifier = Modifier.padding(
                    top = 6.dp
                )
            )
        }
    }
}