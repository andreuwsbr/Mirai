package com.andrews.mirai.data.source.niadd.parser

import org.jsoup.Jsoup
import java.net.URI

class NiaddPageParser(
    private val baseUrl: String
) {

    fun parseImageUrls(
        html: String
    ): List<String> {
        if (html.isBlank()) {
            return emptyList()
        }

        val document = Jsoup.parse(
            html,
            baseUrl
        )

        return document
            .select(
                "img.manga_pic"
            )
            .mapNotNull { image ->
                val imageUrl =
                    image.attr("src")
                        .ifBlank {
                            image.attr(
                                "data-src"
                            )
                        }
                        .trim()

                if (imageUrl.isBlank()) {
                    null
                } else {
                    resolveUrl(imageUrl)
                }
            }
            .filter { imageUrl ->
                imageUrl.isNotBlank() &&
                        !imageUrl.startsWith(
                            prefix = "data:",
                            ignoreCase = true
                        )
            }
            .distinct()
    }

    fun extractTotalPages(
        html: String
    ): Int {
        if (html.isBlank()) {
            return 0
        }

        val document = Jsoup.parse(
            html,
            baseUrl
        )

        val pageOptions =
            document.select(
                "select.sl-page option"
            )

        if (pageOptions.isNotEmpty()) {
            val largestPageNumber =
                pageOptions
                    .mapNotNull { option ->
                        PAGE_NUMBER_REGEX
                            .find(
                                option.text()
                            )
                            ?.groupValues
                            ?.getOrNull(1)
                            ?.toIntOrNull()
                    }
                    .maxOrNull()

            if (
                largestPageNumber != null &&
                largestPageNumber > 0
            ) {
                return largestPageNumber
            }

            return pageOptions.size
        }

        val pageCountMatch =
            TOTAL_PAGES_REGEX.find(
                document.text()
            )

        return pageCountMatch
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
    }

    private fun resolveUrl(
        value: String
    ): String {
        return runCatching {
            URI(baseUrl)
                .resolve(value.trim())
                .toString()
        }.getOrDefault(
            value.trim()
        )
    }

    private companion object {
        val PAGE_NUMBER_REGEX =
            Regex(
                pattern =
                    """(\d+)\s*/\s*\d+"""
            )

        val TOTAL_PAGES_REGEX =
            Regex(
                pattern =
                    """\d+\s+of\s+(\d+)""",
                option =
                    RegexOption.IGNORE_CASE
            )
    }
}