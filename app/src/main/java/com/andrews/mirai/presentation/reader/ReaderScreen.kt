package com.andrews.mirai.presentation.reader

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderErrorContent
import com.andrews.mirai.presentation.reader.components.ReaderLoadingContent
import com.andrews.mirai.presentation.reader.logic.ReaderChapterController
import com.andrews.mirai.presentation.reader.logic.ReaderChapterNavigation
import com.andrews.mirai.presentation.reader.logic.ReaderChapterPageLoader
import com.andrews.mirai.presentation.reader.logic.ReaderLongStripMapper
import com.andrews.mirai.presentation.reader.logic.ReaderPageProvider
import com.andrews.mirai.presentation.reader.logic.ReaderPreloader
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.settings.ReaderBackground
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsSheet
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import com.andrews.mirai.presentation.reader.state.ReaderChapterPagesState
import com.andrews.mirai.presentation.reader.state.ReaderScreenState
import kotlinx.coroutines.launch

private const val PRELOAD_DISTANCE =
    6

private const val NEXT_CHAPTER_PRELOAD_PAGES =
    2

@Composable
fun ReaderScreen(
    chapter: Chapter,
    chapters: List<Chapter>,
    onChapterSelected: (Chapter) -> Unit,
    onBackClick: () -> Unit
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val sourceId =
        SourceRepository.currentSource.id

    val coroutineScope =
        rememberCoroutineScope()

    val orderedChapters =
        remember(chapters) {
            ReaderChapterNavigation
                .orderedChapters(
                    chapters
                )
        }

    val progressStore =
        remember(applicationContext) {
            ReadingProgressStore(
                applicationContext
            )
        }

    val settingsStore =
        remember(applicationContext) {
            ReaderSettingsStore(
                applicationContext
            )
        }

    val imageCache =
        remember(applicationContext) {
            ReaderImageCache(
                applicationContext
            )
        }

    val imageDownloader =
        remember(imageCache) {
            ReaderImageDownloader(
                imageCache
            )
        }

    val pageProvider =
        remember(
            applicationContext,
            sourceId
        ) {
            ReaderPageProvider(
                context = applicationContext,
                sourceId = sourceId
            )
        }

    val chapterPageLoader =
        remember(pageProvider) {
            ReaderChapterPageLoader(
                pageProvider
            )
        }

    val screenState =
        remember(
            chapter.mangaId
        ) {
            ReaderScreenState(
                initialChapter = chapter,
                initialPageIndex =
                    progressStore.getPage(
                        chapter.id
                    )
            )
        }

    val chapterController =
        remember(
            screenState,
            chapterPageLoader,
            progressStore
        ) {
            ReaderChapterController(
                screenState = screenState,
                pageLoader = chapterPageLoader,
                progressStore = progressStore
            )
        }

    val preloader =
        remember(imageDownloader) {
            ReaderPreloader(
                imageDownloader
            )
        }

    var preferences by remember {
        mutableStateOf(
            settingsStore.load()
        )
    }

    val activeChapter =
        screenState.activeChapter

    val previousChapter =
        ReaderChapterNavigation
            .previousChapter(
                chapters = orderedChapters,
                chapter = activeChapter
            )

    val nextChapter =
        ReaderChapterNavigation
            .nextChapter(
                chapters = orderedChapters,
                chapter = activeChapter
            )

    val activeState =
        screenState.getChapterState(
            activeChapter.id
        )
            ?: ReaderChapterPagesState(
                isLoading = true
            )

    val activePages =
        activeState.pages

    val currentPageIndex =
        screenState.currentPageIndex(
            chapterId = activeChapter.id,
            pages = activePages
        )

    val isLongStripMode =
        preferences.mode ==
                ReaderMode.LONG_STRIP ||
                preferences.mode ==
                ReaderMode.LONG_STRIP_GAPS

    val longStripState =
        remember(
            orderedChapters,
            screenState.chapterStates,
            activeChapter
        ) {
            ReaderLongStripMapper
                .createState(
                    orderedChapters =
                        orderedChapters,
                    chapterStates =
                        screenState.chapterStates,
                    activeChapter =
                        activeChapter
                )
        }

    val backgroundColor =
        when (
            preferences.background
        ) {
            ReaderBackground.BLACK -> {
                Color.Black
            }

            ReaderBackground.GRAY -> {
                Color(
                    0xFF444444
                )
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
        if (screenState.settingsVisible) {
            screenState.settingsVisible = false
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(
        preferences
    ) {
        settingsStore.save(
            preferences
        )
    }

    LaunchedEffect(
        activeChapter.id
    ) {
        chapterController
            .loadChapterPages(
                activeChapter
            )
    }

    LaunchedEffect(
        activeChapter.id,
        orderedChapters
    ) {
        previousChapter?.let { chapter ->
            chapterController
                .loadChapterPages(
                    chapter
                )
        }

        nextChapter?.let { chapter ->
            chapterController
                .loadChapterPages(
                    chapter
                )
        }
    }

    LaunchedEffect(
        activeChapter.id,
        currentPageIndex,
        activePages.size
    ) {
        if (activePages.isNotEmpty()) {
            progressStore.savePage(
                chapterId =
                    activeChapter.id,
                pageIndex =
                    currentPageIndex,
                totalPages =
                    activePages.size
            )
        }
    }

    LaunchedEffect(
        activePages,
        currentPageIndex,
        nextChapter,
        screenState.chapterStates,
        preloader
    ) {
        preloader.preloadCurrentPages(
            pages = activePages,
            currentPageIndex =
                currentPageIndex,
            preloadDistance =
                PRELOAD_DISTANCE
        )

        val nextChapterPages =
            nextChapter
                ?.let { chapter ->
                    screenState
                        .getChapterState(
                            chapter.id
                        )
                        ?.pages
                }
                .orEmpty()

        preloader.preloadNextChapter(
            pages = nextChapterPages,
            maximumPages =
                NEXT_CHAPTER_PRELOAD_PAGES
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    backgroundColor
                )
    ) {
        when {
            activeState.isLoading -> {
                ReaderLoadingContent()
            }

            activeState.errorMessage != null -> {
                ReaderErrorContent(
                    message =
                        activeState
                            .errorMessage
                )
            }

            activePages.isEmpty() -> {
                Text(
                    text =
                        "Nenhuma página foi encontrada.",
                    modifier =
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .padding(
                                24.dp
                            ),
                    color =
                        foregroundColor,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    textAlign =
                        TextAlign.Center
                )
            }

            else -> {
                ReaderModeContent(
                    activeChapter =
                        activeChapter,
                    pages =
                        activePages,
                    longStripSections =
                        longStripState.sections,
                    mode =
                        preferences.mode,
                    longStripGapDp =
                        preferences.longStripGapDp,
                    initialPage =
                        currentPageIndex,
                    requestedChapterId =
                        screenState
                            .requestedChapterId,
                    requestedPage =
                        screenState.requestedPage,
                    showFinalCompletion =
                        longStripState
                            .showFinalCompletion,
                    imageDownloader =
                        imageDownloader,
                    backgroundColor =
                        backgroundColor,
                    onPositionChanged = {
                            visibleChapter,
                            pageIndex ->

                        screenState.updatePage(
                            chapterId =
                                visibleChapter.id,
                            pageIndex =
                                pageIndex
                        )

                        if (
                            visibleChapter.id !=
                            screenState
                                .activeChapter
                                .id
                        ) {
                            if (
                                visibleChapter.number >
                                screenState
                                    .activeChapter
                                    .number
                            ) {
                                progressStore
                                    .markViewed(
                                        screenState
                                            .activeChapter
                                            .id
                                    )
                            }

                            screenState.activeChapter =
                                visibleChapter

                            onChapterSelected(
                                visibleChapter
                            )
                        }
                    },
                    onRequestedPageConsumed = {
                        screenState
                            .consumeRequestedPage()
                    },
                    onTap = {
                        screenState.toggleControls()
                    }
                )
            }
        }

        ReaderControls(
            controlsVisible =
                screenState.controlsVisible,
            settingsVisible =
                screenState.settingsVisible,
            pagesAvailable =
                activePages.isNotEmpty(),
            chapterName =
                activeChapter.name,
            currentPageIndex =
                currentPageIndex,
            totalPages =
                activePages.size,
            showPageNumber =
                preferences.showPageNumber,
            hasPreviousChapter =
                previousChapter != null,
            hasNextChapter =
                nextChapter != null,
            onBackClick =
                onBackClick,
            onPageSelected = { pageIndex ->
                screenState.requestPage(
                    chapterId =
                        activeChapter.id,
                    pageIndex =
                        pageIndex
                )
            },
            onPreviousChapterClick = {
                previousChapter?.let {
                        targetChapter ->

                    coroutineScope.launch {
                        chapterController
                            .openChapterFromControls(
                                targetChapter =
                                    targetChapter,
                                targetPage =
                                    progressStore
                                        .getPage(
                                            targetChapter.id
                                        ),
                                isLongStripMode =
                                    isLongStripMode,
                                onChapterSelected =
                                    onChapterSelected
                            )
                    }
                }
            },
            onNextChapterClick = {
                nextChapter?.let {
                        targetChapter ->

                    progressStore.markViewed(
                        activeChapter.id
                    )

                    coroutineScope.launch {
                        chapterController
                            .openChapterFromControls(
                                targetChapter =
                                    targetChapter,
                                targetPage =
                                    progressStore
                                        .getPage(
                                            targetChapter.id
                                        ),
                                isLongStripMode =
                                    isLongStripMode,
                                onChapterSelected =
                                    onChapterSelected
                            )
                    }
                }
            },
            onSettingsClick = {
                screenState.settingsVisible =
                    true
            }
        )
    }

    if (screenState.settingsVisible) {
        ReaderSettingsSheet(
            preferences =
                preferences,
            onPreferencesChange = {
                    newPreferences ->

                preferences =
                    newPreferences
            },
            onDismiss = {
                screenState.settingsVisible =
                    false
            }
        )
    }
}