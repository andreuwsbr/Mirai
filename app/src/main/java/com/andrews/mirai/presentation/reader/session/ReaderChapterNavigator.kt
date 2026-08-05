package com.andrews.mirai.presentation.reader.session

import com.andrews.mirai.domain.model.Chapter

class ReaderChapterNavigator(
    private val chapters: List<Chapter>
) {

    fun indexOf(
        chapter: Chapter
    ): Int {
        return indexOf(
            chapterId = chapter.id
        )
    }

    fun indexOf(
        chapterId: String
    ): Int {
        return chapters.indexOfFirst { chapter ->
            chapter.id == chapterId
        }
    }

    fun previousChapter(
        chapter: Chapter
    ): Chapter? {
        val currentIndex =
            indexOf(
                chapter
            )

        if (currentIndex <= 0) {
            return null
        }

        return chapters.getOrNull(
            currentIndex - 1
        )
    }

    fun nextChapter(
        chapter: Chapter
    ): Chapter? {
        val currentIndex =
            indexOf(
                chapter
            )

        if (currentIndex < 0) {
            return null
        }

        return chapters.getOrNull(
            currentIndex + 1
        )
    }

    fun isForwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        val fromIndex =
            indexOf(
                fromChapter
            )

        val toIndex =
            indexOf(
                toChapter
            )

        return fromIndex >= 0 &&
                toIndex > fromIndex
    }

    fun isBackwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        val fromIndex =
            indexOf(
                fromChapter
            )

        val toIndex =
            indexOf(
                toChapter
            )

        return fromIndex >= 0 &&
                toIndex >= 0 &&
                toIndex < fromIndex
    }
}