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
import com.andrews.mirai.presentation.reader.components.ReaderChapterErrorContent
import com.andrews.mirai.presentation.reader.components.ReaderLoadingContent
import com.andrews.mirai.presentation.reader.display.ReaderBrightnessEffect
import com.andrews.mirai.presentation.reader.display.ReaderOrientationEffect
import com.andrews.mirai.presentation.reader.logic.ReaderChapterController
import com.andrews.mirai.presentation.reader.logic.ReaderChapterPageLoader
import com.andrews.mirai.presentation.reader.logic.ReaderLongStripMapper
import com.andrews.mirai.presentation.reader.logic.ReaderPageProvider
import com.andrews.mirai.presentation.reader.logic.ReaderPreloader
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.retry.ReaderRetryController
import com.andrews.mirai.presentation.reader.settings.ReaderBackground
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsSheet
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import com.andrews.mirai.presentation.reader.state.ReaderChapterPagesState
import com.andrews.mirai.presentation.reader.state.ReaderScreenState
import com.andrews.mirai.presentation.reader.state.ReaderUiState
import kotlinx.coroutines.launch

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

    val progressStore =
        remember(
            applicationContext
        ) {
            ReadingProgressStore(
                applicationContext
            )
        }

    val settingsStore =
        remember(
            applicationContext
        ) {
            ReaderSettingsStore(
                applicationContext
            )
        }

    val imageCache =
        remember(
            applicationContext
        ) {
            ReaderImageCache(
                applicationContext
            )
        }

    val imageDownloader =
        remember(
            imageCache
        ) {
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
                context =
                    applicationContext,
                sourceId =
                    sourceId
            )
        }

    val chapterPageLoader =
        remember(
            pageProvider
        ) {
            ReaderChapterPageLoader(
                pageProvider
            )
        }

    val screenState =
        remember(
            chapter.mangaId,
            sourceId
        ) {
            ReaderScreenState(
                initialChapter =
                    chapter,
                initialPageIndex =
                    progressStore.getPage(
                        chapterId =
                            chapter.id,
                        sourceId =
                            sourceId
                    )
            )
        }

    val readerViewModel =
        remember(
            chapter.mangaId,
            sourceId
        ) {
            ReaderViewModel(
                initialChapter =
                    chapter,
                initialChapters =
                    chapters
            )
        }

    val chapterController =
        remember(
            screenState,
            chapterPageLoader,
            progressStore
        ) {
            ReaderChapterController(
                screenState =
                    screenState,
                pageLoader =
                    chapterPageLoader,
                progressStore =
                    progressStore
            )
        }

    val retryController =
        remember(
            chapterController,
            imageCache
        ) {
            ReaderRetryController(
                chapterController =
                    chapterController,
                imageCache =
                    imageCache
            )
        }

    val preloader =
        remember(
            imageDownloader
        ) {
            ReaderPreloader(
                imageDownloader
            )
        }

    var preferences by remember {
        mutableStateOf(
            settingsStore.load()
        )
    }

    var chapterRetrying by remember(
        screenState.activeChapter.id
    ) {
        mutableStateOf(
            false
        )
    }

    /*
     * Atualiza a sessão quando a lista completa
     * chegar da fonte depois de abrir pela Home
     * ou pelo Histórico.
     */
    LaunchedEffect(
        chapters,
        chapter.id
    ) {
        readerViewModel.updateChapters(
            chapters =
                chapters,
            activeChapter =
                screenState.activeChapter
        )
    }

    val activeChapter =
        screenState.activeChapter

    LaunchedEffect(
        activeChapter.id
    ) {
        readerViewModel.selectChapter(
            activeChapter
        )
    }

    val orderedChapters =
        readerViewModel
            .orderedChapters

    val previousChapter =
        readerViewModel
            .previousChapter

    val nextChapter =
        readerViewModel
            .nextChapter

    val activeState =
        screenState.getChapterState(
            activeChapter.id
        )
            ?: ReaderChapterPagesState(
                isLoading =
                    true
            )

    val activePages =
        activeState.pages

    val currentPageIndex =
        screenState.currentPageIndex(
            chapterId =
                activeChapter.id,
            pages =
                activePages
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
                        screenState
                            .chapterStates,
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

    val uiState =
        ReaderUiState(
            activeChapter =
                activeChapter,
            previousChapter =
                previousChapter,
            nextChapter =
                nextChapter,
            currentPageIndex =
                currentPageIndex,
            totalPages =
                activePages.size,
            isLoading =
                activeState.isLoading,
            errorMessage =
                activeState.errorMessage,
            controlsVisible =
                screenState.controlsVisible,
            settingsVisible =
                screenState.settingsVisible
        )

    ReaderSystemUiEffects(
        keepScreenOn =
            preferences.keepScreenOn,
        fullscreen =
            preferences.fullscreen
    )

    ReaderOrientationEffect(
        orientationMode =
            preferences.orientationMode
    )

    ReaderBrightnessEffect(
        brightnessPercent =
            preferences.brightnessPercent
    )

    BackHandler {
        if (
            screenState.settingsVisible
        ) {
            screenState.settingsVisible =
                false
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

    /*
     * Mantém capítulo anterior e próximo prontos
     * para troca rápida.
     */
    LaunchedEffect(
        activeChapter.id,
        orderedChapters
    ) {
        previousChapter
            ?.let { targetChapter ->
                chapterController
                    .loadChapterPages(
                        targetChapter
                    )
            }

        nextChapter
            ?.let { targetChapter ->
                chapterController
                    .loadChapterPages(
                        targetChapter
                    )
            }
    }

    LaunchedEffect(
        activeChapter.id,
        currentPageIndex,
        activePages.size
    ) {
        if (
            activePages.isNotEmpty()
        ) {
            progressStore.savePage(
                chapterId =
                    activeChapter.id,
                pageIndex =
                    currentPageIndex,
                totalPages =
                    activePages.size,
                sourceId =
                    sourceId
            )
        }
    }

    LaunchedEffect(
        activePages,
        currentPageIndex,
        nextChapter,
        screenState.chapterStates,
        preferences.preloadMode
    ) {
        preloader.preloadCurrentPages(
            pages =
                activePages,
            currentPageIndex =
                currentPageIndex,
            preloadDistance =
                preferences
                    .preloadMode
                    .currentChapterPages
        )

        val nextChapterPages =
            nextChapter
                ?.let { targetChapter ->
                    screenState
                        .getChapterState(
                            targetChapter.id
                        )
                        ?.pages
                }
                .orEmpty()

        preloader.preloadNextChapter(
            pages =
                nextChapterPages,
            maximumPages =
                preferences
                    .preloadMode
                    .nextChapterPages
        )
    }

    fun openPreviousChapterFromBoundary() {
        val targetChapter =
            previousChapter
                ?: return

        coroutineScope.launch {
            chapterController
                .openChapterFromControls(
                    targetChapter =
                        targetChapter,

                    /*
                     * Int.MAX_VALUE será limitado para
                     * a última página pelo controlador.
                     */
                    targetPage =
                        Int.MAX_VALUE,

                    isLongStripMode =
                        false,

                    onChapterSelected =
                        onChapterSelected
                )
        }
    }

    fun openNextChapterFromBoundary() {
        val targetChapter =
            nextChapter
                ?: return

        progressStore.markViewed(
            chapterId =
                activeChapter.id,
            sourceId =
                sourceId
        )

        coroutineScope.launch {
            chapterController
                .openChapterFromControls(
                    targetChapter =
                        targetChapter,
                    targetPage =
                        0,
                    isLongStripMode =
                        false,
                    onChapterSelected =
                        onChapterSelected
                )
        }
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
            uiState.isLoading -> {
                ReaderLoadingContent()
            }

            uiState.errorMessage != null -> {
                ReaderChapterErrorContent(
                    message =
                        uiState.errorMessage,
                    foregroundColor =
                        foregroundColor,
                    retrying =
                        chapterRetrying,
                    onRetryClick = {
                        if (
                            chapterRetrying
                        ) {
                            return@ReaderChapterErrorContent
                        }

                        coroutineScope.launch {
                            chapterRetrying =
                                true

                            retryController
                                .retryChapter(
                                    activeChapter
                                )

                            chapterRetrying =
                                false
                        }
                    }
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
                    tapMode =
                        preferences.tapMode,
                    tapZoneSize =
                        preferences.tapZoneSize,
                    initialPage =
                        currentPageIndex,
                    requestedChapterId =
                        screenState
                            .requestedChapterId,
                    requestedPage =
                        screenState
                            .requestedPage,
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
                            val previousActiveChapter =
                                screenState
                                    .activeChapter

                            if (
                                readerViewModel
                                    .isForwardMovement(
                                        fromChapter =
                                            previousActiveChapter,
                                        toChapter =
                                            visibleChapter
                                    )
                            ) {
                                progressStore
                                    .markViewed(
                                        chapterId =
                                            previousActiveChapter
                                                .id,
                                        sourceId =
                                            sourceId
                                    )
                            }

                            screenState.activeChapter =
                                visibleChapter

                            readerViewModel.selectChapter(
                                visibleChapter
                            )

                            onChapterSelected(
                                visibleChapter
                            )
                        }
                    },
                    onPreviousChapterRequested = {
                        openPreviousChapterFromBoundary()
                    },
                    onNextChapterRequested = {
                        openNextChapterFromBoundary()
                    },
                    onRequestedPageConsumed = {
                        screenState
                            .consumeRequestedPage()
                    },
                    onTap = {
                        screenState
                            .toggleControls()
                    }
                )
            }
        }

        ReaderControls(
            controlsVisible =
                uiState.controlsVisible,
            settingsVisible =
                uiState.settingsVisible,
            pagesAvailable =
                uiState.hasPages,
            chapterName =
                uiState.activeChapter.name,
            currentPageIndex =
                uiState.currentPageIndex,
            totalPages =
                uiState.totalPages,
            showPageNumber =
                preferences.showPageNumber,
            hasPreviousChapter =
                uiState.hasPreviousChapter,
            hasNextChapter =
                uiState.hasNextChapter,
            onBackClick =
                onBackClick,
            onPageSelected = {
                    pageIndex ->

                screenState.requestPage(
                    chapterId =
                        activeChapter.id,
                    pageIndex =
                        pageIndex
                )
            },
            onPreviousChapterClick = {
                previousChapter
                    ?.let { targetChapter ->
                        coroutineScope.launch {
                            chapterController
                                .openChapterFromControls(
                                    targetChapter =
                                        targetChapter,
                                    targetPage =
                                        progressStore
                                            .getPage(
                                                chapterId =
                                                    targetChapter
                                                        .id,
                                                sourceId =
                                                    sourceId
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
                nextChapter
                    ?.let { targetChapter ->
                        progressStore.markViewed(
                            chapterId =
                                activeChapter.id,
                            sourceId =
                                sourceId
                        )

                        coroutineScope.launch {
                            chapterController
                                .openChapterFromControls(
                                    targetChapter =
                                        targetChapter,
                                    targetPage =
                                        progressStore
                                            .getPage(
                                                chapterId =
                                                    targetChapter
                                                        .id,
                                                sourceId =
                                                    sourceId
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

    if (
        screenState.settingsVisible
    ) {
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