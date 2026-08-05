package com.andrews.mirai.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.andrews.mirai.presentation.reader.components.ReaderBottomBar
import com.andrews.mirai.presentation.reader.components.ReaderTopBar

@Composable
internal fun BoxScope.ReaderControls(
    controlsVisible: Boolean,
    settingsVisible: Boolean,
    pagesAvailable: Boolean,
    chapterName: String,
    currentPageIndex: Int,
    totalPages: Int,
    showPageNumber: Boolean,
    hasPreviousChapter: Boolean,
    hasNextChapter: Boolean,
    onBackClick: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onPreviousChapterClick: () -> Unit,
    onNextChapterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    AnimatedVisibility(
        visible =
            controlsVisible &&
                    !settingsVisible,
        enter =
            fadeIn(),
        exit =
            fadeOut(),
        modifier =
            Modifier.align(
                Alignment.TopCenter
            )
    ) {
        ReaderTopBar(
            chapterName =
                chapterName,
            currentPage =
                currentPageIndex + 1,
            totalPages =
                totalPages,
            showPageNumber =
                showPageNumber,
            onBackClick =
                onBackClick
        )
    }

    AnimatedVisibility(
        visible =
            controlsVisible &&
                    !settingsVisible &&
                    pagesAvailable,
        enter =
            fadeIn(),
        exit =
            fadeOut(),
        modifier =
            Modifier.align(
                Alignment.BottomCenter
            )
    ) {
        ReaderBottomBar(
            currentPage =
                currentPageIndex,
            totalPages =
                totalPages,
            showPageNumber =
                showPageNumber,
            hasPreviousChapter =
                hasPreviousChapter,
            hasNextChapter =
                hasNextChapter,
            onPageSelected =
                onPageSelected,
            onPreviousChapterClick =
                onPreviousChapterClick,
            onNextChapterClick =
                onNextChapterClick,
            onSettingsClick =
                onSettingsClick
        )
    }
}