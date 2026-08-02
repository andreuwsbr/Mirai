package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class AstralChapterParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): List<Chapter> {
        val document = Jsoup.parse(
            html,
            baseUrl
        )

        return document
            .select(
                AstralToonsSelectors
                    .CHAPTER_LINK
            )
            .mapNotNull { element ->
                parseChapter(
                    element = element,
                    manga = manga
                )
            }
            .distinctBy { chapter ->
                chapter.id
            }
            .sortedByDescending { chapter ->
                chapter.number
            }
    }

    private fun parseChapter(
        element: Element,
        manga: Manga
    ): Chapter? {
        val href = element
            .absUrl("href")
            .ifBlank {
                resolveUrl(
                    element.attr("href")
                )
            }

        if (href.isBlank()) {
            return null
        }

        val data = element
            .attr(
                AstralToonsSelectors
                    .CHAPTER_LOCK_ATTRIBUTE
            )
            .lowercase()

        if (
            data.contains(
                AstralToonsSelectors
                    .CHAPTER_LOCKED_VALUE
            )
        ) {
            return null
        }

        val visibleText =
            element.text().trim()

        val chapterNumber =
            extractChapterNumber(
                href = href,
                text = visibleText
            ) ?: return null

        val chapterName =
            extractChapterName(
                text = visibleText,
                number = chapterNumber
            )

        return Chapter(
            id = href,
            mangaId = manga.id,
            name = chapterName,
            number = chapterNumber,
            url = href,
            uploadedAt =
                extractUploadedAt(
                    visibleText
                )
        )
    }

    private fun extractChapterNumber(
        href: String,
        text: String
    ): Double? {
        val numberFromUrl =
            AstralToonsSelectors
                .CHAPTER_NUMBER_FROM_URL
                .find(href)
                ?.groupValues
                ?.getOrNull(1)
                ?.toDoubleOrNull()

        if (numberFromUrl != null) {
            return numberFromUrl
        }

        return AstralToonsSelectors
            .CHAPTER_NUMBER_FROM_TEXT
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun extractChapterName(
        text: String,
        number: Double
    ): String {
        val match =
            AstralToonsSelectors
                .CHAPTER_NAME
                .find(text)

        return match
            ?.value
            ?.replaceFirstChar {
                    character ->
                character.uppercase()
            }
            ?: "Capítulo ${
                formatNumber(number)
            }"
    }

    private fun extractUploadedAt(
        text: String
    ): String {
        AstralToonsSelectors
            .CHAPTER_DATE_PATTERNS
            .forEach { pattern ->
                val result =
                    pattern.find(text)

                if (result != null) {
                    return result.value
                }
            }

        return ""
    }

    private fun resolveUrl(
        value: String
    ): String {
        val normalizedValue =
            value.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return when {
            normalizedValue.startsWith(
                prefix = "https://",
                ignoreCase = true
            ) -> {
                normalizedValue
            }

            normalizedValue.startsWith(
                prefix = "http://",
                ignoreCase = true
            ) -> {
                normalizedValue
            }

            else -> {
                "$baseUrl/${
                    normalizedValue.trimStart('/')
                }"
            }
        }
    }

    private fun formatNumber(
        value: Double
    ): String {
        return if (
            value % 1.0 == 0.0
        ) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}