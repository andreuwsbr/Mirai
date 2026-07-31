package com.andrews.mirai.data.source

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage

interface MangaSource {
    val id: String
    val name: String
    val baseUrl: String

    suspend fun getPopular(page: Int = 1): List<Manga>
    suspend fun search(query: String, page: Int = 1): List<Manga>
    suspend fun getDetails(manga: Manga): Manga
    suspend fun getChapters(manga: Manga): List<Chapter>
    suspend fun getPages(chapter: Chapter): List<ReaderPage>
}
