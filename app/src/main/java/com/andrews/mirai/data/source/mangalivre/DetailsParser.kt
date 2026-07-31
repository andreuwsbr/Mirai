package com.andrews.mirai.data.source.mangalivre

import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class DetailsParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): Manga {
        val document = Jsoup.parse(html, baseUrl)

        return manga.copy(
            title = findTitle(document).ifBlank {
                manga.title
            },
            description = findDescription(document).ifBlank {
                manga.description
            },
            coverUrl = findCover(document) ?: manga.coverUrl,
            author = findAuthor(document).ifBlank {
                manga.author
            },
            genres = findGenres(document).ifEmpty {
                manga.genres
            }
        )
    }

    private fun findTitle(document: Document): String {
        val selectors = listOf(
            "h1",
            ".post-title h1",
            ".manga-title h1",
            ".manga-title",
            ".entry-title",
            ".post-title"
        )

        return findFirstText(
            document = document,
            selectors = selectors
        )
    }

    private fun findDescription(document: Document): String {
        val selectors = listOf(
            ".description-summary .summary__content",
            ".summary__content",
            ".manga-excerpt",
            ".description",
            ".sinopse",
            ".summary",
            ".entry-content p"
        )

        selectors.forEach { selector ->
            val element = document.selectFirst(selector)

            val text = element
                ?.text()
                ?.trim()
                .orEmpty()

            if (
                text.isNotBlank() &&
                !text.equals("Sinopse", ignoreCase = true)
            ) {
                return text
            }
        }

        return ""
    }

    private fun findCover(document: Document): String? {
        val selectors = listOf(
            ".summary_image img",
            ".manga-thumb img",
            ".manga-cover img",
            ".post-content_item img",
            ".tab-summary img",
            "article img"
        )

        selectors.forEach { selector ->
            val image = document.selectFirst(selector)
                ?: return@forEach

            val coverUrl = extractImageUrl(image)

            if (!coverUrl.isNullOrBlank()) {
                return coverUrl
            }
        }

        return null
    }

    private fun findAuthor(document: Document): String {
        val selectors = listOf(
            ".author-content",
            ".post-content_item:has(.summary-heading:matchesOwn((?i)autor)) .summary-content",
            ".post-content_item:has(h5:matchesOwn((?i)autor)) .summary-content",
            ".manga-author",
            "[class*=author]"
        )

        selectors.forEach { selector ->
            val text = document
                .selectFirst(selector)
                ?.text()
                ?.trim()
                .orEmpty()

            val cleanedText = cleanLabel(
                text = text,
                labels = listOf("Autor", "Author")
            )

            if (cleanedText.isNotBlank()) {
                return cleanedText
            }
        }

        return ""
    }

    private fun findGenres(document: Document): List<String> {
        val genres = linkedSetOf<String>()

        val selectors = listOf(
            ".genres-content a",
            ".genres a",
            ".manga-genres a",
            "a[href*=genero]",
            "a[href*=genre]"
        )

        selectors.forEach { selector ->
            document.select(selector).forEach { element ->
                val genre = element.text().trim()

                if (
                    genre.isNotBlank() &&
                    !genre.equals("Gêneros", ignoreCase = true) &&
                    !genre.equals("Genres", ignoreCase = true)
                ) {
                    genres += genre
                }
            }
        }

        return genres.toList()
    }

    private fun extractImageUrl(image: Element): String? {
        return image.absUrl("data-src")
            .ifBlank { image.absUrl("data-lazy-src") }
            .ifBlank { image.absUrl("data-original") }
            .ifBlank { image.absUrl("src") }
            .ifBlank { null }
    }

    private fun findFirstText(
        document: Document,
        selectors: List<String>
    ): String {
        selectors.forEach { selector ->
            val text = document
                .selectFirst(selector)
                ?.text()
                ?.trim()
                .orEmpty()

            if (text.isNotBlank()) {
                return text
            }
        }

        return ""
    }

    private fun cleanLabel(
        text: String,
        labels: List<String>
    ): String {
        var result = text.trim()

        labels.forEach { label ->
            result = result
                .removePrefix("$label:")
                .removePrefix(label)
                .trim()
        }

        return result
    }
}