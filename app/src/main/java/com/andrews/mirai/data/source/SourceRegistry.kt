package com.andrews.mirai.data.source

import com.andrews.mirai.data.source.mangalivre.MangaLivreSource

object SourceRegistry {

    private val sources: List<MangaSource> = listOf(
        MangaLivreSource()
    )

    fun all(): List<MangaSource> = sources

    fun byId(id: String): MangaSource? =
        sources.firstOrNull { it.id == id }

    fun default(): MangaSource =
        sources.first()
}