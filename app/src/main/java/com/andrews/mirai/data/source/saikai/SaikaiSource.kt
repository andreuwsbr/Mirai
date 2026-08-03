package com.andrews.mirai.data.source.saikai

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SaikaiSource : MangaSource {

    private val http =
        HttpClient

    private val parser =
        SaikaiJsonParser()

    override val id: String
        get() = "saikaiscan"

    override val name: String
        get() = "Saikai Scan"

    override val baseUrl: String
        get() = SaikaiUrls.BASE_URL

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {
        return withContext(
            Dispatchers.IO
        ) {
            waitForApiRequest()

            val response =
                http.get(
                    url =
                        SaikaiUrls.popular(
                            page
                        ),
                    headers =
                        SaikaiHeaders
                            .apiHeaders()
                )

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            parser.parseMangas(
                response.body
            )
        }
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

        return withContext(
            Dispatchers.IO
        ) {
            waitForApiRequest()

            val response =
                http.get(
                    url =
                        SaikaiUrls.search(
                            query =
                                normalizedQuery,
                            page =
                                page
                        ),
                    headers =
                        SaikaiHeaders
                            .apiHeaders()
                )

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            parser.parseMangas(
                response.body
            )
        }
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val slug =
            SaikaiUrls.normalizeSlug(
                manga.id
            )

        if (slug.isBlank()) {
            return manga
        }

        return withContext(
            Dispatchers.IO
        ) {
            waitForApiRequest()

            val response =
                http.get(
                    url =
                        SaikaiUrls.details(
                            slug
                        ),
                    headers =
                        SaikaiHeaders
                            .apiHeaders()
                )

            if (!response.isSuccessful) {
                return@withContext manga
            }

            parser.parseDetails(
                json =
                    response.body,
                fallbackManga =
                    manga
            )
        }
    }

    override suspend fun getChapters(
        manga: Manga
    ): List<Chapter> {
        val slug =
            SaikaiUrls.normalizeSlug(
                manga.id
            )

        if (slug.isBlank()) {
            return emptyList()
        }

        return withContext(
            Dispatchers.IO
        ) {
            waitForApiRequest()

            val response =
                http.get(
                    url =
                        SaikaiUrls.chapters(
                            slug
                        ),
                    headers =
                        SaikaiHeaders
                            .apiHeaders()
                )

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            parser.parseChapters(
                json =
                    response.body,
                mangaId =
                    slug
            )
        }
    }

    override suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        val releaseId =
            chapter.id.trim()

        if (releaseId.isBlank()) {
            return emptyList()
        }

        return withContext(
            Dispatchers.IO
        ) {
            waitForApiRequest()

            val response =
                http.get(
                    url =
                        SaikaiUrls.release(
                            releaseId
                        ),
                    headers =
                        SaikaiHeaders
                            .apiHeaders()
                )

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            /*
             * Mantemos exatamente a ordem
             * recebida pela API.
             *
             * Não ordenar esta lista por URL,
             * nome de arquivo ou número.
             */
            parser.parsePages(
                response.body
            )
        }
    }

    private suspend fun waitForApiRequest() {
        val delayTime =
            synchronized(
                requestLock
            ) {
                val currentTime =
                    System.currentTimeMillis()

                val nextAllowedTime =
                    lastRequestTime +
                            MINIMUM_REQUEST_INTERVAL_MS

                val remainingTime =
                    (
                            nextAllowedTime -
                                    currentTime
                            ).coerceAtLeast(0L)

                lastRequestTime =
                    currentTime +
                            remainingTime

                remainingTime
            }

        if (delayTime > 0L) {
            delay(
                delayTime
            )
        }
    }

    private companion object {

        const val MINIMUM_REQUEST_INTERVAL_MS =
            1_000L

        val requestLock =
            Any()

        var lastRequestTime =
            0L
    }
}