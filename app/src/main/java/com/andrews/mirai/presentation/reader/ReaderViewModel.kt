package com.andrews.mirai.presentation.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.session.ReaderSession

class ReaderViewModel(
    initialChapter: Chapter,
    initialChapters: List<Chapter>
) {

    var session by mutableStateOf(
        ReaderSession.create(
            chapters = initialChapters,
            activeChapter = initialChapter
        )
    )
        private set

    val orderedChapters: List<Chapter>
        get() = session.chapters

    val activeChapter: Chapter
        get() = session.activeChapter

    val previousChapter: Chapter?
        get() = session.previousChapter

    val nextChapter: Chapter?
        get() = session.nextChapter

    fun updateChapters(
        chapters: List<Chapter>,
        activeChapter: Chapter =
            session.activeChapter
    ) {
        session =
            ReaderSession.create(
                chapters = chapters,
                activeChapter = activeChapter
            )
    }

    fun selectChapter(
        chapter: Chapter
    ) {
        session =
            session.withActiveChapter(
                chapter
            )
    }

    fun isForwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        return session.isForwardMovement(
            fromChapter = fromChapter,
            toChapter = toChapter
        )
    }

    fun isBackwardMovement(
        fromChapter: Chapter,
        toChapter: Chapter
    ): Boolean {
        return session.isBackwardMovement(
            fromChapter = fromChapter,
            toChapter = toChapter
        )
    }
}