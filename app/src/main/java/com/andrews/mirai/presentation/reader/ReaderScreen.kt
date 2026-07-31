package com.andrews.mirai.presentation.reader

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import com.andrews.mirai.presentation.reader.components.MiraiReaderImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.runtime.snapshotFlow
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import kotlinx.coroutines.flow.distinctUntilChanged

private const val IMAGE_LOG_TAG = "MIRAI_IMAGE"
private const val PRELOAD_DISTANCE = 3

@Composable
fun ReaderScreen(
    chapter: Chapter,
    onBackClick: () -> Unit
) {
    var pages by remember(chapter.id) {
        mutableStateOf<List<ReaderPage>>(emptyList())
    }

    var isLoading by remember(chapter.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(chapter.id) {
        mutableStateOf<String?>(null)
    }

    var controlsVisible by remember {
        mutableStateOf(true)
    }

    val applicationContext =
        LocalContext.current.applicationContext

    val imageCache = remember(applicationContext) {
        ReaderImageCache(applicationContext)
    }

    val imageDownloader = remember(imageCache) {
        ReaderImageDownloader(imageCache)
    }

    val listState = rememberLazyListState()

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    var positionRestored by remember(chapter.id) {
        mutableStateOf(false)
    }

    val currentPageIndex by remember {
        derivedStateOf {
            if (pages.isEmpty()) {
                0
            } else {
                listState.firstVisibleItemIndex
                    .coerceIn(0, pages.lastIndex)
            }
        }
    }

    val readingProgress by remember {
        derivedStateOf {
            if (pages.isEmpty()) {
                0f
            } else {
                ((currentPageIndex + 1).toFloat() / pages.size.toFloat())
                    .coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(chapter.id) {
        isLoading = true
        errorMessage = null
        pages = emptyList()

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository.currentSource.getPages(chapter)
            }
        }.onSuccess { result ->
            pages = result

            Log.d(
                IMAGE_LOG_TAG,
                "Capítulo: ${chapter.name} | " +
                        "Total de páginas: ${result.size}"
            )

            result.forEach { page ->
                Log.d(
                    IMAGE_LOG_TAG,
                    "Página ${page.index + 1}: ${page.imageUrl}"
                )
            }
        }.onFailure { throwable ->
            errorMessage = throwable.message
                ?: "Não foi possível carregar as páginas."

            Log.e(
                IMAGE_LOG_TAG,
                "Erro ao carregar páginas de ${chapter.name}",
                throwable
            )
        }

        isLoading = false
    }

    LaunchedEffect(
        chapter.id,
        pages
    ) {
        if (
            pages.isNotEmpty() &&
            !positionRestored
        ) {
            val savedPage = progressStore
                .getPage(chapter.id)
                .coerceIn(
                    minimumValue = 0,
                    maximumValue = pages.lastIndex
                )

            listState.scrollToItem(savedPage)
            positionRestored = true
        }
    }

    LaunchedEffect(
        chapter.id,
        positionRestored
    ) {
        if (!positionRestored) {
            return@LaunchedEffect
        }

        snapshotFlow {
            listState.firstVisibleItemIndex
        }
            .distinctUntilChanged()
            .collect { pageIndex ->
                progressStore.savePage(
                    chapterId = chapter.id,
                    pageIndex = pageIndex,
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

        val firstPageToPreload = currentPageIndex + 1

        val lastPageToPreload = (
                currentPageIndex + PRELOAD_DISTANCE
                ).coerceAtMost(pages.lastIndex)

        if (firstPageToPreload > lastPageToPreload) {
            return@LaunchedEffect
        }

        for (
        pageIndex in firstPageToPreload..lastPageToPreload
        ) {
            val page = pages[pageIndex]

            runCatching {
                imageDownloader.download(
                    imageUrl = page.imageUrl
                )
            }.onSuccess { file ->
                Log.d(
                    IMAGE_LOG_TAG,
                    "PRÉ-CARREGADA | " +
                            "Página ${page.index + 1} | " +
                            "${file.length()} bytes"
                )
            }.onFailure { throwable ->
                Log.e(
                    IMAGE_LOG_TAG,
                    "ERRO NO PRÉ-CARREGAMENTO | " +
                            "Página ${page.index + 1}",
                    throwable
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible = !controlsVisible
                    }
                )
            }
    ) {
        when {
            isLoading -> {
                LoadingContent(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            pages.isEmpty() -> {
                Text(
                    text = "Nenhuma página foi encontrada.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = pages,
                        key = { page ->
                            "${page.index}-${page.imageUrl}"
                        }
                    ) { page ->
                        ReaderPageContent(
                            page = page,
                            imageDownloader = imageDownloader
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                chapterName = chapter.name,
                currentPage = currentPageIndex + 1,
                totalPages = pages.size,
                progress = readingProgress,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun ReaderPageContent(
    page: ReaderPage,
    imageDownloader: ReaderImageDownloader
) {
    var imageFile by remember(page.imageUrl) {
        mutableStateOf<File?>(null)
    }

    var imageAspectRatio by remember(page.imageUrl) {
        mutableStateOf<Float?>(null)
    }

    var isDownloading by remember(page.imageUrl) {
        mutableStateOf(true)
    }

    var downloadError by remember(page.imageUrl) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(page.imageUrl) {
        isDownloading = true
        downloadError = null
        imageFile = null
        imageAspectRatio = null

        runCatching {
            val downloadedFile = imageDownloader.download(
                imageUrl = page.imageUrl
            )

            val dimensions = readImageDimensions(
                downloadedFile
            )

            downloadedFile to dimensions
        }.onSuccess { result ->
            val downloadedFile = result.first
            val dimensions = result.second

            imageFile = downloadedFile
            imageAspectRatio = dimensions

            Log.d(
                IMAGE_LOG_TAG,
                "ARQUIVO LOCAL | " +
                        "Página ${page.index + 1} | " +
                        "Tamanho: ${downloadedFile.length()} bytes | " +
                        "Proporção: $dimensions | " +
                        "Arquivo: ${downloadedFile.absolutePath}"
            )
        }.onFailure { throwable ->
            downloadError = throwable.message
                ?: "Não foi possível baixar esta página."

            Log.e(
                IMAGE_LOG_TAG,
                "ERRO NO DOWNLOAD | " +
                        "Página ${page.index + 1} | " +
                        "URL: ${page.imageUrl}",
                throwable
            )
        }

        isDownloading = false
    }

    when {
        isDownloading -> {
            PageLoadingContent(
                pageNumber = page.index + 1
            )
        }

        downloadError != null -> {
            Text(
                text = "Erro na página ${page.index + 1}\n" +
                        downloadError,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .padding(24.dp),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        imageFile != null &&
                imageAspectRatio != null -> {

            MiraiReaderImage(
                imageFileUri = Uri.fromFile(imageFile),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio!!)
            )
        }
    }
}

private suspend fun readImageDimensions(
    imageFile: File
): Float = withContext(Dispatchers.IO) {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    BitmapFactory.decodeFile(
        imageFile.absolutePath,
        options
    )

    val width = options.outWidth
    val height = options.outHeight

    if (width <= 0 || height <= 0) {
        throw IllegalStateException(
            "Não foi possível identificar o tamanho da imagem."
        )
    }

    Log.d(
        IMAGE_LOG_TAG,
        "IMAGEM ORIGINAL | ${width}x${height} | " +
                "Arquivo: ${imageFile.absolutePath}"
    )

    width.toFloat() / height.toFloat()
}

@Composable
private fun PageLoadingContent(
    pageNumber: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Text(
            text = "Baixando página $pageNumber...",
            modifier = Modifier.padding(top = 12.dp),
            color = Color.White
        )
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()

        Text(
            text = "Carregando páginas...",
            modifier = Modifier.padding(top = 12.dp),
            color = Color.White
        )
    }
}

@Composable
private fun ReaderTopBar(
    chapterName: String,
    currentPage: Int,
    totalPages: Int,
    progress: Float,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.78f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                text = chapterName,
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (totalPages > 0) {
                Text(
                    text = "$currentPage / $totalPages",
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (totalPages > 0) {
            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}