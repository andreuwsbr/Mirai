package com.andrews.mirai.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.andrews.mirai.data.source.SourceRegistry
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.home.components.ContinueReadingCard
import com.andrews.mirai.presentation.home.components.HomeSection
import com.andrews.mirai.presentation.home.components.MangaHorizontalRow

@Composable
fun HomeScreen() {

    val source = SourceRegistry.default()

    val items = listOf(
        Manga("1", "Torre Celestial", "Demonstração"),
        Manga("2", "Lua Vermelha", "Demonstração"),
        Manga("3", "O Imperador Renascido", "Demonstração"),
        Manga("4", "Espada do Norte", "Demonstração")
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        MiraiHeader(
            title = "Mirai",
            subtitle = "Fonte ativa: ${source.name}"
        )

        ContinueReadingCard(
            title = "Solo Leveling",
            chapter = "Capítulo 178",
            progress = 0.81f
        )

        HomeSection(
            title = "Em destaque"
        ) {
            MangaHorizontalRow(
                mangas = items
            )
        }

        HomeSection(
            title = "Recentes"
        ) {
            MangaHorizontalRow(
                mangas = items
            )
        }

        HomeSection(
            title = "Populares"
        ) {
            MangaHorizontalRow(
                mangas = items
            )
        }
    }
}