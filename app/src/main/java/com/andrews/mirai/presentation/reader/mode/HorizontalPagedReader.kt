package com.andrews.mirai.presentation.reader.mode

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderPageContent
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagedReader(
    pages: List<ReaderPage>,
    mode: ReaderMode,
    initialPage: Int,
    requestedPage: Int?,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPageChanged: (Int) -> Unit,
    onRequestedPageConsumed: () -> Unit,
    onTap: () -> Unit
) {
    val safeInitialPage = initialPage.coerceIn(
        minimumValue = 0,
        maximumValue = pages.lastIndex
    )

    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        pageCount = {
            pages.size
        }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow {
            pagerState.currentPage
        }
            .distinctUntilChanged()
            .collect { pageIndex ->
                onPageChanged(pageIndex)
            }
    }

    LaunchedEffect(requestedPage) {
        requestedPage?.let { pageIndex ->
            pagerState.animateScrollToPage(
                pageIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue = pages.lastIndex
                )
            )

            onRequestedPageConsumed()
        }
    }

    HorizontalPager(
        state = pagerState,
        reverseLayout =
            mode == ReaderMode.PAGED_RIGHT_TO_LEFT,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        ReaderPageContent(
            page = pages[pageIndex],
            imageDownloader = imageDownloader,
            paged = true,
            backgroundColor = backgroundColor,
            onTap = onTap
        )
    }
}