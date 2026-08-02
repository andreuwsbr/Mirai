package com.andrews.mirai.data.source.niadd.parser

import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class NiaddDetailsParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): Manga {
        if (html.isBlank()) {
            return manga
        }

        val document = Jsoup.parse(
            html,
            baseUrl
        )

        val title =
            document.selectFirst(
                "h1.book-headline-name"
            )
                ?.text()
                ?.trim()
                ?.ifBlank {
                    null
                }
                ?: manga.title

        val description =
            extractDescription(
                document = document,
                manga = manga
            )

        val coverUrl =
            extractCoverUrl(document)
                ?: manga.coverUrl

        val author =
            extractInformation(
                document = document,
                labels = listOf(
                    "Autor (es):",
                    "Autor(es):",
                    "Autor:"
                )
            )
                ?.ifBlank {
                    null
                }
                ?: manga.author

        val statusText =
            document.selectFirst(
                "span.book-status"
            )
                ?.text()
                ?.trim()
                .orEmpty()

        val parsedStatus =
            parseStatus(statusText)

        val genres =
            extractGenres(document)
                .ifEmpty {
                    manga.genres
                }

        return manga.copy(
            title = title,
            description = description,
            coverUrl = coverUrl,
            author = author,
            status = if (
                parsedStatus != MangaStatus.UNKNOWN
            ) {
                parsedStatus
            } else {
                manga.status
            },
            type = detectType(
                currentType = manga.type,
                documentText = document.text()
            ),
            genres = genres
        )
    }

    private fun extractDescription(
        document: Document,
        manga: Manga
    ): String {
        val synopsisTitle =
            document.select(
                ".detail-cate-title"
            ).firstOrNull { element ->
                element.text()
                    .trim()
                    .equals(
                        other = "Sinopse",
                        ignoreCase = true
                    )
            }

        val descriptionAfterTitle =
            synopsisTitle
                ?.nextElementSibling()
                ?.text()
                ?.trim()
                ?.ifBlank {
                    null
                }

        if (descriptionAfterTitle != null) {
            return descriptionAfterTitle
        }

        val descriptionByClass =
            document.selectFirst(
                "section.detail-section.detail-synopsis"
            )
                ?.text()
                ?.trim()
                ?.ifBlank {
                    null
                }

        if (descriptionByClass != null) {
            return descriptionByClass
        }

        /*
         * O catálogo da Niadd pode colocar os gêneros no campo
         * de descrição. Só usamos a descrição anterior quando
         * ela parece realmente ser uma sinopse.
         */
        val previousDescription =
            manga.description.trim()

        return if (
            previousDescription.length >=
            MINIMUM_FALLBACK_DESCRIPTION_LENGTH
        ) {
            previousDescription
        } else {
            ""
        }
    }

    private fun extractCoverUrl(
        document: Document
    ): String? {
        val coverElement =
            document.selectFirst(
                "div.bookside-img"
            ) ?: return null

        val style =
            coverElement.attr(
                "style"
            )

        val styleUrl =
            BACKGROUND_URL_REGEX
                .find(style)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()

        if (!styleUrl.isNullOrBlank()) {
            return resolveUrl(styleUrl)
        }

        val imageUrl =
            coverElement
                .selectFirst("img")
                ?.let { image ->
                    image.attr("src")
                        .ifBlank {
                            image.attr(
                                "data-src"
                            )
                        }
                }
                ?.trim()

        return imageUrl
            ?.takeIf { value ->
                value.isNotBlank()
            }
            ?.let(::resolveUrl)
    }

    private fun extractInformation(
        document: Document,
        labels: List<String>
    ): String? {
        val candidates =
            document.select(
                "div, li, p"
            )
                .filter { element ->
                    val text =
                        element.text().trim()

                    labels.any { label ->
                        text.startsWith(
                            prefix = label,
                            ignoreCase = true
                        )
                    }
                }
                .sortedBy { element ->
                    element.text().length
                }

        val element =
            candidates.firstOrNull()
                ?: return null

        val linksText =
            element.select("a")
                .joinToString(
                    separator = ", "
                ) { link ->
                    link.text().trim()
                }
                .trim()

        if (linksText.isNotBlank()) {
            return linksText
        }

        var text =
            element.text().trim()

        labels.forEach { label ->
            if (
                text.startsWith(
                    prefix = label,
                    ignoreCase = true
                )
            ) {
                text =
                    text.substring(
                        label.length
                    ).trim()
            }
        }

        return text.takeIf { value ->
            value.isNotBlank()
        }
    }

    private fun extractGenres(
        document: Document
    ): List<String> {
        val genreSection =
            document.select(
                "div, section, li"
            )
                .filter { element ->
                    val text =
                        element.text().trim()

                    text.startsWith(
                        prefix = "Gêneros:",
                        ignoreCase = true
                    ) ||
                            text.startsWith(
                                prefix = "Generos:",
                                ignoreCase = true
                            )
                }
                .minByOrNull { element ->
                    element.text().length
                }
                ?: return emptyList()

        val genresFromLinks =
            genreSection.select("a")
                .map { link ->
                    link.text().trim()
                }
                .filter { genre ->
                    genre.isNotBlank()
                }
                .distinct()

        if (genresFromLinks.isNotEmpty()) {
            return genresFromLinks
        }

        return genreSection
            .text()
            .substringAfter(
                delimiter = ":",
                missingDelimiterValue = ""
            )
            .split(",")
            .map { genre ->
                genre.trim()
            }
            .filter { genre ->
                genre.isNotBlank()
            }
            .distinct()
    }

    private fun parseStatus(
        value: String
    ): MangaStatus {
        val normalizedValue =
            value.lowercase()

        return when {
            normalizedValue.contains(
                "concluído"
            ) ||
                    normalizedValue.contains(
                        "concluido"
                    ) ||
                    normalizedValue.contains(
                        "completo"
                    ) -> {
                MangaStatus.COMPLETED
            }

            normalizedValue.contains(
                "andamento"
            ) ||
                    normalizedValue.contains(
                        "progresso"
                    ) -> {
                MangaStatus.ONGOING
            }

            normalizedValue.contains(
                "hiato"
            ) -> {
                MangaStatus.HIATUS
            }

            normalizedValue.contains(
                "cancelado"
            ) -> {
                MangaStatus.CANCELLED
            }

            else -> {
                MangaStatus.UNKNOWN
            }
        }
    }

    private fun detectType(
        currentType: MangaType,
        documentText: String
    ): MangaType {
        if (
            currentType != MangaType.UNKNOWN
        ) {
            return currentType
        }

        val normalizedText =
            documentText.lowercase()

        return when {
            normalizedText.contains(
                "manhwa"
            ) -> {
                MangaType.MANHWA
            }

            normalizedText.contains(
                "manhua"
            ) -> {
                MangaType.MANHUA
            }

            normalizedText.contains(
                "mangá"
            ) ||
                    normalizedText.contains(
                        "manga"
                    ) -> {
                MangaType.MANGA
            }

            else -> {
                MangaType.UNKNOWN
            }
        }
    }

    private fun resolveUrl(
        value: String
    ): String {
        return runCatching {
            URI(baseUrl)
                .resolve(value.trim())
                .toString()
        }.getOrDefault(
            value.trim()
        )
    }

    private companion object {
        const val MINIMUM_FALLBACK_DESCRIPTION_LENGTH =
            80

        val BACKGROUND_URL_REGEX =
            Regex(
                pattern =
                    """background-image\s*:\s*url\(['"]?([^'")]+)""",
                option =
                    RegexOption.IGNORE_CASE
            )
    }
}