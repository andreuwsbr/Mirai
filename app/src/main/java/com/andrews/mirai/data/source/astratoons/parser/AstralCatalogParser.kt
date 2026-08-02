package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class AstralCatalogParser(
    private val baseUrl: String
) {

    fun parse(
        html: String
    ): List<Manga> {
        val document = Jsoup.parse(
            html,
            baseUrl
        )

        return document
            .select(
                AstralToonsSelectors
                    .CATALOG_CARD
            )
            .mapNotNull(::parseCard)
            .distinctBy { manga ->
                manga.id
            }
    }

    private fun parseCard(
        card: Element
    ): Manga? {
        val relativeUrl = card
            .attr("href")
            .trim()

        if (
            relativeUrl.isBlank() ||
            relativeUrl.contains(
                other = "/capitulo/",
                ignoreCase = true
            )
        ) {
            return null
        }

        val title = card
            .selectFirst(
                AstralToonsSelectors
                    .CATALOG_TITLE
            )
            ?.text()
            ?.trim()
            .orEmpty()

        if (title.isBlank()) {
            return null
        }

        val fullUrl = card
            .absUrl("href")
            .ifBlank {
                "$baseUrl/${
                    relativeUrl.trimStart('/')
                }"
            }

        val coverUrl = card
            .selectFirst(
                AstralToonsSelectors
                    .CATALOG_COVER
            )
            ?.let { image ->
                image
                    .absUrl("src")
                    .ifBlank {
                        image
                            .attr("src")
                            .trim()
                    }
            }
            ?.takeIf { url ->
                url.isNotBlank()
            }

        val description = card
            .selectFirst(
                AstralToonsSelectors
                    .CATALOG_DESCRIPTION
            )
            ?.text()
            ?.trim()
            .orEmpty()

        val visibleTexts = card
            .select(
                AstralToonsSelectors
                    .CATALOG_METADATA
            )
            .map { span ->
                span.text().trim()
            }
            .filter { text ->
                text.isNotBlank()
            }

        return Manga(
            id = fullUrl,
            title = title,
            description = description,
            coverUrl = coverUrl,
            status =
                parseStatus(visibleTexts),
            type = MangaType.MANHWA,
            genres =
                parseGenres(visibleTexts)
        )
    }

    private fun parseStatus(
        texts: List<String>
    ): MangaStatus {
        return when {
            texts.any { text ->
                text.contains(
                    other = "cancelado",
                    ignoreCase = true
                )
            } -> {
                MangaStatus.CANCELLED
            }

            texts.any { text ->
                text.contains(
                    other = "completo",
                    ignoreCase = true
                )
            } -> {
                MangaStatus.COMPLETED
            }

            texts.any { text ->
                text.contains(
                    other = "hiato",
                    ignoreCase = true
                )
            } -> {
                MangaStatus.HIATUS
            }

            texts.any { text ->
                text.contains(
                    other = "andamento",
                    ignoreCase = true
                ) ||
                        text.contains(
                            other = "em dia",
                            ignoreCase = true
                        )
            } -> {
                MangaStatus.ONGOING
            }

            else -> {
                MangaStatus.UNKNOWN
            }
        }
    }

    private fun parseGenres(
        texts: List<String>
    ): List<String> {
        val ignoredTexts = listOf(
            "completo",
            "cancelado",
            "hiato",
            "em andamento",
            "em dia"
        )

        return texts
            .filterNot { text ->
                ignoredTexts.any { ignored ->
                    text.equals(
                        other = ignored,
                        ignoreCase = true
                    )
                }
            }
            .distinct()
    }
}