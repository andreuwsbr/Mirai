package com.andrews.mirai.presentation.reader.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage

class ReaderScreenState(
    initialChapter: Chapter,
    initialPageIndex: Int
) {

    var activeChapter by mutableStateOf(
        initialChapter
    )

    var chapterStates by mutableStateOf<
            Map<String, ReaderChapterPagesState>
            >(
        emptyMap()
    )
        private set

    var pageByChapter by mutableStateOf(
        mapOf(
            initialChapter.id to initialPageIndex
        )
    )
        private set

    var controlsVisible by mutableStateOf(true)

    var settingsVisible by mutableStateOf(false)

    var requestedChapterId by mutableStateOf<String?>(
        null
    )
        private set

    var requestedPage by mutableStateOf<Int?>(
        null
    )
        private set

    fun getChapterState(
        chapterId: String
    ): ReaderChapterPagesState? {
        return chapterStates[chapterId]
    }

    fun isLoadingOrLoaded(
        chapterId: String
    ): Boolean {
        val chapterState =
            chapterStates[chapterId]

        return chapterState?.isLoading == true ||
                chapterState
                    ?.pages
                    ?.isNotEmpty() == true
    }

    fun markChapterLoading(
        chapterId: String
    ) {
        chapterStates =
            chapterStates + (
                    chapterId to
                            ReaderChapterPagesState(
                                isLoading = true
                            )
                    )
    }

    fun setChapterLoaded(
        chapterId: String,
        chapterState: ReaderChapterPagesState,
        safePageIndex: Int
    ) {
        chapterStates =
            chapterStates + (
                    chapterId to chapterState
                    )

        pageByChapter =
            pageByChapter + (
                    chapterId to safePageIndex
                    )
    }

    fun setChapterError(
        chapterId: String,
        message: String
    ) {
        chapterStates =
            chapterStates + (
                    chapterId to
                            ReaderChapterPagesState(
                                isLoading = false,
                                errorMessage = message
                            )
                    )
    }

    fun removeChapterState(
        chapterId: String
    ) {
        chapterStates =
            chapterStates - chapterId
    }

    fun getSavedPage(
        chapterId: String,
        fallbackPage: Int
    ): Int {
        return pageByChapter[
            chapterId
        ] ?: fallbackPage
    }

    fun updatePage(
        chapterId: String,
        pageIndex: Int
    ) {
        pageByChapter =
            pageByChapter + (
                    chapterId to pageIndex
                    )
    }

    fun currentPageIndex(
        chapterId: String,
        pages: List<ReaderPage>
    ): Int {
        return (
                pageByChapter[chapterId] ?: 0
                ).coerceIn(
                minimumValue = 0,
                maximumValue =
                    pages
                        .lastIndex
                        .coerceAtLeast(0)
            )
    }

    fun requestPage(
        chapterId: String,
        pageIndex: Int
    ) {
        requestedChapterId =
            chapterId

        requestedPage =
            pageIndex
    }

    fun consumeRequestedPage() {
        requestedChapterId = null
        requestedPage = null
    }

    fun toggleControls() {
        controlsVisible =
            !controlsVisible
    }
}