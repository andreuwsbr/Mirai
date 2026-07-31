package com.andrews.mirai.data.source.mangalivre

import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HomeParser(
    private val baseUrl: String
) {

    fun parse(html: String): List<Manga> {
        val document = Jsoup.parse(html, baseUrl)
        val mangasByUrl = linkedMapOf<String, Manga>()

        document
            .select("a[href*=/manga/]")
            .forEach { link ->
                val manga = parseMangaLink(link) ?: return@forEach
                mangasByUrl[manga.id] = manga
            }

        return mangasByUrl.values.toList()
    }

    private fun parseMangaLink(link: Element): Manga? {
        val absoluteUrl = link
            .absUrl("href")
            .substringBefore("#")
            .substringBefore("?")
            .trimEnd('/')

        if (!absoluteUrl.contains("/manga/")) {
            return null
        }

        val slug = absoluteUrl
            .substringAfter("/manga/")
            .substringBefore("/")
            .trim()

        if (slug.isBlank()) {
            return null
        }

        val title = findTitle(link)

        if (
            title.isBlank() ||
            title.equals("Ler", ignoreCase = true) ||
            title.startsWith("Capítulo", ignoreCase = true)
        ) {
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

    private fun findTitle(link: Element): String {
        val directText = link.text().trim()

        if (
            directText.isNotBlank() &&
            !directText.equals("Ler", ignoreCase = true) &&
            !directText.startsWith("Capítulo", ignoreCase = true)
        ) {
            return directText
        }

        val card = link.closest(
            "article, .item, .page-item-detail, .row, .manga, .manga-item"
        )

        return card
            ?.selectFirst("h1, h2, h3, h4, .post-title, .manga-title")
            ?.text()
            ?.trim()
            .orEmpty()
    }

    private fun findCover(link: Element): String? {
        val card = link.closest(
            "article, .item, .page-item-detail, .row, .manga, .manga-item"
        )

        val image = link.selectFirst("img")
            ?: card?.selectFirst("img")
            ?: return null

        return image.absUrl("data-src")
            .ifBlank { image.absUrl("data-lazy-src") }
            .ifBlank { image.absUrl("src") }
            .ifBlank { null }
    }
}