package com.andrews.mirai.presentation.reader.navigation

sealed interface ReaderNavigationResult {

    data class Page(
        val pageIndex: Int
    ) : ReaderNavigationResult

    data object PreviousChapter :
        ReaderNavigationResult

    data object NextChapter :
        ReaderNavigationResult

    data object None :
        ReaderNavigationResult
}