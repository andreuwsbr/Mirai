package com.andrews.mirai.data.source.mangalivre

import com.andrews.mirai.domain.model.ReaderPage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI

class PageParser(
    private val baseUrl: String
) {

    fun parse(html: String): List<ReaderPage> {
        val document = Jsoup.parse(html, baseUrl)
        val pagesByUrl = linkedMapOf<String, ReaderPage>()

        document
            .select(".chapter-image-container")
            .forEachIndexed { position, container ->
                val page = parsePage(
                    container = container,
                    fallbackIndex = position
                ) ?: return@forEachIndexed

                pagesByUrl[page.imageUrl] = page
            }

        return pagesByUrl.values
            .sortedBy { page -> page.index }
    }

    private fun parsePage(
        container: Element,
        fallbackIndex: Int
    ): ReaderPage? {
        val image = container.selectFirst("img.chapter-image")
            ?: container.selectFirst("img")
            ?: return null

        val imageUrl = extractBestImageUrl(image)
            ?: return null

        val pageNumber = container
            .attr("data-page")
            .toIntOrNull()
            ?: container
                .id()
                .removePrefix("page-")
                .toIntOrNull()
            ?: fallbackIndex + 1

        return ReaderPage(
            index = pageNumber - 1,
            imageUrl = imageUrl
        )
    }

    private fun extractBestImageUrl(image: Element): String? {
        val directAttributes = listOf(
            "data-full",
            "data-full-url",
            "data-original",
            "data-original-src",
            "data-high-res-src",
            "data-hires",
            "data-src"
        )

        directAttributes.forEach { attribute ->
            val rawUrl = image.attr(attribute).trim()

            if (rawUrl.isNotBlank()) {
                return resolveUrl(
                    image = image,
                    rawUrl = rawUrl
                )
            }
        }

        val srcSetAttributes = listOf(
            "data-srcset",
            "data-lazy-srcset",
            "srcset"
        )

        srcSetAttributes.forEach { attribute ->
            val srcSet = image.attr(attribute).trim()

            val largestUrl = extractLargestUrlFromSrcSet(srcSet)

            if (!largestUrl.isNullOrBlank()) {
                return resolveUrl(
                    image = image,
                    rawUrl = largestUrl
                )
            }
        }

        val lazyAttributes = listOf(
            "data-lazy-src",
            "data-url",
            "src"
        )

        lazyAttributes.forEach { attribute ->
            val rawUrl = image.attr(attribute).trim()

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
                    .split(Regex("\\s+"))

                val url = parts.firstOrNull()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
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
            val pageBaseUrl = image.baseUri()
                .ifBlank { baseUrl }

            URI(pageBaseUrl)
                .resolve(cleanUrl)
                .toString()
        }.getOrDefault(cleanUrl)
    }

    private data class ImageCandidate(
        val url: String,
        val size: Double
    )
}