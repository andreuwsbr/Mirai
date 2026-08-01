package com.andrews.mirai.data.source.sakura

import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup

class DetailsParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): Manga {

        val doc = Jsoup.parse(html, baseUrl)

        val title =
            doc.selectFirst("h1.h1-titulo")
                ?.text()
                ?.trim()
                ?: manga.title

        val author =
            doc.selectFirst("p.autor a")
                ?.text()
                ?.trim()
                ?: manga.author

        val description =
            doc.selectFirst("p.sinopse")
                ?.text()
                ?.trim()
                ?: manga.description

        val cover =
            doc.selectFirst("img.capa")
                ?.absUrl("src")
                ?: manga.coverUrl

        val genres =
            doc.select("div.generos a")
                .map {
                    it.text().trim()
                }

        return manga.copy(
            title = title,
            author = author,
            description = description,
            coverUrl = cover,
            genres = genres
        )
    }
}