package com.andrews.mirai.data.source.mangalivre.parser

import com.andrews.mirai.data.source.mangalivre.MangaLivreSelectors
import com.andrews.mirai.domain.model.ReaderPage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI

class PageParser(
    private val baseUrl: String
) {

    fun parse(
        html: String
    ): List<ReaderPage> {
        val document =
            Jsoup.parse(html, baseUrl)

        val pagesByUrl =
            linkedMapOf<String, ReaderPage>()

        document
            .select(
                MangaLivreSelectors
                    .PAGE_CONTAINER
            )
            .forEachIndexed {
                    position,
                    container ->

                val page = parsePage(
                    container = container,
                    fallbackIndex = position
                ) ?: return@forEachIndexed

                pagesByUrl[page.imageUrl] =
                    page
            }

        return pagesByUrl
            .values
            .sortedBy { page ->
                page.index
            }
    }

    private fun parsePage(
        container: Element,
        fallbackIndex: Int
    ): ReaderPage? {
        val image = container.selectFirst(
            MangaLivreSelectors
                .PAGE_PRIMARY_IMAGE
        ) ?: container.selectFirst(
            MangaLivreSelectors
                .PAGE_FALLBACK_IMAGE
        ) ?: return null

        val imageUrl =
            extractBestImageUrl(image)
                ?: return null

        val pageNumber = container
            .attr(
                MangaLivreSelectors
                    .PAGE_NUMBER_ATTRIBUTE
            )
            .toIntOrNull()
            ?: container
                .id()
                .removePrefix(
                    MangaLivreSelectors
                        .PAGE_ID_PREFIX
                )
                .toIntOrNull()
            ?: fallbackIndex + 1

        return ReaderPage(
            index = pageNumber - 1,
            imageUrl = imageUrl
        )
    }

    private fun extractBestImageUrl(
        image: Element
    ): String? {
        MangaLivreSelectors
            .PAGE_DIRECT_IMAGE_ATTRIBUTES
            .forEach { attribute ->
                val rawUrl =
                    image
                        .attr(attribute)
                        .trim()

                if (rawUrl.isNotBlank()) {
                    return resolveUrl(
                        image = image,
                        rawUrl = rawUrl
                    )
                }
            }

        MangaLivreSelectors
            .PAGE_SRCSET_ATTRIBUTES
            .forEach { attribute ->
                val srcSet = image
                    .attr(attribute)
                    .trim()

                val largestUrl =
                    extractLargestUrlFromSrcSet(
                        srcSet
                    )

                if (!largestUrl.isNullOrBlank()) {
                    return resolveUrl(
                        image = image,
                        rawUrl = largestUrl
                    )
                }
            }

        MangaLivreSelectors
            .PAGE_LAZY_IMAGE_ATTRIBUTES
            .forEach { attribute ->
                val rawUrl = image
                    .attr(attribute)
                    .trim()

                if (rawUrl.isNotBlank()) {
                    return resolveUrl(
                        image = image,
                        rawUrl = rawUrl
                    )
                }
            }

        return null
    }

    private fun extractLargestUrlFromSrcSet(
        srcSet: String
    ): String? {
        if (srcSet.isBlank()) {
            return null
        }

        return srcSet
            .split(",")
            .mapNotNull { candidate ->
                val parts = candidate
                    .trim()
                    .split(
                        Regex("\\s+")
                    )

                val url = parts
                    .firstOrNull()
                    ?.trim()
                    ?.takeIf { value ->
                        value.isNotBlank()
                    }
                    ?: return@mapNotNull null

                val size = parts
                    .getOrNull(1)
                    ?.removeSuffix("w")
                    ?.removeSuffix("x")
                    ?.toDoubleOrNull()
                    ?: 1.0

                ImageCandidate(
                    url = url,
                    size = size
                )
            }
            .maxByOrNull { candidate ->
                candidate.size
            }
            ?.url
    }

    private fun resolveUrl(
        image: Element,
        rawUrl: String
    ): String {
        val cleanUrl = rawUrl
            .trim()
            .removePrefix("\"")
            .removeSuffix("\"")

        return runCatching {
            val pageBaseUrl = image
                .baseUri()
                .ifBlank {
                    baseUrl
                }

            URI(pageBaseUrl)
                .resolve(cleanUrl)
                .toString()
        }.getOrDefault(
            cleanUrl
        )
    }

    private data class ImageCandidate(
        val url: String,
        val size: Double
    )
}