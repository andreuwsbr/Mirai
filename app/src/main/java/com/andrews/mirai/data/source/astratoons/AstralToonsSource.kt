package com.andrews.mirai.data.source.astratoons

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.astratoons.parser.AstralCatalogParser
import com.andrews.mirai.data.source.astratoons.parser.AstralChapterApiParser
import com.andrews.mirai.data.source.astratoons.parser.AstralChapterParser
import com.andrews.mirai.data.source.astratoons.parser.AstralComicIdParser
import com.andrews.mirai.data.source.astratoons.parser.AstralDetailsParser
import com.andrews.mirai.data.source.astratoons.parser.AstralPageParser
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage

class AstralToonsSource : MangaSource {

    private val http =
        HttpClient

    private val catalogParser by lazy {
        AstralCatalogParser(baseUrl)
    }

    private val detailsParser by lazy {
        AstralDetailsParser(baseUrl)
    }

    private val chapterParser by lazy {
        AstralChapterParser(baseUrl)
    }

    private val pageParser by lazy {
        AstralPageParser(baseUrl)
    }

    private val comicIdParser by lazy {
        AstralComicIdParser()
    }

    private val chapterApiParser by lazy {
        AstralChapterApiParser()
    }

    override val id: String
        get() = "astraltoons"

    override val name: String
        get() = "AstraToons"

    override val baseUrl: String
        get() = AstralToonsUrls.BASE_URL

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {
        val response = http.get(
            AstralToonsUrls.popular(page)
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return catalogParser.parse(
            response.body
        )
    }

    override suspend fun search(
        query: String,
        page: Int
    ): List<Manga> {
        val normalizedQuery =
            query.trim()

        if (normalizedQuery.isBlank()) {
            return getPopular(page)
        }

        val response = http.get(
            AstralToonsUrls.search(
                query = normalizedQuery,
                page = page
            )
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return catalogParser.parse(
            response.body
        )
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val detailsUrl =
            AstralToonsUrls.resolve(
                manga.id
            )

        if (detailsUrl.isBlank()) {
            return manga
        }

        val response =
            http.get(detailsUrl)

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
        val detailsUrl =
            AstralToonsUrls.resolve(
                manga.id
            )

        if (detailsUrl.isBlank()) {
            return emptyList()
        }

        val detailsResponse =
            http.get(detailsUrl)

        if (!detailsResponse.isSuccessful) {
            return emptyList()
        }

        val allChapters =
            mutableListOf<Chapter>()

        allChapters += chapterParser.parse(
            html = detailsResponse.body,
            manga = manga
        )

        val comicId = comicIdParser.parse(
            detailsResponse.body
        ) ?: return allChapters
            .distinctBy { chapter ->
                chapter.id
            }
            .sortedByDescending { chapter ->
                chapter.number
            }

        var page = 2
        var hasMore = true

        while (
            hasMore &&
            page <= MAXIMUM_CHAPTER_PAGES
        ) {
            val apiResponse = http.get(
                AstralToonsUrls.chaptersApi(
                    comicId = comicId,
                    page = page
                )
            )

            if (!apiResponse.isSuccessful) {
                break
            }

            val apiPage =
                chapterApiParser.parse(
                    apiResponse.body
                ) ?: break

            if (apiPage.html.isBlank()) {
                break
            }

            val pageChapters =
                chapterParser.parse(
                    html = apiPage.html,
                    manga = manga
                )

            if (pageChapters.isEmpty()) {
                break
            }

            allChapters += pageChapters

            hasMore =
                apiPage.hasMore

            page++
        }

        return allChapters
            .distinctBy { chapter ->
                chapter.id
            }
            .sortedByDescending { chapter ->
                chapter.number
            }
    }

    override suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        val chapterUrl =
            AstralToonsUrls.resolve(
                chapter.url.ifBlank {
                    chapter.id
                }
            )

        if (chapterUrl.isBlank()) {
            return emptyList()
        }

        val response =
            http.get(chapterUrl)

        if (!response.isSuccessful) {
            return emptyList()
        }

        return pageParser.parse(
            html = response.body
        )
    }

    private companion object {
        const val MAXIMUM_CHAPTER_PAGES =
            100
    }
}