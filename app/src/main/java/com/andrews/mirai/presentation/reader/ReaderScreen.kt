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
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderErrorContent
import com.andrews.mirai.presentation.reader.components.ReaderLoadingContent
import com.andrews.mirai.presentation.reader.mode.LongStripChapterSection
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.settings.ReaderBackground
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsSheet
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val IMAGE_LOG_TAG = "MIRAI_IMAGE"
private const val PRELOAD_DISTANCE = 6
private const val NEXT_CHAPTER_PRELOAD_PAGES = 2

private data class ChapterPagesState(
    val pages: List<ReaderPage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun ReaderScreen(
    chapter: Chapter,
    chapters: List<Chapter>,
    onChapterSelected: (Chapter) -> Unit,
    onBackClick: () -> Unit
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val coroutineScope =
        rememberCoroutineScope()

    val orderedChapters = remember(chapters) {
        chapters.sortedBy { item ->
            item.number
        }
    }

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
        mutableStateOf(settingsStore.load())
    }

    var activeChapter by remember(chapter.mangaId) {
        mutableStateOf(chapter)
    }

    var chapterStates by remember(chapter.mangaId) {
        mutableStateOf<Map<String, ChapterPagesState>>(
            emptyMap()
        )
    }

    var pageByChapter by remember(chapter.mangaId) {
        mutableStateOf(
            mapOf(
                chapter.id to progressStore.getPage(
                    chapter.id
                )
            )
        )
    }

    var controlsVisible by remember(chapter.mangaId) {
        mutableStateOf(true)
    }

    var settingsVisible by remember {
        mutableStateOf(false)
    }

    var requestedChapterId by remember(chapter.mangaId) {
        mutableStateOf<String?>(null)
    }

    var requestedPage by remember(chapter.mangaId) {
        mutableStateOf<Int?>(null)
    }

    suspend fun loadChapterPages(
        targetChapter: Chapter
    ) {
        val existingState =
            chapterStates[targetChapter.id]

        if (
            existingState?.isLoading == true ||
            existingState?.pages?.isNotEmpty() == true
        ) {
            return
        }

        chapterStates = chapterStates + (
                targetChapter.id to ChapterPagesState(
                    isLoading = true
                )
                )

        try {
            val result = withContext(Dispatchers.IO) {
                SourceRepository
                    .currentSource
                    .getPages(
                        chapter = targetChapter
                    )
            }

            chapterStates = chapterStates + (
                    targetChapter.id to ChapterPagesState(
                        pages = result
                    )
                    )

            val savedPage =
                pageByChapter[targetChapter.id]
                    ?: progressStore.getPage(
                        targetChapter.id
                    )

            val safePage = savedPage.coerceIn(
                minimumValue = 0,
                maximumValue =
                    result.lastIndex.coerceAtLeast(0)
            )

            pageByChapter = pageByChapter + (
                    targetChapter.id to safePage
                    )

            Log.d(
                IMAGE_LOG_TAG,
                "${targetChapter.name}: ${result.size} páginas"
            )
        } catch (exception: CancellationException) {
            /*
             * O Compose pode cancelar o carregamento quando a lista
             * de capítulos é atualizada. Isso não é um erro real.
             */
            chapterStates =
                chapterStates - targetChapter.id

            throw exception
        } catch (throwable: Throwable) {
            chapterStates = chapterStates + (
                    targetChapter.id to ChapterPagesState(
                        errorMessage =
                            throwable.message
                                ?: "Não foi possível carregar as páginas."
                    )
                    )

            Log.e(
                IMAGE_LOG_TAG,
                "Erro ao carregar ${targetChapter.name}",
                throwable
            )
        }
    }

    fun chapterIndex(
        targetChapter: Chapter
    ): Int {
        return orderedChapters.indexOfFirst { item ->
            item.id == targetChapter.id
        }
    }

    fun previousOf(
        targetChapter: Chapter
    ): Chapter? {
        val index = chapterIndex(targetChapter)

        return if (index > 0) {
            orderedChapters.getOrNull(index - 1)
        } else {
            null
        }
    }

    fun nextOf(
        targetChapter: Chapter
    ): Chapter? {
        val index = chapterIndex(targetChapter)

        return if (index >= 0) {
            orderedChapters.getOrNull(index + 1)
        } else {
            null
        }
    }

    val previousChapter =
        previousOf(activeChapter)

    val nextChapter =
        nextOf(activeChapter)

    val activeState =
        chapterStates[activeChapter.id]
            ?: ChapterPagesState(
                isLoading = true
            )

    val activePages =
        activeState.pages

    val currentPageIndex =
        (pageByChapter[activeChapter.id] ?: 0)
            .coerceIn(
                minimumValue = 0,
                maximumValue =
                    activePages.lastIndex.coerceAtLeast(0)
            )

    val isLongStripMode =
        preferences.mode == ReaderMode.LONG_STRIP ||
                preferences.mode == ReaderMode.LONG_STRIP_GAPS

    val longStripSections = remember(
        orderedChapters,
        chapterStates,
        activeChapter
    ) {
        val knownChapters =
            if (orderedChapters.isEmpty()) {
                listOf(activeChapter)
            } else {
                orderedChapters
            }

        knownChapters.mapNotNull { item ->
            chapterStates[item.id]?.let { state ->
                LongStripChapterSection(
                    chapter = item,
                    pages = state.pages,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage
                )
            }
        }
    }

    val lastLoadedSectionChapter =
        longStripSections.lastOrNull()?.chapter

    val showFinalCompletion =
        lastLoadedSectionChapter != null &&
                (
                        orderedChapters.isEmpty() ||
                                orderedChapters.lastOrNull()?.id ==
                                lastLoadedSectionChapter.id
                        )

    val backgroundColor = when (
        preferences.background
    ) {
        ReaderBackground.BLACK ->
            Color.Black

        ReaderBackground.GRAY ->
            Color(0xFF444444)

        ReaderBackground.WHITE ->
            Color.White
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
        settingsStore.save(preferences)
    }

    /*
     * Carrega somente o capítulo que está sendo exibido.
     * A mudança da lista de capítulos não reinicia este carregamento.
     */
    LaunchedEffect(
        activeChapter.id
    ) {
        loadChapterPages(
            activeChapter
        )
    }

    /*
     * Quando a lista completa chega pelo Continue Lendo,
     * carrega os capítulos anterior e seguinte separadamente.
     */
    LaunchedEffect(
        activeChapter.id,
        orderedChapters
    ) {
        previousOf(
            activeChapter
        )?.let { previous ->
            loadChapterPages(previous)
        }

        nextOf(
            activeChapter
        )?.let { next ->
            loadChapterPages(next)
        }
    }

    LaunchedEffect(
        activeChapter.id,
        currentPageIndex,
        activePages.size
    ) {
        if (activePages.isNotEmpty()) {
            progressStore.savePage(
                chapterId = activeChapter.id,
                pageIndex = currentPageIndex,
                totalPages = activePages.size
            )
        }
    }

    LaunchedEffect(
        activePages,
        currentPageIndex,
        nextChapter,
        chapterStates,
        imageDownloader
    ) {
        if (activePages.isNotEmpty()) {
            val endIndex =
                (
                        currentPageIndex +
                                PRELOAD_DISTANCE
                        ).coerceAtMost(
                        activePages.lastIndex
                    )

            for (index in currentPageIndex..endIndex) {
                try {
                    imageDownloader.downloadWithInfo(
                        activePages[index].imageUrl
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Throwable) {
                    /*
                     * Falha no pré-carregamento não deve
                     * interromper o leitor.
                     */
                }
            }
        }

        val nextPages =
            nextChapter
                ?.let { item ->
                    chapterStates[item.id]?.pages
                }
                .orEmpty()

        nextPages
            .take(NEXT_CHAPTER_PRELOAD_PAGES)
            .forEach { page ->
                try {
                    imageDownloader.downloadWithInfo(
                        page.imageUrl
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Throwable) {
                    // Ignora apenas falhas do pré-carregamento.
                }
            }
    }

    fun openChapterFromControls(
        targetChapter: Chapter,
        targetPage: Int
    ) {
        coroutineScope.launch {
            loadChapterPages(
                targetChapter
            )

            val pages =
                chapterStates[targetChapter.id]
                    ?.pages
                    .orEmpty()

            val safeTargetPage =
                targetPage.coerceIn(
                    minimumValue = 0,
                    maximumValue =
                        pages.lastIndex.coerceAtLeast(0)
                )

            pageByChapter = pageByChapter + (
                    targetChapter.id to safeTargetPage
                    )

            if (isLongStripMode) {
                requestedChapterId =
                    targetChapter.id

                requestedPage =
                    safeTargetPage
            } else {
                activeChapter =
                    targetChapter

                requestedChapterId =
                    targetChapter.id

                requestedPage =
                    safeTargetPage

                onChapterSelected(
                    targetChapter
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when {
            activeState.isLoading -> {
                ReaderLoadingContent()
            }

            activeState.errorMessage != null -> {
                ReaderErrorContent(
                    message =
                        activeState.errorMessage
                )
            }

            activePages.isEmpty() -> {
                Text(
                    text =
                        "Nenhuma página foi encontrada.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = foregroundColor,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                ReaderModeContent(
                    activeChapter =
                        activeChapter,
                    pages =
                        activePages,
                    longStripSections =
                        longStripSections,
                    mode =
                        preferences.mode,
                    longStripGapDp =
                        preferences.longStripGapDp,
                    initialPage =
                        currentPageIndex,
                    requestedChapterId =
                        requestedChapterId,
                    requestedPage =
                        requestedPage,
                    showFinalCompletion =
                        showFinalCompletion,
                    imageDownloader =
                        imageDownloader,
                    backgroundColor =
                        backgroundColor,
                    onPositionChanged = {
                            visibleChapter,
                            pageIndex ->

                        pageByChapter = pageByChapter + (
                                visibleChapter.id to pageIndex
                                )

                        if (
                            visibleChapter.id !=
                            activeChapter.id
                        ) {
                            if (
                                visibleChapter.number >
                                activeChapter.number
                            ) {
                                progressStore.markViewed(
                                    activeChapter.id
                                )
                            }

                            activeChapter =
                                visibleChapter

                            onChapterSelected(
                                visibleChapter
                            )
                        }
                    },
                    onRequestedPageConsumed = {
                        requestedChapterId = null
                        requestedPage = null
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
                requestedChapterId =
                    activeChapter.id

                requestedPage =
                    pageIndex
            },
            onPreviousChapterClick = {
                previousChapter?.let { targetChapter ->
                    openChapterFromControls(
                        targetChapter =
                            targetChapter,
                        targetPage =
                            progressStore.getPage(
                                targetChapter.id
                            )
                    )
                }
            },
            onNextChapterClick = {
                nextChapter?.let { targetChapter ->
                    progressStore.markViewed(
                        activeChapter.id
                    )

                    openChapterFromControls(
                        targetChapter =
                            targetChapter,
                        targetPage =
                            progressStore.getPage(
                                targetChapter.id
                            )
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
            preferences =
                preferences,
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