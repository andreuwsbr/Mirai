package com.andrews.mirai.presentation.reader.navigation

class ReaderPageNavigator(
    totalPages: Int
) {

    private val totalPages =
        totalPages.coerceAtLeast(
            0
        )

    fun navigate(
        currentPage: Int,
        direction: ReaderPageDirection
    ): ReaderNavigationResult {
        if (totalPages <= 0) {
            return ReaderNavigationResult.None
        }

        val safeCurrentPage =
            currentPage.coerceIn(
                minimumValue = 0,
                maximumValue =
                    totalPages - 1
            )

        return when (direction) {
            ReaderPageDirection.PREVIOUS -> {
                if (safeCurrentPage > 0) {
                    ReaderNavigationResult.Page(
                        pageIndex =
                            safeCurrentPage - 1
                    )
                } else {
                    ReaderNavigationResult
                        .PreviousChapter
                }
            }

            ReaderPageDirection.NEXT -> {
                if (
                    safeCurrentPage <
                    totalPages - 1
                ) {
                    ReaderNavigationResult.Page(
                        pageIndex =
                            safeCurrentPage + 1
                    )
                } else {
                    ReaderNavigationResult
                        .NextChapter
                }
            }
        }
    }
}