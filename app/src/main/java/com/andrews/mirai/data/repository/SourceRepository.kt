package com.andrews.mirai.data.repository

import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.mangalivre.MangaLivreSource

object SourceRepository {

    private val mangaLivreSource: MangaSource = MangaLivreSource()

    val currentSource: MangaSource
        get() = mangaLivreSource
}