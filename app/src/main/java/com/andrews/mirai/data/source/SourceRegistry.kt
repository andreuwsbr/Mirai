package com.andrews.mirai.data.source

import com.andrews.mirai.data.source.mangalivre.MangaLivreSource

object SourceRegistry {

    private val registeredSources: List<MangaSource> = listOf(
        MangaLivreSource()
    )

    fun all(): List<MangaSource> {
        return registeredSources
    }

    fun default(): MangaSource {
        return registeredSources.first()
    }

    fun findById(
        sourceId: String
    ): MangaSource? {
        return registeredSources.firstOrNull { source ->
            source.id == sourceId
        }
    }
}