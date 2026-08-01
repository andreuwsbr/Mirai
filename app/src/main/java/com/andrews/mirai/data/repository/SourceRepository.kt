package com.andrews.mirai.data.repository

import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.SourceRegistry

object SourceRepository {

    val sources: List<MangaSource>
        get() = SourceRegistry.all()

    var currentSource: MangaSource =
        SourceRegistry.default()

    fun selectSource(sourceId: String): Boolean {
        val source = SourceRegistry.findById(sourceId)
            ?: return false

        currentSource = source
        return true
    }
}