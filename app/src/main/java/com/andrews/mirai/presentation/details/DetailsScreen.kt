package com.andrews.mirai.presentation.details

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrews.mirai.data.download.ChapterDownloadManager
import com.andrews.mirai.data.download.ChapterDownloadRequest
import com.andrews.mirai.data.download.DownloadRepository
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetailsScreen(
    manga: Manga,
    onBackClick: () -> Unit,
    onChapterClick: (
        chapter: Chapter,
        chapters: List<Chapter>
    ) -> Unit
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val sourceId =
        SourceRepository.currentSource.id

    val progressStore =
        remember(applicationContext) {
            ReadingProgressStore(
                applicationContext
            )
        }

    val downloadRepository =
        remember(applicationContext) {
            DownloadRepository(
                applicationContext
            )
        }

    val downloadManager =
        remember(applicationContext) {
            ChapterDownloadManager(
                applicationContext
            )
        }

    val downloadedChapters by
    downloadRepository
        .observeDownloadedChapters(
            sourceId = sourceId,
            mangaId = manga.id
        )
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    val downloadedChaptersById =
        downloadedChapters.associateBy { chapter ->
            chapter.chapterId
        }

    val listState =
        rememberLazyListState()

    val scope =
        rememberCoroutineScope()

    var detailedManga by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf(manga)
    }

    var chapters by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf<List<Chapter>>(
            emptyList()
        )
    }

    var viewedChapterIds by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf<Set<String>>(
            emptySet()
        )
    }

    var chapterQuery by rememberSaveable(
        manga.id,
        sourceId
    ) {
        mutableStateOf("")
    }

    var searchExpanded by rememberSaveable(
        manga.id,
        sourceId
    ) {
        mutableStateOf(false)
    }

    var descendingOrder by rememberSaveable(
        manga.id,
        sourceId
    ) {
        mutableStateOf(true)
    }

    var detailsLoading by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf(true)
    }

    var chaptersLoading by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf(true)
    }

    var detailsError by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf<String?>(null)
    }

    var chaptersError by remember(
        manga.id,
        sourceId
    ) {
        mutableStateOf<String?>(null)
    }

    val favorites by FavoriteStore
        .favorites
        .collectAsStateWithLifecycle()

    val isFavorite =
        favorites.any { favorite ->
            favorite.id == detailedManga.id
        }

    LaunchedEffect(
        manga.id,
        sourceId
    ) {
        detailsLoading = true
        chaptersLoading = true
        detailsError = null
        chaptersError = null

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository
                    .currentSource
                    .getDetails(manga)
            }
        }.onSuccess { result ->
            detailedManga = result
        }.onFailure {
            /*
             * Mantemos os dados que já vieram da tela anterior.
             * Isso permite abrir a obra sem internet.
             */
            detailedManga = manga
        }

        detailsLoading = false

        val onlineChaptersResult =
            runCatching {
                withContext(Dispatchers.IO) {
                    SourceRepository
                        .currentSource
                        .getChapters(manga)
                }
            }

        val onlineChapters =
            onlineChaptersResult
                .getOrNull()
                .orEmpty()

        if (onlineChapters.isNotEmpty()) {
            chapters = onlineChapters
            chaptersError = null
        } else {
            val offlineChapters =
                withContext(Dispatchers.IO) {
                    downloadRepository
                        .observeDownloadedChapters(
                            sourceId = sourceId,
                            mangaId = manga.id
                        )
                        .first()
                        .filter { entity ->
                            entity.status ==
                                    DownloadStatus.COMPLETED
                        }
                        .map { entity ->
                            Chapter(
                                id =
                                    entity.chapterId,
                                mangaId =
                                    entity.mangaId,
                                name =
                                    entity.chapterName,
                                number =
                                    entity.chapterNumber,
                                url =
                                    entity.chapterUrl,
                                uploadedAt =
                                    entity.uploadedAt
                            )
                        }
                }

            chapters = offlineChapters

            chaptersError =
                if (offlineChapters.isEmpty()) {
                    onlineChaptersResult
                        .exceptionOrNull()
                        ?.message
                        ?: "Nenhum capítulo disponível offline."
                } else {
                    null
                }
        }

        viewedChapterIds =
            chapters
                .filter { chapter ->
                    progressStore.isViewed(
                        chapter.id
                    )
                }
                .map { chapter ->
                    chapter.id
                }
                .toSet()

        chaptersLoading = false
    }

    val normalizedQuery =
        chapterQuery.trim()

    val exactChapterNumber =
        normalizedQuery
            .replace(",", ".")
            .toDoubleOrNull()

    val filteredChapters =
        chapters
            .filter { chapter ->
                when {
                    normalizedQuery.isBlank() -> {
                        true
                    }

                    exactChapterNumber != null -> {
                        chapter.number ==
                                exactChapterNumber
                    }

                    else -> {
                        chapter.name.contains(
                            normalizedQuery,
                            ignoreCase = true
                        )
                    }
                }
            }
            .let { result ->
                if (descendingOrder) {
                    result.sortedByDescending { chapter ->
                        chapter.number
                    }
                } else {
                    result.sortedBy { chapter ->
                        chapter.number
                    }
                }
            }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            DetailsTopBar(
                manga = detailedManga,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onFavoriteClick = {
                    FavoriteStore.toggleFavorite(
                        detailedManga
                    )
                }
            )
        }

        item {
            MangaDetailsContent(
                manga = detailedManga,
                detailsLoading = detailsLoading,
                detailsError = detailsError
            )
        }

        item {
            DetailsChapterContent(
                chapterQuery = chapterQuery,
                searchExpanded = searchExpanded,
                descendingOrder = descendingOrder,
                normalizedQuery = normalizedQuery,
                chaptersCount = chapters.size,
                filteredChaptersCount =
                    filteredChapters.size,
                chaptersLoading = chaptersLoading,
                chaptersError = chaptersError,
                onQueryChange = { value ->
                    chapterQuery = value
                },
                onSearchExpandedChange = { expanded ->
                    searchExpanded = expanded
                },
                onToggleOrder = {
                    descendingOrder =
                        !descendingOrder

                    chapterQuery = ""

                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }

        items(
            items = filteredChapters,
            key = { chapter ->
                chapter.id
            }
        ) { chapter ->
            val isViewed =
                chapter.id in viewedChapterIds

            val downloadedChapter =
                downloadedChaptersById[
                    chapter.id
                ]

            val downloadStatus =
                downloadedChapter?.status

            val downloadProgress =
                downloadedChapter
                    ?.progressPercent
                    ?: 0

            val downloadRequest =
                ChapterDownloadRequest(
                    sourceId = sourceId,
                    mangaId = detailedManga.id,
                    mangaTitle =
                        detailedManga.title,
                    mangaDescription =
                        detailedManga.description,
                    mangaCoverUrl =
                        detailedManga.coverUrl,
                    mangaAuthor =
                        detailedManga.author,
                    mangaStatus =
                        detailedManga
                            .status
                            .displayName,
                    mangaType =
                        detailedManga
                            .type
                            .displayName,
                    mangaGenres =
                        detailedManga
                            .genres
                            .joinToString(
                                separator = ", "
                            ),
                    chapterId =
                        chapter.id,
                    chapterName =
                        chapter.name,
                    chapterNumber =
                        chapter.number,
                    chapterUrl =
                        chapter.url,
                    chapterUploadedAt =
                        chapter.uploadedAt
                )

            ChapterItem(
                chapter = chapter,
                isViewed = isViewed,
                downloadStatus =
                    downloadStatus,
                downloadProgress =
                    downloadProgress,
                onClick = {
                    progressStore.markViewed(
                        chapter.id
                    )

                    progressStore.registerReading(
                        manga = detailedManga,
                        chapter = chapter
                    )

                    viewedChapterIds =
                        viewedChapterIds +
                                chapter.id

                    onChapterClick(
                        chapter,
                        filteredChapters
                    )
                },
                onDownloadClick = {
                    scope.launch {
                        when (downloadStatus) {
                            DownloadStatus.QUEUED,
                            DownloadStatus.DOWNLOADING -> {
                                downloadManager
                                    .cancelAndDelete(
                                        downloadRequest
                                    )
                            }

                            DownloadStatus.FAILED,
                            DownloadStatus.PAUSED -> {
                                downloadManager.retry(
                                    downloadRequest
                                )
                            }

                            DownloadStatus.COMPLETED -> {
                                Unit
                            }

                            null -> {
                                downloadManager.enqueue(
                                    downloadRequest
                                )
                            }
                        }
                    }
                }
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}