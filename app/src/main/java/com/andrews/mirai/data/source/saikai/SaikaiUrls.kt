package com.andrews.mirai.data.source.saikai

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object SaikaiUrls {

    const val BASE_URL =
        "https://housesaikai.net"

    const val API_BASE_URL =
        "https://api.housesaikai.net"

    const val IMAGE_BASE_URL =
        "https://s3-beta.housesaikai.net"

    private const val STORIES_PATH =
        "/api/stories"

    private const val RELEASES_PATH =
        "/api/releases"

    private const val DEFAULT_PAGE_SIZE =
        12

    fun popular(
        page: Int
    ): String {
        val safePage =
            page.coerceAtLeast(1)

        return buildString {
            append(API_BASE_URL)
            append(STORIES_PATH)
            append("?format=2")
            append("&sortProperty=pageviews")
            append("&sortDirection=desc")
            append("&page=")
            append(safePage)
            append("&per_page=")
            append(DEFAULT_PAGE_SIZE)
            append(
                "&relationships=language,type,format"
            )
        }
    }

    fun search(
        query: String,
        page: Int
    ): String {
        val safePage =
            page.coerceAtLeast(1)

        val encodedQuery =
            encodeQueryValue(
                query.trim()
            )

        return buildString {
            append(API_BASE_URL)
            append(STORIES_PATH)
            append("?format=2")
            append("&q=")
            append(encodedQuery)
            append("&sortProperty=pageviews")
            append("&sortDirection=desc")
            append("&page=")
            append(safePage)
            append("&per_page=")
            append(DEFAULT_PAGE_SIZE)
            append(
                "&relationships=language,type,format"
            )
        }
    }

    fun details(
        slug: String
    ): String {
        return buildString {
            append(API_BASE_URL)
            append(STORIES_PATH)
            append("?format=2")
            append("&slug=")
            append(
                encodeQueryValue(
                    normalizeSlug(slug)
                )
            )
            append("&per_page=1")
            append(
                "&relationships=" +
                        "language,type,format," +
                        "artists,status,genres"
            )
        }
    }

    fun chapters(
        slug: String
    ): String {
        return buildString {
            append(API_BASE_URL)
            append(STORIES_PATH)
            append("?format=2")
            append("&slug=")
            append(
                encodeQueryValue(
                    normalizeSlug(slug)
                )
            )
            append("&per_page=1")
            append(
                "&relationships=releases"
            )
        }
    }

    fun release(
        releaseId: String
    ): String {
        val safeReleaseId =
            releaseId.trim()

        return buildString {
            append(API_BASE_URL)
            append(RELEASES_PATH)
            append("/")
            append(
                encodePathValue(
                    safeReleaseId
                )
            )
            append(
                "?relationships=releaseImages"
            )
        }
    }

    fun mangaPage(
        slug: String
    ): String {
        return "$BASE_URL/comics/${
            normalizeSlug(slug)
        }"
    }

    fun image(
        imagePath: String
    ): String {
        val normalizedPath =
            imagePath.trim()

        if (normalizedPath.isBlank()) {
            return ""
        }

        if (
            normalizedPath.startsWith(
                prefix = "http://",
                ignoreCase = true
            ) ||
            normalizedPath.startsWith(
                prefix = "https://",
                ignoreCase = true
            )
        ) {
            return normalizedPath
        }

        return "$IMAGE_BASE_URL/${
            normalizedPath.trimStart('/')
        }"
    }

    fun normalizeSlug(
        mangaId: String
    ): String {
        val normalizedValue =
            mangaId.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return normalizedValue
            .substringAfter(
                delimiter = "/comics/",
                missingDelimiterValue =
                    normalizedValue
            )
            .substringBefore('?')
            .substringBefore('#')
            .trim('/')
    }

    private fun encodeQueryValue(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
                .toString()
        )
    }

    private fun encodePathValue(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
                .toString()
        )
            .replace(
                oldValue = "+",
                newValue = "%20"
            )
    }
}