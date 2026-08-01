package com.andrews.mirai.data.source.sakura

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup

class ChapterParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): List<Chapter> {

        val doc = Jsoup.parse(html, baseUrl)

        return doc
            .select("div.chapter-item.parent")
            .map { element ->

                val url =
                    element.attr("data-url")

                val name =
                    element
                        .selectFirst("a.title-text")
                        ?.text()
                        ?.trim()
                        ?: ""

                val uploaded =
                    element
                        .selectFirst(".meta-info span")
                        ?.text()
                        ?: ""

                val number =
                    Regex("""\d+(\.\d+)?""")
                        .find(name)
                        ?.value
                        ?.toDoubleOrNull()
                        ?: 0.0

                Chapter(
                    id = url,
                    mangaId = manga.id,
                    name = name,
                    number = number,
                    url = baseUrl + url,
                    uploadedAt = uploaded
                )

            }
    }
}