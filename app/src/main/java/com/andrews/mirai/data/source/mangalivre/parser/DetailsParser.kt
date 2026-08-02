package com.andrews.mirai.data.source.mangalivre.parser

import com.andrews.mirai.data.source.mangalivre.MangaLivreSelectors
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
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
        val document =
            Jsoup.parse(html, baseUrl)

        val parsedStatus =
            findStatus(document)

        return manga.copy(
            title = findTitle(document)
                .ifBlank {
                    manga.title
                },
            description =
                findDescription(document)
                    .ifBlank {
                        manga.description
                    },
            coverUrl =
                findCover(document)
                    ?: manga.coverUrl,
            author = findAuthor(document)
                .ifBlank {
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
            type = MangaType.UNKNOWN,
            genres = findGenres(document)
                .ifEmpty {
                    manga.genres
                }
        )
    }

    private fun findTitle(
        document: Document
    ): String {
        return findFirstText(
            document = document,
            selectors =
                MangaLivreSelectors
                    .DETAILS_TITLE
        )
    }

    private fun findDescription(
        document: Document
    ): String {
        MangaLivreSelectors
            .DETAILS_DESCRIPTION
            .forEach { selector ->
                val text = document
                    .selectFirst(selector)
                    ?.text()
                    ?.trim()
                    .orEmpty()

                if (
                    text.isNotBlank() &&
                    !text.equals(
                        other = "Sinopse",
                        ignoreCase = true
                    )
                ) {
                    return text
                }
            }

        return ""
    }

    private fun findCover(
        document: Document
    ): String? {
        MangaLivreSelectors
            .DETAILS_COVER
            .forEach { selector ->
                val image =
                    document.selectFirst(
                        selector
                    ) ?: return@forEach

                val coverUrl =
                    extractImageUrl(image)

                if (!coverUrl.isNullOrBlank()) {
                    return coverUrl
                }
            }

        return null
    }

    private fun findAuthor(
        document: Document
    ): String {
        val metaAuthor = findMetaValue(
            document = document,
            labels = listOf(
                "Autor",
                "Author"
            )
        )

        if (isValidInformation(metaAuthor)) {
            return metaAuthor
        }

        MangaLivreSelectors
            .DETAILS_AUTHOR_FALLBACK
            .forEach { selector ->
                val text = document
                    .selectFirst(selector)
                    ?.text()
                    ?.trim()
                    .orEmpty()

                val cleanedText = cleanLabel(
                    text = text,
                    labels = listOf(
                        "Autor",
                        "Author"
                    )
                )

                if (
                    isValidInformation(
                        cleanedText
                    )
                ) {
                    return cleanedText
                }
            }

        return ""
    }

    private fun findStatus(
        document: Document
    ): MangaStatus {
        val statusText = findMetaValue(
            document = document,
            labels = listOf(
                "Status",
                "Situação"
            )
        )

        return parseStatus(statusText)
    }

    private fun parseStatus(
        value: String
    ): MangaStatus {
        val normalizedValue =
            value.trim().lowercase()

        return when {
            normalizedValue.contains(
                "cancelad"
            ) -> {
                MangaStatus.CANCELLED
            }

            normalizedValue.contains(
                "hiato"
            ) -> {
                MangaStatus.HIATUS
            }

            normalizedValue.contains(
                "complet"
            ) ||
                    normalizedValue.contains(
                        "conclu"
                    ) ||
                    normalizedValue.contains(
                        "finaliz"
                    ) -> {
                MangaStatus.COMPLETED
            }

            normalizedValue.contains(
                "lançamento"
            ) ||
                    normalizedValue.contains(
                        "lancamento"
                    ) ||
                    normalizedValue.contains(
                        "andamento"
                    ) ||
                    normalizedValue.contains(
                        "em dia"
                    ) ||
                    normalizedValue.contains(
                        "ongoing"
                    ) -> {
                MangaStatus.ONGOING
            }

            else -> {
                MangaStatus.UNKNOWN
            }
        }
    }

    private fun findMetaValue(
        document: Document,
        labels: List<String>
    ): String {
        document
            .select(
                MangaLivreSelectors
                    .DETAILS_META_ITEM
            )
            .forEach { item ->
                val label = item
                    .selectFirst(
                        MangaLivreSelectors
                            .DETAILS_META_LABEL
                    )
                    ?.text()
                    ?.trim()
                    ?.trimEnd(':')
                    .orEmpty()

                val isRequestedLabel =
                    labels.any { expected ->
                        label.equals(
                            other = expected,
                            ignoreCase = true
                        )
                    }

                if (isRequestedLabel) {
                    return item
                        .selectFirst(
                            MangaLivreSelectors
                                .DETAILS_META_VALUE
                        )
                        ?.text()
                        ?.trim()
                        .orEmpty()
                }
            }

        return ""
    }

    private fun findGenres(
        document: Document
    ): List<String> {
        val genres =
            linkedSetOf<String>()

        MangaLivreSelectors
            .DETAILS_GENRES
            .forEach { selector ->
                document
                    .select(selector)
                    .forEach { element ->
                        val genre =
                            element
                                .text()
                                .trim()

                        if (
                            genre.isNotBlank() &&
                            !genre.equals(
                                other = "Gêneros",
                                ignoreCase = true
                            ) &&
                            !genre.equals(
                                other = "Genres",
                                ignoreCase = true
                            )
                        ) {
                            genres += genre
                        }
                    }
            }

        return genres.toList()
    }

    private fun extractImageUrl(
        image: Element
    ): String? {
        MangaLivreSelectors
            .DETAILS_IMAGE_ATTRIBUTES
            .forEach { attribute ->
                val imageUrl =
                    image.absUrl(attribute)

                if (imageUrl.isNotBlank()) {
                    return imageUrl
                }
            }

        return null
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
        var result =
            text.trim()

        labels.forEach { label ->
            result = result
                .removePrefix("$label:")
                .removePrefix(label)
                .trim()
        }

        return result
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