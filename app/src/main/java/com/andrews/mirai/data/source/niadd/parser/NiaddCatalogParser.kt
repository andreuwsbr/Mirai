package com.andrews.mirai.data.source.niadd.parser

import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import java.net.URI

class NiaddCatalogParser(
    private val baseUrl: String
) {

    fun parse(
        html: String
    ): List<Manga> {
        if (html.isBlank()) {
            return emptyList()
        }

        val document = Jsoup.parse(
            html,
            baseUrl
        )

        return document
            .select(
                "div.manga-list div.manga-item"
            )
            .mapNotNull { item ->
                val link = item.selectFirst(
                    "a.hover-underline[href]"
                ) ?: item.selectFirst(
                    "a[href*=/manga/]"
                ) ?: return@mapNotNull null

                val mangaUrl = resolveUrl(
                    link.attr("href")
                )

                if (mangaUrl.isBlank()) {
                    return@mapNotNull null
                }

                val title =
                    item.selectFirst(
                        "div.manga-name"
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
                        ?: return@mapNotNull null

                val coverUrl =
                    item.selectFirst(
                        "div.manga-img img"
                    )
                        ?.let { image ->
                            image.attr("src")
                                .ifBlank {
                                    image.attr(
                                        "data-src"
                                    )
                                }
                        }
                        ?.let(::resolveUrl)
                        ?.takeIf { url ->
                            url.isNotBlank()
                        }

                val description =
                    item.selectFirst(
                        "div.manga-part-inner-info"
                    )
                        ?.text()
                        ?.trim()
                        .orEmpty()

                Manga(
                    id = mangaUrl,
                    title = title,
                    description = description,
                    coverUrl = coverUrl,
                    type = detectType(
                        title = title,
                        text = item.text()
                    )
                )
            }
            .distinctBy { manga ->
                manga.id
            }
    }

    private fun detectType(
        title: String,
        text: String
    ): MangaType {
        val combinedText =
            "$title $text".lowercase()

        return when {
            combinedText.contains(
                "manhwa"
            ) -> {
                MangaType.MANHWA
            }

            combinedText.contains(
                "manhua"
            ) -> {
                MangaType.MANHUA
            }

            combinedText.contains(
                "mangá"
            ) ||
                    combinedText.contains(
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
}