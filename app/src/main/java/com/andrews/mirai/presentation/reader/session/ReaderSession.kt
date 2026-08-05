package com.andrews.mirai.presentation.reader.session

import com.andrews.mirai.domain.model.Chapter

data class ReaderSession(
    val chapters: List<Chapter>,
    val activeChapter: Chapter
) {

    private val navigator =
        ReaderChapterNavigator(
            chapters = chapters
        )

    val previousChapter: Chapter?
        get() {
            return navigator.previousChapter(
                activeChapter
            )
        }

    val nextChapter: Chapter?
        get() {
            return navigator.nextChapter(
                activeChapter
            )
        }

    fun indexOf(
        chapter: Chapter
    ): Int {
        return navigator.indexOf(
            chapter
        )
    }

    fun isForwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        return navigator.isForwardMovement(
            fromChapter = fromChapter,
            toChapter = toChapter
        )
    }

    fun isBackwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        return navigator.isBackwardMovement(
            fromChapter = fromChapter,
            toChapter = toChapter
        )
    }

    fun withActiveChapter(
        chapter: Chapter
    ): ReaderSession {
        val resolvedChapters =
            ReaderChapterOrderResolver.resolve(
                chapters = chapters,
                activeChapter = chapter
            )

        val resolvedActiveChapter =
            resolvedChapters
                .firstOrNull { item ->
                    item.id == chapter.id
                }
                ?: chapter

        return copy(
            chapters = resolvedChapters,
            activeChapter =
                resolvedActiveChapter
        )
    }

    companion object {

        fun create(
            chapters: List<Chapter>,
            activeChapter: Chapter
        ): ReaderSession {
            val orderedChapters =
                ReaderChapterOrderResolver.resolve(
                    chapters = chapters,
                    activeChapter =
                        activeChapter
                )

            val resolvedActiveChapter =
                orderedChapters
                    .firstOrNull { chapter ->
                        chapter.id ==
                                activeChapter.id
                    }
                    ?: activeChapter

            return ReaderSession(
                chapters =
                    orderedChapters,
                activeChapter =
                    resolvedActiveChapter
            )
        }
    }
}