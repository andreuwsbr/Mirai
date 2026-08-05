package com.andrews.mirai.presentation.reader.mode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderPageContent
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import kotlinx.coroutines.flow.distinctUntilChanged
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import kotlin.math.abs

internal data class LongStripChapterSection(
    val chapter: Chapter,
    val pages: List<ReaderPage>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private sealed interface LongStripItem {
    val key: String

    data class Page(
        val chapter: Chapter,
        val page: ReaderPage
    ) : LongStripItem {
        override val key =
            "page|${chapter.id}|${page.index}|${page.imageUrl}"
    }

    data class Transition(
        val completedChapter: Chapter,
        val nextChapter: Chapter
    ) : LongStripItem {
        override val key =
            "transition|${completedChapter.id}|${nextChapter.id}"
    }

    data class Loading(
        val chapter: Chapter
    ) : LongStripItem {
        override val key = "loading|${chapter.id}"
    }

    data class Error(
        val chapter: Chapter,
        val message: String
    ) : LongStripItem {
        override val key = "error|${chapter.id}"
    }

    data class Empty(
        val chapter: Chapter
    ) : LongStripItem {
        override val key = "empty|${chapter.id}"
    }

    data class Finished(
        val chapter: Chapter
    ) : LongStripItem {
        override val key = "finished|${chapter.id}"
    }
}

@Composable
internal fun LongStripReader(
    sections: List<LongStripChapterSection>,
    mode: ReaderMode,
    gapDp: Int,
    initialChapterId: String,
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
    val items = remember(
        sections,
        showFinalCompletion
    ) {
        buildList {
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    add(
                        LongStripItem.Transition(
                            completedChapter =
                                sections[index - 1].chapter,
                            nextChapter = section.chapter
                        )
                    )
                }

                when {
                    section.isLoading -> {
                        add(
                            LongStripItem.Loading(
                                section.chapter
                            )
                        )
                    }

                    section.errorMessage != null -> {
                        add(
                            LongStripItem.Error(
                                chapter = section.chapter,
                                message = section.errorMessage
                            )
                        )
                    }

                    section.pages.isEmpty() -> {
                        add(
                            LongStripItem.Empty(
                                section.chapter
                            )
                        )
                    }

                    else -> {
                        section.pages.forEach { page ->
                            add(
                                LongStripItem.Page(
                                    chapter = section.chapter,
                                    page = page
                                )
                            )
                        }
                    }
                }
            }

            val finalChapter = sections.lastOrNull()?.chapter

            if (
                showFinalCompletion &&
                finalChapter != null
            ) {
                add(
                    LongStripItem.Finished(
                        finalChapter
                    )
                )
            }
        }
    }

    val initialItemIndex = remember(
        items,
        initialChapterId,
        initialPage
    ) {
        items.indexOfFirst { item ->
            item is LongStripItem.Page &&
                    item.chapter.id == initialChapterId &&
                    item.page.index >= initialPage
        }.coerceAtLeast(0)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialItemIndex
    )

    val zoomableState = rememberZoomableState()

    LaunchedEffect(listState, items) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter =
                (
                        layoutInfo.viewportStartOffset +
                                layoutInfo.viewportEndOffset
                        ) / 2

            layoutInfo.visibleItemsInfo
                .mapNotNull { visibleItem ->
                    val item = items.getOrNull(
                        visibleItem.index
                    )

                    if (item is LongStripItem.Page) {
                        val itemCenter =
                            visibleItem.offset +
                                    visibleItem.size / 2

                        Triple(
                            item.chapter,
                            item.page.index,
                            abs(itemCenter - viewportCenter)
                        )
                    } else {
                        null
                    }
                }
                .minByOrNull { value ->
                    value.third
                }
                ?.let { value ->
                    value.first to value.second
                }
        }
            .distinctUntilChanged()
            .collect { position ->
                if (position != null) {
                    onPositionChanged(
                        position.first,
                        position.second
                    )
                }
            }
    }

    LaunchedEffect(
        requestedChapterId,
        requestedPage,
        items
    ) {
        val chapterId = requestedChapterId
        val pageIndex = requestedPage

        if (
            chapterId != null &&
            pageIndex != null
        ) {
            val targetIndex = items.indexOfFirst { item ->
                item is LongStripItem.Page &&
                        item.chapter.id == chapterId &&
                        item.page.index >= pageIndex
            }

            if (targetIndex >= 0) {
                listState.animateScrollToItem(
                    targetIndex
                )

                onRequestedPageConsumed()
            }
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
                count = items.size,
                key = { index ->
                    items[index].key
                }
            ) { index ->
                when (val item = items[index]) {
                    is LongStripItem.Page -> {
                        ReaderPageContent(
                            page = item.page,
                            imageDownloader =
                                imageDownloader,
                            paged = false,
                            backgroundColor =
                                backgroundColor,
                            onTap = onTap
                        )
                    }

                    is LongStripItem.Transition -> {
                        ChapterTransitionContent(
                            completedChapter =
                                item.completedChapter,
                            nextChapter = item.nextChapter,
                            backgroundColor =
                                backgroundColor
                        )
                    }

                    is LongStripItem.Loading -> {
                        ChapterStatusContent(
                            text =
                                "Carregando ${item.chapter.name}...",
                            loading = true,
                            backgroundColor =
                                backgroundColor
                        )
                    }

                    is LongStripItem.Error -> {
                        ChapterStatusContent(
                            text = item.message,
                            loading = false,
                            backgroundColor =
                                backgroundColor
                        )
                    }

                    is LongStripItem.Empty -> {
                        ChapterStatusContent(
                            text =
                                "Nenhuma página foi encontrada em ${item.chapter.name}.",
                            loading = false,
                            backgroundColor =
                                backgroundColor
                        )
                    }

                    is LongStripItem.Finished -> {
                        FinishedContent(
                            chapter = item.chapter,
                            backgroundColor =
                                backgroundColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterTransitionContent(
    completedChapter: Chapter,
    nextChapter: Chapter,
    backgroundColor: Color
) {
    val contentColor = contentColorFor(
        backgroundColor
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .background(backgroundColor)
            .padding(
                horizontal = 40.dp,
                vertical = 48.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Concluído:",
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = completedChapter.name,
            color = contentColor,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Próximo:",
            modifier = Modifier.padding(top = 28.dp),
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = nextChapter.name,
            color = contentColor,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun ChapterStatusContent(
    text: String,
    loading: Boolean,
    backgroundColor: Color
) {
    val contentColor = contentColorFor(
        backgroundColor
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp)
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
        }

        Text(
            text = text,
            modifier = Modifier.padding(
                top = if (loading) 16.dp else 0.dp
            ),
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun FinishedContent(
    chapter: Chapter,
    backgroundColor: Color
) {
    val contentColor = contentColorFor(
        backgroundColor
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .background(backgroundColor)
            .padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Leitura concluída",
            color = contentColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = chapter.name,
            modifier = Modifier.padding(top = 12.dp),
            color = contentColor,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

private fun contentColorFor(
    backgroundColor: Color
): Color {
    return if (backgroundColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
}