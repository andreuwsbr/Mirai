package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class AstralDetailsParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): Manga {
        val document =
            Jsoup.parse(html, baseUrl)

        val visibleTexts = document
            .select("span")
            .map { element ->
                element.text().trim()
            }
            .filter { text ->
                text.isNotBlank()
            }

        val parsedAuthor =
            findLabeledValue(
                texts = visibleTexts,
                label = "Autor"
            )

        val parsedStatus =
            parseStatus(visibleTexts)

        val parsedType =
            parseType(visibleTexts)

        return manga.copy(
            title = findTitle(document)
                .ifBlank {
                    manga.title
                },
            author =
                if (
                    isValidInformation(
                        parsedAuthor
                    )
                ) {
                    parsedAuthor
                } else {
                    manga.author
                },
            status =
                if (
                    parsedStatus !=
                    MangaStatus.UNKNOWN
                ) {
                    parsedStatus
                } else {
                    manga.status
                },
            type =
                if (
                    parsedType !=
                    MangaType.UNKNOWN
                ) {
                    parsedType
                } else {
                    manga.type
                }
        )
    }

    private fun findTitle(
        document: Document
    ): String {
        return document
            .selectFirst("h1")
            ?.text()
            ?.trim()
            .orEmpty()
    }

    private fun findLabeledValue(
        texts: List<String>,
        label: String
    ): String {
        val prefix =
            "$label:"

        return texts
            .firstOrNull { text ->
                text.startsWith(
                    prefix = prefix,
                    ignoreCase = true
                )
            }
            ?.substringAfter(":")
            ?.trim()
            .orEmpty()
    }

    private fun parseStatus(
        texts: List<String>
    ): MangaStatus {
        val normalizedTexts =
            texts.map { text ->
                text.trim().lowercase()
            }

        return when {
            normalizedTexts.any { text ->
                text.contains("cancelad")
            } -> {
                MangaStatus.CANCELLED
            }

            normalizedTexts.any { text ->
                text.contains("hiato")
            } -> {
                MangaStatus.HIATUS
            }

            normalizedTexts.any { text ->
                text.contains("complet") ||
                        text.contains("conclu") ||
                        text.contains("finaliz")
            } -> {
                MangaStatus.COMPLETED
            }

            normalizedTexts.any { text ->
                text == "em dia" ||
                        text.contains(
                            "em andamento"
                        ) ||
                        text.contains(
                            "em lançamento"
                        ) ||
                        text.contains(
                            "em lancamento"
                        )
            } -> {
                MangaStatus.ONGOING
            }

            else -> {
                MangaStatus.UNKNOWN
            }
        }
    }

    private fun parseType(
        texts: List<String>
    ): MangaType {
        return when {
            texts.any { text ->
                text.equals(
                    other = "Manhua",
                    ignoreCase = true
                )
            } -> {
                MangaType.MANHUA
            }

            texts.any { text ->
                text.equals(
                    other = "Manhwa",
                    ignoreCase = true
                )
            } -> {
                MangaType.MANHWA
            }

            texts.any { text ->
                text.equals(
                    other = "Mangá",
                    ignoreCase = true
                ) ||
                        text.equals(
                            other = "Manga",
                            ignoreCase = true
                        )
            } -> {
                MangaType.MANGA
            }

            else -> {
                MangaType.UNKNOWN
            }
        }
    }

    private fun isValidInformation(
        value: String
    ): Boolean {
        val normalizedValue =
            value.trim().lowercase()

        return normalizedValue.isNotBlank() &&
                normalizedValue != "n/a" &&
                normalizedValue != "não informado" &&
                normalizedValue != "nao informado" &&
                normalizedValue != "-"
    }
}