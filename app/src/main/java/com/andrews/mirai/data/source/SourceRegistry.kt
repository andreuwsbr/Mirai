package com.andrews.mirai.data.source

import com.andrews.mirai.data.source.astratoons.AstralToonsSource
import com.andrews.mirai.data.source.mangalivre.MangaLivreSource
import com.andrews.mirai.data.source.niadd.NiaddSource
import com.andrews.mirai.data.source.saikai.SaikaiSource

object SourceRegistry {

    private val registeredSources:
            List<MangaSource> = listOf(
        MangaLivreSource(),
        AstralToonsSource(),
        NiaddSource(),
        SaikaiSource()
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
        return registeredSources
            .firstOrNull { source ->
                source.id == sourceId
            }
    }
}