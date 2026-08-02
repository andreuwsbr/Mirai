package com.andrews.mirai.data.source.niadd.parser

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup
import java.net.URI

class NiaddChapterParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): List<Chapter> {
        if (html.isBlank()) {
            return emptyList()
        }

        val document = Jsoup.parse(
            html,
            baseUrl
        )

        return document
            .select(
                "ul.chapter-list a.hover-underline[href], " +
                        "ul.chapter-list a[href]"
            )
            .mapNotNull { link ->
                val chapterUrl =
                    resolveUrl(
                        link.attr("href")
                    )

                if (chapterUrl.isBlank()) {
                    return@mapNotNull null
                }

                val chapterName =
                    link.selectFirst(
                        "span.chp-title"
                    )
                        ?.text()
                        ?.trim()
                        ?.ifBlank {
                            null
                        }
                        ?: link.attr("title")
                            .trim()
                            .ifBlank {
                                null
                            }
                        ?: link.text()
                            .trim()
                            .ifBlank {
                                null
                            }
                        ?: return@mapNotNull null

                val uploadedAt =
                    link.selectFirst(
                        ".chp-right"
                    )
                        ?.text()
                        ?.trim()
                        .orEmpty()

                Chapter(
                    id = chapterUrl,
                    mangaId = manga.id,
                    name = chapterName,
                    number = extractChapterNumber(
                        chapterName
                    ),
                    url = chapterUrl,
                    uploadedAt = uploadedAt
                )
            }
            .distinctBy { chapter ->
                chapter.id
            }
    }

    private fun extractChapterNumber(
        chapterName: String
    ): Double {
        val normalizedName =
            chapterName
                .replace(
                    Regex(
                        pattern =
                            """(?i)one[\s-]?shot"""
                    ),
                    " 0 "
                )

        val numberText =
            NUMBER_REGEX
                .findAll(normalizedName)
                .lastOrNull()
                ?.value
                ?: return 0.0

        val numberParts =
            numberText
                .split(
                    ".",
                    "-",
                    "_"
                )
                .filter { part ->
                    part.isNotBlank()
                }

        if (numberParts.isEmpty()) {
            return 0.0
        }

        val wholeNumber =
            numberParts.first()
                .toDoubleOrNull()
                ?: return 0.0

        if (numberParts.size == 1) {
            return wholeNumber
        }

        val decimalPart =
            numberParts
                .drop(1)
                .joinToString(
                    separator = ""
                )
                .take(6)

        return "${
            wholeNumber.toLong()
        }.$decimalPart"
            .toDoubleOrNull()
            ?: wholeNumber
    }

    private fun resolveUrl(
        value: String
    ): String {
        val normalizedValue =
            value.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return runCatching {
            URI(baseUrl)
                .resolve(normalizedValue)
                .toString()
        }.getOrDefault(
            normalizedValue
        )
    }

    private companion object {
        val NUMBER_REGEX =
            Regex(
                pattern =
                    """\d+(?:[._-]\d+)*"""
            )
    }
}