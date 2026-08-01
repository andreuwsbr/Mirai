package com.andrews.mirai.data.source.astratoons.parser

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
            .select("a[href*='/capitulo/']")
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
            .attr("x-data")
            .lowercase()

        if (data.contains("islocked: true")) {
            return null
        }

        val visibleText = element
            .text()
            .trim()

        val chapterNumber = extractChapterNumber(
            href = href,
            text = visibleText
        ) ?: return null

        val chapterName = extractChapterName(
            text = visibleText,
            number = chapterNumber
        )

        return Chapter(
            id = href,
            mangaId = manga.id,
            name = chapterName,
            number = chapterNumber,
            url = href,
            uploadedAt = extractUploadedAt(
                visibleText
            )
        )
    }

    private fun extractChapterNumber(
        href: String,
        text: String
    ): Double? {
        val numberFromUrl = Regex(
            pattern = """/capitulo/(\d+(?:\.\d+)?)""",
            option = RegexOption.IGNORE_CASE
        )
            .find(href)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()

        if (numberFromUrl != null) {
            return numberFromUrl
        }

        return Regex(
            pattern = """cap[ií]tulo\s+(\d+(?:\.\d+)?)""",
            option = RegexOption.IGNORE_CASE
        )
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun extractChapterName(
        text: String,
        number: Double
    ): String {
        val match = Regex(
            pattern = """cap[ií]tulo\s+\d+(?:\.\d+)?""",
            option = RegexOption.IGNORE_CASE
        ).find(text)

        return match
            ?.value
            ?.replaceFirstChar { character ->
                character.uppercase()
            }
            ?: "Capítulo ${formatNumber(number)}"
    }

    private fun extractUploadedAt(
        text: String
    ): String {
        val patterns = listOf(
            """há\s+\d+\s+(?:minuto|minutos|hora|horas|dia|dias|semana|semanas|mês|meses|ano|anos)""",
            """ontem""",
            """hoje"""
        )

        patterns.forEach { pattern ->
            val result = Regex(
                pattern = pattern,
                option = RegexOption.IGNORE_CASE
            ).find(text)

            if (result != null) {
                return result.value
            }
        }

        return ""
    }

    private fun resolveUrl(
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
                "$baseUrl/${normalizedValue.trimStart('/')}"
            }
        }
    }

    private fun formatNumber(
        value: Double
    ): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}