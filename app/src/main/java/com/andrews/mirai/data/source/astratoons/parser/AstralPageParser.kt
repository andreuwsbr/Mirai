package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors
import com.andrews.mirai.domain.model.ReaderPage
import org.jsoup.Jsoup
import java.net.URI

class AstralPageParser(
    private val baseUrl: String
) {

    fun parse(
        html: String
    ): List<ReaderPage> {
        val document = Jsoup.parse(
            html,
            baseUrl
        )

        val imageUrls =
            linkedSetOf<String>()

        document
            .select(
                AstralToonsSelectors
                    .PAGE_IMAGE
            )
            .forEach { image ->
                val rawUrl =
                    AstralToonsSelectors
                        .PAGE_IMAGE_ATTRIBUTES
                        .asSequence()
                        .map { attribute ->
                            image
                                .attr(attribute)
                                .trim()
                        }
                        .firstOrNull { value ->
                            value.contains(
                                other =
                                    AstralToonsSelectors
                                        .PAGE_STORAGE_PATH,
                                ignoreCase = true
                            )
                        }

                if (!rawUrl.isNullOrBlank()) {
                    resolveUrl(rawUrl)
                        ?.let(imageUrls::add)
                }
            }

        extractUrlsFromHtml(html)
            .forEach(imageUrls::add)

        return imageUrls
            .sortedWith(
                compareBy(
                    ::extractPageNumber
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

    private fun extractUrlsFromHtml(
        html: String
    ): List<String> {
        return AstralToonsSelectors
            .PAGE_URL
            .findAll(html)
            .map { match ->
                match.value
                    .replace("\\/", "/")
                    .replace(
                        "\\u0026",
                        "&"
                    )
            }
            .distinct()
            .toList()
    }

    private fun resolveUrl(
        value: String
    ): String? {
        val normalizedValue = value
            .trim()
            .replace("\\/", "/")
            .replace(
                "\\u0026",
                "&"
            )

        if (
            normalizedValue.isBlank() ||
            normalizedValue.startsWith(
                prefix = "data:",
                ignoreCase = true
            )
        ) {
            return null
        }

        return runCatching {
            URI(baseUrl)
                .resolve(normalizedValue)
                .toString()
        }.getOrNull()
    }

    private fun extractPageNumber(
        imageUrl: String
    ): Int {
        val fileName = imageUrl
            .substringBefore("?")
            .substringAfterLast("/")

        return AstralToonsSelectors
            .PAGE_NUMBER
            .find(fileName)
            ?.value
            ?.toIntOrNull()
            ?: Int.MAX_VALUE
    }
}