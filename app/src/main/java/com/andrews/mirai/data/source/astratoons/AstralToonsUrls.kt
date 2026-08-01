package com.andrews.mirai.data.source.astratoons

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AstralToonsUrls {

    const val BASE_URL =
        "https://astratoons.com"

    private const val COMICS_PATH =
        "/comics"

    fun popular(
        page: Int
    ): String {
        return if (page <= 1) {
            "$BASE_URL$COMICS_PATH"
        } else {
            "$BASE_URL$COMICS_PATH?page=$page"
        }
    }

    fun search(
        query: String,
        page: Int
    ): String {
        val encodedQuery = URLEncoder.encode(
            query.trim(),
            StandardCharsets.UTF_8.toString()
        )

        return buildString {
            append(BASE_URL)
            append(COMICS_PATH)
            append("?search=")
            append(encodedQuery)

            if (page > 1) {
                append("&page=")
                append(page)
            }
        }
    }

    fun chaptersApi(
        comicId: Long,
        page: Int
    ): String {
        return buildString {
            append(BASE_URL)
            append("/api/comics/")
            append(comicId)
            append("/chapters")
            append("?search=")
            append("&order=desc")
            append("&page=")
            append(page)
        }
    }

    fun resolve(
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
                "$BASE_URL/${normalizedValue.trimStart('/')}"
            }
        }
    }
}