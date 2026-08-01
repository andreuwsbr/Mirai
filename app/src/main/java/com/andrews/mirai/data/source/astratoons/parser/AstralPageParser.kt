package com.andrews.mirai.data.source.astratoons.parser

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

        val imageUrls = linkedSetOf<String>()

        document
            .select(
                "img[src*='/storage/chapters/'], " +
                        "img[data-src*='/storage/chapters/'], " +
                        "img[data-lazy-src*='/storage/chapters/']"
            )
            .forEach { image ->
                val rawUrl = sequenceOf(
                    image.attr("src"),
                    image.attr("data-src"),
                    image.attr("data-lazy-src")
                )
                    .map(String::trim)
                    .firstOrNull { value ->
                        value.contains(
                            "/storage/chapters/",
                            ignoreCase = true
                        )
                    }

                if (!rawUrl.isNullOrBlank()) {
                    resolveUrl(rawUrl)?.let(imageUrls::add)
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
            .mapIndexed { index, imageUrl ->
                ReaderPage(
                    index = index,
                    imageUrl = imageUrl
                )
            }
    }

    private fun extractUrlsFromHtml(
        html: String
    ): List<String> {
        val regex = Regex(
            pattern =
                """https?://[^"'\\\s<>]+/storage/chapters/[^"'\\\s<>]+?\.(?:jpg|jpeg|png|webp|avif)(?:\?[^"'\\\s<>]*)?""",
            option = RegexOption.IGNORE_CASE
        )

        return regex
            .findAll(html)
            .map { match ->
                match.value
                    .replace("\\/", "/")
                    .replace("\\u0026", "&")
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
            .replace("\\u0026", "&")

        if (
            normalizedValue.isBlank() ||
            normalizedValue.startsWith("data:")
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

        return Regex("""\d+""")
            .find(fileName)
            ?.value
            ?.toIntOrNull()
            ?: Int.MAX_VALUE
    }
}