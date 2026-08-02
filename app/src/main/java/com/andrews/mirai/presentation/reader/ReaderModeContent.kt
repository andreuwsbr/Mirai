package com.andrews.mirai.presentation.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.mode.HorizontalPagedReader
import com.andrews.mirai.presentation.reader.mode.LongStripReader
import com.andrews.mirai.presentation.reader.mode.VerticalPagedReader
import com.andrews.mirai.presentation.reader.settings.ReaderMode

@Composable
internal fun ReaderModeContent(
    pages: List<ReaderPage>,
    mode: ReaderMode,
    longStripGapDp: Int,
    initialPage: Int,
    requestedPage: Int?,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPageChanged: (Int) -> Unit,
    onRequestedPageConsumed: () -> Unit,
    onEndReached: () -> Unit,
    onTap: () -> Unit
) {
    when (mode) {
        ReaderMode.LONG_STRIP,
        ReaderMode.LONG_STRIP_GAPS -> {
            LongStripReader(
                pages = pages,
                mode = mode,
                gapDp = longStripGapDp,
                initialPage = initialPage,
                requestedPage = requestedPage,
                imageDownloader = imageDownloader,
                backgroundColor = backgroundColor,
                onPageChanged = onPageChanged,
                onRequestedPageConsumed =
                    onRequestedPageConsumed,
                onEndReached = onEndReached,
                onTap = onTap
            )
        }

        ReaderMode.PAGED_LEFT_TO_RIGHT,
        ReaderMode.PAGED_RIGHT_TO_LEFT -> {
            HorizontalPagedReader(
                pages = pages,
                mode = mode,
                initialPage = initialPage,
                requestedPage = requestedPage,
                imageDownloader = imageDownloader,
                backgroundColor = backgroundColor,
                onPageChanged = onPageChanged,
                onRequestedPageConsumed =
                    onRequestedPageConsumed,
                onTap = onTap
            )
        }

        ReaderMode.PAGED_VERTICAL -> {
            VerticalPagedReader(
                pages = pages,
                initialPage = initialPage,
                requestedPage = requestedPage,
                imageDownloader = imageDownloader,
                backgroundColor = backgroundColor,
                onPageChanged = onPageChanged,
                onRequestedPageConsumed =
                    onRequestedPageConsumed,
                onTap = onTap
            )
        }
    }
}