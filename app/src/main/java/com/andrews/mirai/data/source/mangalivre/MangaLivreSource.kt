package com.andrews.mirai.data.source.mangalivre

import com.andrews.mirai.data.source.madara.MadaraSource
import com.andrews.mirai.data.source.madara.MadaraSourceConfig
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage

class MangaLivreSource : MadaraSource(

    config = MadaraSourceConfig(
        id = "mangalivre",
        name = "Manga Livre",
        baseUrl = "https://mangalivre.blog"
    )

) {

    private val detailsParser by lazy {
        DetailsParser(baseUrl)
    }

    private val chapterParser by lazy {
        ChapterParser(baseUrl)
    }

    private val pageParser by lazy {
        PageParser(baseUrl)
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val detailsUrl = resolveUrl(manga.id)

        if (detailsUrl.isBlank()) {
            return manga
        }

        val response = http.get(detailsUrl)

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
        val detailsUrl = resolveUrl(manga.id)

        if (detailsUrl.isBlank()) {
            return emptyList()
        }

        val response = http.get(detailsUrl)

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
        val chapterUrl = resolveUrl(
            chapter.url.ifBlank {
                chapter.id
            }
        )

        if (chapterUrl.isBlank()) {
            return emptyList()
        }

        val response = http.get(chapterUrl)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return pageParser.parse(
            html = response.body
        )
    }

    private fun resolveUrl(
        value: String
    ): String {
        val normalizedValue = value.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return when {
            normalizedValue.startsWith(
                "https://",
                ignoreCase = true
            ) -> normalizedValue

            normalizedValue.startsWith(
                "http://",
                ignoreCase = true
            ) -> normalizedValue

            else -> {
                "$baseUrl/${normalizedValue.trimStart('/')}"
            }
        }
    }
}