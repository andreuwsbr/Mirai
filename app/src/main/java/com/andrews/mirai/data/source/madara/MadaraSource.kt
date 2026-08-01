package com.andrews.mirai.data.source.madara

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.madara.parser.MadaraHomeParser
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.ReaderPage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class MadaraSource(

    protected val config: MadaraSourceConfig

) : MangaSource {

    protected val http = HttpClient

    private val homeParser by lazy {
        MadaraHomeParser(config)
    }

    override val id: String
        get() = config.id

    override val name: String
        get() = config.name

    override val baseUrl: String
        get() = config.baseUrl

    protected fun buildSearchUrl(
        query: String,
        page: Int
    ): String {

        val encodedQuery = URLEncoder.encode(
            query.trim(),
            StandardCharsets.UTF_8.toString()
        )

        return if (page <= 1) {
            baseUrl + config.searchPath.format(encodedQuery)
        } else {
            baseUrl + config.searchPath.format(encodedQuery) +
                    "&paged=$page"
        }
    }

    protected fun buildPopularUrl(
        page: Int
    ): String {

        return if (page <= 1) {
            baseUrl + config.popularPath
        } else {
            "$baseUrl/page/$page/"
        }
    }

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {

        if (!config.supportsPopular) {
            return emptyList()
        }

        val response = http.get(
            buildPopularUrl(page)
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return homeParser.parse(
            response.body
        )
    }

    override suspend fun search(
        query: String,
        page: Int
    ): List<Manga> {

        if (!config.supportsSearch) {
            return emptyList()
        }

        val normalizedQuery = query.trim()

        if (normalizedQuery.isBlank()) {
            return getPopular(page)
        }

        val response = http.get(
            buildSearchUrl(
                normalizedQuery,
                page
            )
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return homeParser.parse(
            response.body
        )
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        error("getDetails() ainda não implementado")
    }

    override suspend fun getChapters(
        manga: Manga
    ): List<Chapter> {
        error("getChapters() ainda não implementado")
    }

    override suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        error("getPages() ainda não implementado")
    }
}