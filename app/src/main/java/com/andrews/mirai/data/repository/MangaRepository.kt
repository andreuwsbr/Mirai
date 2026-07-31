package com.andrews.mirai.data.repository

import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage

class MangaRepository(
    private val source: MangaSource
) {
    suspend fun popular(): List<Manga> = source.getPopular()
    suspend fun search(query: String): List<Manga> = source.search(query)
    suspend fun details(manga: Manga): Manga = source.getDetails(manga)
    suspend fun chapters(manga: Manga): List<Chapter> = source.getChapters(manga)
    suspend fun pages(chapter: Chapter): List<ReaderPage> = source.getPages(chapter)
}
