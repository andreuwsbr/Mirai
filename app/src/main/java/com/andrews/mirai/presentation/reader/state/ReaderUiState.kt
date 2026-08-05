package com.andrews.mirai.presentation.reader.state

import com.andrews.mirai.domain.model.Chapter

data class ReaderUiState(
    val activeChapter: Chapter,
    val previousChapter: Chapter? = null,
    val nextChapter: Chapter? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val controlsVisible: Boolean = true,
    val settingsVisible: Boolean = false
) {
    val hasPreviousChapter: Boolean
        get() = previousChapter != null

    val hasNextChapter: Boolean
        get() = nextChapter != null

    val hasPages: Boolean
        get() = totalPages > 0

    val currentPageNumber: Int
        get() {
            if (totalPages <= 0) {
                return 0
            }

            return (
                    currentPageIndex + 1
                    ).coerceIn(
                    minimumValue = 1,
                    maximumValue = totalPages
                )
        }
}