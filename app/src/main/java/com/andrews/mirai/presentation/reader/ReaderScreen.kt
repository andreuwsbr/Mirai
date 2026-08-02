package com.andrews.mirai.presentation.reader

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderErrorContent
import com.andrews.mirai.presentation.reader.components.ReaderLoadingContent
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.settings.ReaderBackground
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsSheet
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val IMAGE_LOG_TAG =
    "MIRAI_IMAGE"

private const val PRELOAD_DISTANCE =
    3

@Composable
fun ReaderScreen(
    chapter: Chapter,
    chapters: List<Chapter>,
    onChapterSelected: (Chapter) -> Unit,
    onBackClick: () -> Unit
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val orderedChapters = remember(chapters) {
        chapters.sortedBy { item ->
            item.number
        }
    }

    val currentChapterIndex = remember(
        chapter.id,
        orderedChapters
    ) {
        orderedChapters.indexOfFirst { item ->
            item.id == chapter.id
        }
    }

    val previousChapter =
        orderedChapters.getOrNull(
            currentChapterIndex - 1
        )

    val nextChapter =
        orderedChapters.getOrNull(
            currentChapterIndex + 1
        )

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    val settingsStore = remember(applicationContext) {
        ReaderSettingsStore(applicationContext)
    }

    val imageCache = remember(applicationContext) {
        ReaderImageCache(applicationContext)
    }

    val imageDownloader = remember(imageCache) {
        ReaderImageDownloader(imageCache)
    }

    var preferences by remember {
        mutableStateOf(
            settingsStore.load()
        )
    }

    var pages by remember(chapter.id) {
        mutableStateOf<List<ReaderPage>>(
            emptyList()
        )
    }

    var isLoading by remember(chapter.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(chapter.id) {
        mutableStateOf<String?>(null)
    }

    var controlsVisible by remember(chapter.id) {
        mutableStateOf(true)
    }

    var settingsVisible by remember {
        mutableStateOf(false)
    }

    var currentPageIndex by remember(chapter.id) {
        mutableIntStateOf(
            progressStore.getPage(
                chapter.id
            )
        )
    }

    var requestedPage by remember(chapter.id) {
        mutableStateOf<Int?>(null)
    }

    var automaticChapterChangeStarted by remember(
        chapter.id
    ) {
        mutableStateOf(false)
    }

    val backgroundColor = when (
        preferences.background
    ) {
        ReaderBackground.BLACK -> {
            Color.Black
        }

        ReaderBackground.GRAY -> {
            Color(0xFF444444)
        }

        ReaderBackground.WHITE -> {
            Color.White
        }
    }

    val foregroundColor =
        if (
            preferences.background ==
            ReaderBackground.WHITE
        ) {
            Color.Black
        } else {
            Color.White
        }

    ReaderSystemUiEffects(
        keepScreenOn =
            preferences.keepScreenOn,
        fullscreen =
            preferences.fullscreen
    )

    BackHandler {
        if (settingsVisible) {
            settingsVisible = false
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(preferences) {
        settingsStore.save(
            preferences
        )
    }

    LaunchedEffect(chapter.id) {
        isLoading = true
        errorMessage = null
        pages = emptyList()

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository
                    .currentSource
                    .getPages(
                        chapter = chapter
                    )
            }
        }.onSuccess { result ->
            pages = result

            currentPageIndex =
                currentPageIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue =
                        result.lastIndex
                            .coerceAtLeast(0)
                )

            Log.d(
                IMAGE_LOG_TAG,
                "${chapter.name}: ${result.size} páginas"
            )
        }.onFailure { throwable ->
            errorMessage =
                throwable.message
                    ?: "Não foi possível carregar as páginas."

            Log.e(
                IMAGE_LOG_TAG,
                "Erro ao carregar ${chapter.name}",
                throwable
            )
        }

        isLoading = false
    }

    LaunchedEffect(
        chapter.id,
        currentPageIndex,
        pages.size
    ) {
        if (pages.isNotEmpty()) {
            progressStore.savePage(
                chapterId = chapter.id,
                pageIndex =
                    currentPageIndex.coerceIn(
                        minimumValue = 0,
                        maximumValue =
                            pages.lastIndex
                    ),
                totalPages = pages.size
            )
        }
    }

    LaunchedEffect(
        pages,
        currentPageIndex,
        imageDownloader
    ) {
        if (pages.isEmpty()) {
            return@LaunchedEffect
        }

        val startIndex =
            (currentPageIndex + 1)
                .coerceAtMost(
                    pages.lastIndex
                )

        val endIndex =
            (
                    currentPageIndex +
                            PRELOAD_DISTANCE
                    ).coerceAtMost(
                    pages.lastIndex
                )

        if (startIndex <= endIndex) {
            for (index in startIndex..endIndex) {
                runCatching {
                    imageDownloader.download(
                        pages[index].imageUrl
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                backgroundColor
            )
    ) {
        when {
            isLoading -> {
                ReaderLoadingContent()
            }

            errorMessage != null -> {
                ReaderErrorContent(
                    message =
                        errorMessage.orEmpty()
                )
            }

            pages.isEmpty() -> {
                Text(
                    text =
                        "Nenhuma página foi encontrada.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = foregroundColor,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                ReaderModeContent(
                    pages = pages,
                    mode = preferences.mode,
                    longStripGapDp =
                        preferences.longStripGapDp,
                    initialPage =
                        currentPageIndex,
                    requestedPage =
                        requestedPage,
                    imageDownloader =
                        imageDownloader,
                    backgroundColor =
                        backgroundColor,
                    onPageChanged = { pageIndex ->
                        currentPageIndex =
                            pageIndex
                    },
                    onRequestedPageConsumed = {
                        requestedPage = null
                    },
                    onEndReached = {
                        if (
                            !automaticChapterChangeStarted
                        ) {
                            val chapterToOpen =
                                nextChapter

                            if (chapterToOpen != null) {
                                automaticChapterChangeStarted =
                                    true

                                progressStore.markViewed(
                                    chapter.id
                                )

                                onChapterSelected(
                                    chapterToOpen
                                )
                            }
                        }
                    },
                    onTap = {
                        controlsVisible =
                            !controlsVisible
                    }
                )
            }
        }

        ReaderControls(
            controlsVisible =
                controlsVisible,
            settingsVisible =
                settingsVisible,
            pagesAvailable =
                pages.isNotEmpty(),
            chapterName =
                chapter.name,
            currentPageIndex =
                currentPageIndex,
            totalPages =
                pages.size,
            showPageNumber =
                preferences.showPageNumber,
            hasPreviousChapter =
                previousChapter != null,
            hasNextChapter =
                nextChapter != null,
            onBackClick =
                onBackClick,
            onPageSelected = { pageIndex ->
                requestedPage =
                    pageIndex
            },
            onPreviousChapterClick = {
                previousChapter?.let {
                        chapterToOpen ->

                    automaticChapterChangeStarted =
                        true

                    onChapterSelected(
                        chapterToOpen
                    )
                }
            },
            onNextChapterClick = {
                nextChapter?.let {
                        chapterToOpen ->

                    automaticChapterChangeStarted =
                        true

                    progressStore.markViewed(
                        chapter.id
                    )

                    onChapterSelected(
                        chapterToOpen
                    )
                }
            },
            onSettingsClick = {
                settingsVisible = true
            }
        )
    }

    if (settingsVisible) {
        ReaderSettingsSheet(
            preferences = preferences,
            onPreferencesChange = {
                    newPreferences ->

                preferences =
                    newPreferences
            },
            onDismiss = {
                settingsVisible = false
            }
        )
    }
}