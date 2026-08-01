package com.andrews.mirai.presentation.reader.mode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderPageContent
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import kotlinx.coroutines.flow.distinctUntilChanged
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun LongStripReader(
    pages: List<ReaderPage>,
    mode: ReaderMode,
    gapDp: Int,
    initialPage: Int,
    requestedPage: Int?,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPageChanged: (Int) -> Unit,
    onRequestedPageConsumed: () -> Unit,
    onEndReached: () -> Unit,
    onTap: () -> Unit
) {
    val safeInitialPage = initialPage.coerceIn(
        minimumValue = 0,
        maximumValue = pages.lastIndex
    )

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = safeInitialPage
    )

    val zoomableState = rememberZoomableState()

    var userScrolled by remember(pages) {
        mutableStateOf(false)
    }

    var endReported by remember(pages) {
        mutableStateOf(false)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }
            .distinctUntilChanged()
            .collect { pageIndex ->
                onPageChanged(pageIndex)
            }
    }

    LaunchedEffect(listState, pages) {
        snapshotFlow {
            listState.isScrollInProgress
        }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling) {
                    userScrolled = true
                }
            }
    }

    LaunchedEffect(
        listState,
        pages,
        userScrolled
    ) {
        snapshotFlow {
            val layoutInfo =
                listState.layoutInfo

            val lastVisibleItem =
                layoutInfo.visibleItemsInfo
                    .lastOrNull()

            val lastPageFullyVisible =
                lastVisibleItem != null &&
                        lastVisibleItem.index ==
                        pages.lastIndex &&
                        lastVisibleItem.offset +
                        lastVisibleItem.size <=
                        layoutInfo.viewportEndOffset

            userScrolled &&
                    lastPageFullyVisible &&
                    !listState.isScrollInProgress
        }
            .distinctUntilChanged()
            .collect { reachedEnd ->
                if (
                    reachedEnd &&
                    !endReported
                ) {
                    endReported = true
                    onEndReached()
                }
            }
    }

    LaunchedEffect(requestedPage) {
        requestedPage?.let { pageIndex ->
            listState.animateScrollToItem(
                pageIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue = pages.lastIndex
                )
            )

            onRequestedPageConsumed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .zoomable(
                state = zoomableState,
                onClick = {
                    onTap()
                }
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement =
                if (
                    mode ==
                    ReaderMode.LONG_STRIP_GAPS
                ) {
                    Arrangement.spacedBy(
                        gapDp.dp
                    )
                } else {
                    Arrangement.Top
                }
        ) {
            items(
                items = pages,
                key = { page ->
                    "${page.index}-${page.imageUrl}"
                }
            ) { page ->
                ReaderPageContent(
                    page = page,
                    imageDownloader =
                        imageDownloader,
                    paged = false,
                    backgroundColor =
                        backgroundColor,
                    onTap = {}
                )
            }
        }
    }
}