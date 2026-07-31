package com.andrews.mirai.data.source.mangalivre

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MangaLivreSource : MangaSource {

    override val id = "mangalivre"

    override val name = "Manga Livre"

    override val baseUrl = "https://mangalivre.blog"

    private val http = HttpClient

    private val homeParser = HomeParser(baseUrl)

    private val detailsParser = DetailsParser(baseUrl)

    private val chapterParser = ChapterParser(baseUrl)

    private val pageParser = PageParser(baseUrl)

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {
        val url = if (page <= 1) {
            "$baseUrl/"
        } else {
            "$baseUrl/page/$page/"
        }

        val response = http.get(url)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return homeParser.parse(response.body)
    }

    override suspend fun search(
        query: String,
        page: Int
    ): List<Manga> {
        val normalizedQuery = query.trim()

        if (normalizedQuery.isBlank()) {
            return getPopular(page)
        }

        val encodedQuery = URLEncoder.encode(
            normalizedQuery,
            StandardCharsets.UTF_8.toString()
        )

        val url = if (page <= 1) {
            "$baseUrl/?s=$encodedQuery"
        } else {
            "$baseUrl/?s=$encodedQuery&paged=$page"
        }

        val response = http.get(url)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return homeParser.parse(response.body)
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val response = http.get(manga.id)

        if (!response.isSuccessful) {
            return manga
        }

        return detailsParser.parse(
            html = response.body,
            manga = manga
        )
    }

    override suspend fun getChapters(
        manga: Manga
    ): List<Chapter> {
        val response = http.get(manga.id)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return chapterParser.parse(
            html = response.body,
            manga = manga
        )
    }

    override suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        val chapterUrl = chapter.url.ifBlank {
            chapter.id
        }

        val response = http.get(chapterUrl)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return pageParser.parse(response.body)
    }
}