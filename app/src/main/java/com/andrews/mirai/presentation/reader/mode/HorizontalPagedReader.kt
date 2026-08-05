package com.andrews.mirai.presentation.reader.mode

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderPageContent
import com.andrews.mirai.presentation.reader.gesture.ReaderTapAction
import com.andrews.mirai.presentation.reader.gesture.readerTapOverlay
import com.andrews.mirai.presentation.reader.navigation.ReaderNavigationResult
import com.andrews.mirai.presentation.reader.navigation.ReaderPageDirection
import com.andrews.mirai.presentation.reader.navigation.ReaderPageNavigator
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import com.andrews.mirai.presentation.reader.settings.ReaderTapMode
import com.andrews.mirai.presentation.reader.settings.ReaderTapZoneSize
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagedReader(
    pages: List<ReaderPage>,
    mode: ReaderMode,
    initialPage: Int,
    requestedPage: Int?,
    tapMode: ReaderTapMode,
    tapZoneSize: ReaderTapZoneSize,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPageChanged: (Int) -> Unit,
    onPreviousChapterRequested: () -> Unit,
    onNextChapterRequested: () -> Unit,
    onRequestedPageConsumed: () -> Unit,
    onCenterTap: () -> Unit
) {
    val safeInitialPage =
        initialPage.coerceIn(
            minimumValue = 0,
            maximumValue =
                pages
                    .lastIndex
                    .coerceAtLeast(0)
        )

    val pagerState =
        rememberPagerState(
            initialPage =
                safeInitialPage,
            pageCount = {
                pages.size
            }
        )

    val coroutineScope =
        rememberCoroutineScope()

    val pageNavigator =
        remember(
            pages.size
        ) {
            ReaderPageNavigator(
                totalPages =
                    pages.size
            )
        }

    val reverseReadingDirection =
        mode ==
                ReaderMode.PAGED_RIGHT_TO_LEFT

    fun executeNavigation(
        direction: ReaderPageDirection
    ) {
        when (
            val result =
                pageNavigator.navigate(
                    currentPage =
                        pagerState.currentPage,
                    direction =
                        direction
                )
        ) {
            is ReaderNavigationResult.Page -> {
                coroutineScope.launch {
                    pagerState
                        .animateScrollToPage(
                            result.pageIndex
                        )
                }
            }

            ReaderNavigationResult
                .PreviousChapter -> {
                onPreviousChapterRequested()
            }

            ReaderNavigationResult
                .NextChapter -> {
                onNextChapterRequested()
            }

            ReaderNavigationResult.None -> {
                Unit
            }
        }
    }

    LaunchedEffect(
        pagerState
    ) {
        snapshotFlow {
            pagerState.currentPage
        }
            .distinctUntilChanged()
            .collect { pageIndex ->
                onPageChanged(
                    pageIndex
                )
            }
    }

    LaunchedEffect(
        requestedPage
    ) {
        requestedPage?.let { pageIndex ->
            pagerState
                .animateScrollToPage(
                    pageIndex.coerceIn(
                        minimumValue = 0,
                        maximumValue =
                            pages
                                .lastIndex
                                .coerceAtLeast(0)
                    )
                )

            onRequestedPageConsumed()
        }
    }

    HorizontalPager(
        state =
            pagerState,
        reverseLayout =
            reverseReadingDirection,
        modifier =
            Modifier
                .fillMaxSize()
                .readerTapOverlay(
                    enabled =
                        tapMode ==
                                ReaderTapMode
                                    .TAP_AND_SWIPE,
                    edgeFraction =
                        tapZoneSize
                            .edgeFraction,
                    vertical =
                        false,
                    reverseReadingDirection =
                        reverseReadingDirection,
                    onAction = { action ->
                        when (action) {
                            ReaderTapAction
                                .PREVIOUS_PAGE -> {
                                executeNavigation(
                                    ReaderPageDirection
                                        .PREVIOUS
                                )
                            }

                            ReaderTapAction
                                .NEXT_PAGE -> {
                                executeNavigation(
                                    ReaderPageDirection
                                        .NEXT
                                )
                            }

                            ReaderTapAction
                                .TOGGLE_CONTROLS -> {
                                onCenterTap()
                            }
                        }
                    }
                )
    ) { pageIndex ->
        ReaderPageContent(
            page =
                pages[pageIndex],
            imageDownloader =
                imageDownloader,
            paged =
                true,
            backgroundColor =
                backgroundColor,
            onTap = {
                if (
                    tapMode ==
                    ReaderTapMode.SWIPE_ONLY
                ) {
                    onCenterTap()
                }
            }
        )
    }
}