package com.andrews.mirai.presentation.reader.logic

import com.andrews.mirai.domain.model.Chapter

object ReaderChapterNavigation {

    fun orderedChapters(
        chapters: List<Chapter>
    ): List<Chapter> {
        return chapters.sortedBy { chapter ->
            chapter.number
        }
    }

    fun indexOf(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Int {
        return chapters.indexOfFirst { item ->
            item.id == chapter.id
        }
    }

    fun previousChapter(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Chapter? {
        val chapterIndex =
            indexOf(
                chapters = chapters,
                chapter = chapter
            )

        return if (chapterIndex > 0) {
            chapters.getOrNull(
                chapterIndex - 1
            )
        } else {
            null
        }
    }

    fun nextChapter(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Chapter? {
        val chapterIndex =
            indexOf(
                chapters = chapters,
                chapter = chapter
            )

        return if (chapterIndex >= 0) {
            chapters.getOrNull(
                chapterIndex + 1
            )
        } else {
            null
        }
    }
}