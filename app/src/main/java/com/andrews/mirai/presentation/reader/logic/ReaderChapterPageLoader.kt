package com.andrews.mirai.presentation.reader.logic

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.state.ReaderChapterPagesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ReaderChapterLoadResult(
    val state: ReaderChapterPagesState,
    val safePageIndex: Int
)

class ReaderChapterPageLoader(
    private val pageProvider: ReaderPageProvider
) {

    suspend fun load(
        chapter: Chapter,
        savedPageIndex: Int
    ): ReaderChapterLoadResult {
        val pages =
            withContext(Dispatchers.IO) {
                pageProvider.getPages(
                    chapter
                )
            }

        val safePageIndex =
            savedPageIndex.coerceIn(
                minimumValue = 0,
                maximumValue =
                    pages
                        .lastIndex
                        .coerceAtLeast(0)
            )

        return ReaderChapterLoadResult(
            state =
                ReaderChapterPagesState(
                    pages = pages,
                    isLoading = false,
                    errorMessage = null
                ),
            safePageIndex = safePageIndex
        )
    }
}