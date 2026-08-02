package com.andrews.mirai.data.source.niadd

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.niadd.parser.NiaddCatalogParser
import com.andrews.mirai.data.source.niadd.parser.NiaddChapterParser
import com.andrews.mirai.data.source.niadd.parser.NiaddDetailsParser
import com.andrews.mirai.data.source.niadd.parser.NiaddPageParser
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.ceil

class NiaddSource : MangaSource {

    override val id: String =
        NiaddConfig.ID

    override val name: String =
        NiaddConfig.NAME

    override val baseUrl: String =
        NiaddConfig.BASE_URL

    private val http =
        HttpClient

    private val catalogParser by lazy {
        NiaddCatalogParser(baseUrl)
    }

    private val detailsParser by lazy {
        NiaddDetailsParser(baseUrl)
    }

    private val chapterParser by lazy {
        NiaddChapterParser(baseUrl)
    }

    private val pageParser by lazy {
        NiaddPageParser(baseUrl)
    }

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {
        val safePage =
            page.coerceAtLeast(1)

        val catalogUrl =
            if (safePage == 1) {
                "$baseUrl${
                    NiaddConfig.CATALOG_PATH
                }"
            } else {
                "$baseUrl/category/index_$safePage.html"
            }

        val response =
            http.get(catalogUrl)

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
            return emptyList()
        }

        val encodedQuery =
            URLEncoder.encode(
                normalizedQuery,
                StandardCharsets.UTF_8
                    .toString()
            )

        val searchUrl =
            "$baseUrl${
                NiaddConfig.SEARCH_PATH
            }?search_type=1&name=$encodedQuery"

        val response =
            http.get(searchUrl)

        if (!response.isSuccessful) {
            return emptyList()
        }

        val results =
            catalogParser.parse(
                response.body
            )

        val normalizedSearchText =
            normalizeSearchText(
                normalizedQuery
            )

        return results
            .withIndex()
            .sortedWith(
                compareBy { indexedManga ->
                    val normalizedTitle =
                        normalizeSearchText(
                            indexedManga.value.title
                        )

                    when {
                        normalizedTitle ==
                                normalizedSearchText -> {
                            0
                        }

                        normalizedTitle.startsWith(
                            normalizedSearchText
                        ) -> {
                            1
                        }

                        normalizedTitle.contains(
                            normalizedSearchText
                        ) -> {
                            2
                        }

                        else -> {
                            3
                        }
                    }
                }
            )
            .map { indexedManga ->
                indexedManga.value
            }
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val detailsUrl =
            resolveUrl(manga.id)

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
        val chaptersUrl =
            buildChaptersUrl(
                manga.id
            )

        if (chaptersUrl.isBlank()) {
            return emptyList()
        }

        val response =
            http.get(chaptersUrl)

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
        val chapterUrl =
            resolveUrl(
                chapter.url.ifBlank {
                    chapter.id
                }
            )

        if (chapterUrl.isBlank()) {
            return emptyList()
        }

        val initialResponse =
            http.get(chapterUrl)

        if (!initialResponse.isSuccessful) {
            return emptyList()
        }

        val initialImages =
            pageParser.parseImageUrls(
                initialResponse.body
            )

        val totalPages =
            pageParser.extractTotalPages(
                initialResponse.body
            )

        if (totalPages <= 1) {
            return initialImages.mapIndexed {
                    index,
                    imageUrl ->

                ReaderPage(
                    index = index,
                    imageUrl = imageUrl
                )
            }
        }

        val chapterRootUrl =
            chapterUrl
                .substringBefore("?")
                .substringBefore("#")
                .trimEnd('/')

        val groupCount =
            ceil(
                totalPages / 10.0
            )
                .toInt()
                .coerceAtLeast(1)

        val collectedImages =
            mutableListOf<String>()

        for (
        groupNumber in 1..groupCount
        ) {
            val groupUrl =
                "$chapterRootUrl-10-" +
                        "$groupNumber.html"

            val groupResponse =
                http.get(groupUrl)

            if (!groupResponse.isSuccessful) {
                continue
            }

            collectedImages.addAll(
                pageParser.parseImageUrls(
                    groupResponse.body
                )
            )
        }

        var uniqueImages =
            collectedImages.distinct()

        if (
            uniqueImages.size <
            totalPages
        ) {
            val fallbackImages =
                mutableListOf<String>()

            for (
            pageNumber in 1..totalPages
            ) {
                val pageUrl =
                    "$chapterRootUrl-" +
                            "$pageNumber.html"

                val pageResponse =
                    http.get(pageUrl)

                if (!pageResponse.isSuccessful) {
                    continue
                }

                val pageImage =
                    pageParser
                        .parseImageUrls(
                            pageResponse.body
                        )
                        .firstOrNull()

                if (pageImage != null) {
                    fallbackImages.add(
                        pageImage
                    )
                }
            }

            if (
                fallbackImages
                    .distinct()
                    .size >
                uniqueImages.size
            ) {
                uniqueImages =
                    fallbackImages.distinct()
            }
        }

        if (uniqueImages.isEmpty()) {
            uniqueImages =
                initialImages.distinct()
        }

        return uniqueImages
            .take(
                totalPages.coerceAtLeast(
                    uniqueImages.size
                )
            )
            .mapIndexed {
                    index,
                    imageUrl ->

                ReaderPage(
                    index = index,
                    imageUrl = imageUrl
                )
            }
    }

    private fun buildChaptersUrl(
        mangaId: String
    ): String {
        val detailsUrl =
            resolveUrl(mangaId)

        if (detailsUrl.isBlank()) {
            return ""
        }

        return when {
            detailsUrl.endsWith(
                suffix = "/chapters.html",
                ignoreCase = true
            ) -> {
                detailsUrl
            }

            detailsUrl.endsWith(
                suffix = ".html",
                ignoreCase = true
            ) -> {
                detailsUrl
                    .removeSuffix(".html") +
                        "/chapters.html"
            }

            else -> {
                detailsUrl.trimEnd('/') +
                        "/chapters.html"
            }
        }
    }

    private fun resolveUrl(
        value: String
    ): String {
        val normalizedValue =
            value.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return runCatching {
            URI(baseUrl)
                .resolve(normalizedValue)
                .toString()
        }.getOrDefault(
            normalizedValue
        )
    }

    private fun normalizeSearchText(
        value: String
    ): String {
        return value
            .lowercase()
            .replace(
                Regex(
                    pattern =
                        """[^a-z0-9À-ÿ]+"""
                ),
                " "
            )
            .trim()
    }
}