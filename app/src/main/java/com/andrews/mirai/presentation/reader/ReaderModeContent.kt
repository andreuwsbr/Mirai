package com.andrews.mirai.presentation.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.mode.HorizontalPagedReader
import com.andrews.mirai.presentation.reader.mode.LongStripChapterSection
import com.andrews.mirai.presentation.reader.mode.LongStripReader
import com.andrews.mirai.presentation.reader.mode.VerticalPagedReader
import com.andrews.mirai.presentation.reader.settings.ReaderMode

@Composable
internal fun ReaderModeContent(
    activeChapter: Chapter,
    pages: List<ReaderPage>,
    longStripSections: List<LongStripChapterSection>,
    mode: ReaderMode,
    longStripGapDp: Int,
    initialPage: Int,
    requestedChapterId: String?,
    requestedPage: Int?,
    showFinalCompletion: Boolean,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPositionChanged: (
        chapter: Chapter,
        pageIndex: Int
    ) -> Unit,
    onRequestedPageConsumed: () -> Unit,
    onTap: () -> Unit
) {
    when (mode) {
        ReaderMode.LONG_STRIP,
        ReaderMode.LONG_STRIP_GAPS -> {
            LongStripReader(
                sections = longStripSections,
                mode = mode,
                gapDp = longStripGapDp,
                initialChapterId = activeChapter.id,
                initialPage = initialPage,
                requestedChapterId =
                    requestedChapterId,
                requestedPage = requestedPage,
                showFinalCompletion =
                    showFinalCompletion,
                imageDownloader = imageDownloader,
                backgroundColor = backgroundColor,
                onPositionChanged =
                    onPositionChanged,
                onRequestedPageConsumed =
                    onRequestedPageConsumed,
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
                onPageChanged = { pageIndex ->
                    onPositionChanged(
                        activeChapter,
                        pageIndex
                    )
                },
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
                onPageChanged = { pageIndex ->
                    onPositionChanged(
                        activeChapter,
                        pageIndex
                    )
                },
                onRequestedPageConsumed =
                    onRequestedPageConsumed,
                onTap = onTap
            )
        }
    }
}