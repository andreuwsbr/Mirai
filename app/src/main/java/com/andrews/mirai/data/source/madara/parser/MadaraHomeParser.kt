package com.andrews.mirai.data.source.madara.parser

import com.andrews.mirai.data.source.madara.MadaraSourceConfig
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class MadaraHomeParser(

    private val config: MadaraSourceConfig

) {

    fun parse(
        html: String
    ): List<Manga> {

        val document =
            Jsoup.parse(
                html,
                config.baseUrl
            )

        val mangas =
            linkedMapOf<String, Manga>()

        document
            .select("a[href]")
            .forEach { link ->

                val manga =
                    parseLink(link)
                        ?: return@forEach

                mangas[manga.id] = manga
            }

        return mangas.values.toList()
    }

    private fun parseLink(
        link: Element
    ): Manga? {

        val absoluteUrl =
            link.absUrl("href")
                .substringBefore("#")
                .substringBefore("?")
                .trimEnd('/')

        if (
            config.mangaPaths.none {
                absoluteUrl.contains(it)
            }
        ) {
            return null
        }

        val title =
            findTitle(link)

        if (title.isBlank()) {
            return null
        }

        return Manga(

            id = absoluteUrl,

            title = title,

            description = "",

            coverUrl = findCover(link),

            type = MangaType.MANHWA
        )
    }

    private fun findTitle(
        link: Element
    ): String {

        val direct =
            link.text().trim()

        if (
            direct.isNotBlank() &&
            !direct.startsWith(
                "Capítulo",
                true
            ) &&
            !direct.equals(
                "Ler",
                true
            )
        ) {
            return direct
        }

        val card =
            link.closest(
                """
                article,
                .page-item-detail,
                .c-tabs-item,
                .row,
                .item,
                .manga,
                .manga-item
                """.trimIndent()
            )

        return card
            ?.selectFirst(
                """
                h1,
                h2,
                h3,
                h4,
                .post-title,
                .manga-title
                """.trimIndent()
            )
            ?.text()
            ?.trim()
            .orEmpty()
    }

    private fun findCover(
        link: Element
    ): String? {

        val card =
            link.closest(
                """
                article,
                .page-item-detail,
                .c-tabs-item,
                .row,
                .item,
                .manga,
                .manga-item
                """.trimIndent()
            )

        val image =
            link.selectFirst("img")
                ?: card?.selectFirst("img")
                ?: return null

        return image.absUrl("data-src")
            .ifBlank {
                image.absUrl("data-lazy-src")
            }
            .ifBlank {
                image.absUrl("data-original")
            }
            .ifBlank {
                image.absUrl("src")
            }
            .ifBlank { null }
    }

}