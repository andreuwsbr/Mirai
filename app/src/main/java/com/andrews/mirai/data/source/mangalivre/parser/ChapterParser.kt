package com.andrews.mirai.data.source.mangalivre.parser

import com.andrews.mirai.data.source.mangalivre.MangaLivreSelectors
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ChapterParser(
    private val baseUrl: String
) {

    fun parse(
        html: String,
        manga: Manga
    ): List<Chapter> {
        val document =
            Jsoup.parse(html, baseUrl)

        val chaptersByUrl =
            linkedMapOf<String, Chapter>()

        document
            .select(
                MangaLivreSelectors.CHAPTER_LINK
            )
            .forEach { link ->
                val chapter = parseChapter(
                    link = link,
                    manga = manga
                ) ?: return@forEach

                chaptersByUrl[chapter.id] =
                    chapter
            }

        return chaptersByUrl
            .values
            .toList()
    }

    private fun parseChapter(
        link: Element,
        manga: Manga
    ): Chapter? {
        val absoluteUrl = link
            .absUrl("href")
            .substringBefore("#")
            .substringBefore("?")
            .trimEnd('/')

        if (absoluteUrl.isBlank()) {
            return null
        }

        val name = link
            .selectFirst(
                MangaLivreSelectors
                    .CHAPTER_NUMBER
            )
            ?.text()
            ?.trim()
            .orEmpty()
            .ifBlank {
                link.text().trim()
            }

        if (name.isBlank()) {
            return null
        }

        val number =
            extractChapterNumber(name)

        val chapterItem = link.closest(
            MangaLivreSelectors.CHAPTER_ITEM
        )

        val uploadedAt = chapterItem
            ?.selectFirst(
                MangaLivreSelectors
                    .CHAPTER_DATE
            )
            ?.text()
            ?.trim()
            .orEmpty()

        return Chapter(
            id = absoluteUrl,
            mangaId = manga.id,
            name = name,
            number = number,
            url = absoluteUrl,
            uploadedAt = uploadedAt
        )
    }

    private fun extractChapterNumber(
        name: String
    ): Double {
        val numberText = Regex(
            pattern =
                """(\d+(?:[.,]\d+)?)"""
        )
            .find(name)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(',', '.')

        return numberText
            ?.toDoubleOrNull()
            ?: 0.0
    }
}