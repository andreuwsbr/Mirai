package com.andrews.mirai.presentation.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.ReaderBottomBar
import com.andrews.mirai.presentation.reader.components.ReaderErrorContent
import com.andrews.mirai.presentation.reader.components.ReaderLoadingContent
import com.andrews.mirai.presentation.reader.components.ReaderTopBar
import com.andrews.mirai.presentation.reader.mode.HorizontalPagedReader
import com.andrews.mirai.presentation.reader.mode.LongStripReader
import com.andrews.mirai.presentation.reader.mode.VerticalPagedReader
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.settings.ReaderBackground
import com.andrews.mirai.presentation.reader.settings.ReaderMode
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsSheet
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val IMAGE_LOG_TAG = "MIRAI_IMAGE"
private const val PRELOAD_DISTANCE = 3

@Composable
fun ReaderScreen(
    chapter: Chapter,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val view = LocalView.current

    val activity = remember(context) {
        context.findActivity()
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

    var pages by remember(chapter.id) {
        mutableStateOf<List<ReaderPage>>(emptyList())
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
            progressStore.getPage(chapter.id)
        )
    }

    var requestedPage by remember {
        mutableStateOf<Int?>(null)
    }

    val backgroundColor = when (preferences.background) {
        ReaderBackground.BLACK -> Color.Black
        ReaderBackground.GRAY -> Color(0xFF444444)
        ReaderBackground.WHITE -> Color.White
    }

    val foregroundColor =
        if (preferences.background == ReaderBackground.WHITE) {
            Color.Black
        } else {
            Color.White
        }

    DisposableEffect(
        preferences.keepScreenOn,
        view
    ) {
        val previousKeepScreenOn = view.keepScreenOn

        view.keepScreenOn =
            preferences.keepScreenOn

        onDispose {
            view.keepScreenOn =
                previousKeepScreenOn
        }
    }

    DisposableEffect(
        preferences.fullscreen,
        activity
    ) {
        val window = activity?.window

        if (window != null) {
            val controller =
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                )

            if (preferences.fullscreen) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    false
                )

                controller.hide(
                    WindowInsetsCompat.Type.systemBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )

                controller.show(
                    WindowInsetsCompat.Type.systemBars()
                )
            }
        }

        onDispose {
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )

                WindowCompat
                    .getInsetsController(
                        window,
                        window.decorView
                    )
                    .show(
                        WindowInsetsCompat.Type.systemBars()
                    )
            }
        }
    }

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

    LaunchedEffect(chapter.id) {
        isLoading = true
        errorMessage = null
        pages = emptyList()

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository.currentSource.getPages(
                    chapter = chapter
                )
            }
        }.onSuccess { result ->
            pages = result

            currentPageIndex =
                currentPageIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue =
                        result.lastIndex.coerceAtLeast(0)
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
                        maximumValue = pages.lastIndex
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
                .coerceAtMost(pages.lastIndex)

        val endIndex =
            (currentPageIndex + PRELOAD_DISTANCE)
                .coerceAtMost(pages.lastIndex)

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
            .background(backgroundColor)
    ) {
        when {
            isLoading -> {
                ReaderLoadingContent()
            }

            errorMessage != null -> {
                ReaderErrorContent(
                    message = errorMessage!!
                )
            }

            pages.isEmpty() -> {
                Text(
                    text = "Nenhuma página foi encontrada.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = foregroundColor,
                    style = MaterialTheme.typography.bodyLarge,
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
                        currentPageIndex = pageIndex
                    },
                    onRequestedPageConsumed = {
                        requestedPage = null
                    },
                    onTap = {
                        controlsVisible =
                            !controlsVisible
                    }
                )
            }
        }

        AnimatedVisibility(
            visible =
                controlsVisible &&
                        !settingsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                Alignment.TopCenter
            )
        ) {
            ReaderTopBar(
                chapterName = chapter.name,
                currentPage =
                    currentPageIndex + 1,
                totalPages = pages.size,
                showPageNumber =
                    preferences.showPageNumber,
                onBackClick = onBackClick
            )
        }

        AnimatedVisibility(
            visible =
                controlsVisible &&
                        !settingsVisible &&
                        pages.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                Alignment.BottomCenter
            )
        ) {
            ReaderBottomBar(
                currentPage =
                    currentPageIndex,
                totalPages = pages.size,
                onPageSelected = { pageIndex ->
                    requestedPage = pageIndex
                },
                onSettingsClick = {
                    settingsVisible = true
                }
            )
        }
    }

    if (settingsVisible) {
        ReaderSettingsSheet(
            preferences = preferences,
            onPreferencesChange = { newPreferences ->
                preferences = newPreferences
            },
            onDismiss = {
                settingsVisible = false
            }
        )
    }
}

@Composable
private fun ReaderModeContent(
    pages: List<ReaderPage>,
    mode: ReaderMode,
    longStripGapDp: Int,
    initialPage: Int,
    requestedPage: Int?,
    imageDownloader: ReaderImageDownloader,
    backgroundColor: Color,
    onPageChanged: (Int) -> Unit,
    onRequestedPageConsumed: () -> Unit,
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

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this

        is ContextWrapper -> {
            baseContext.findActivity()
        }

        else -> null
    }
}