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
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import kotlinx.coroutines.Dispatchers
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

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var detailedManga by remember(manga.id) {
        mutableStateOf(manga)
    }

    var chapters by remember(manga.id) {
        mutableStateOf<List<Chapter>>(emptyList())
    }

    var viewedChapterIds by remember(manga.id) {
        mutableStateOf<Set<String>>(emptySet())
    }

    var chapterQuery by rememberSaveable(manga.id) {
        mutableStateOf("")
    }

    var searchExpanded by rememberSaveable(manga.id) {
        mutableStateOf(false)
    }

    var descendingOrder by rememberSaveable(manga.id) {
        mutableStateOf(true)
    }

    var detailsLoading by remember(manga.id) {
        mutableStateOf(true)
    }

    var chaptersLoading by remember(manga.id) {
        mutableStateOf(true)
    }

    var detailsError by remember(manga.id) {
        mutableStateOf<String?>(null)
    }

    var chaptersError by remember(manga.id) {
        mutableStateOf<String?>(null)
    }

    val favorites by FavoriteStore
        .favorites
        .collectAsStateWithLifecycle()

    val isFavorite = favorites.any { favorite ->
        favorite.id == detailedManga.id
    }

    LaunchedEffect(manga.id) {
        detailsLoading = true
        chaptersLoading = true
        detailsError = null
        chaptersError = null

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository.currentSource.getDetails(
                    manga
                )
            }
        }.onSuccess { result ->
            detailedManga = result
        }.onFailure { throwable ->
            detailsError =
                throwable.message
                    ?: "Erro ao carregar os detalhes."
        }

        detailsLoading = false

        runCatching {
            withContext(Dispatchers.IO) {
                SourceRepository.currentSource.getChapters(
                    manga
                )
            }
        }.onSuccess { result ->
            chapters = result

            viewedChapterIds = result
                .filter { chapter ->
                    progressStore.isViewed(
                        chapter.id
                    )
                }
                .map { chapter ->
                    chapter.id
                }
                .toSet()
        }.onFailure { throwable ->
            chaptersError =
                throwable.message
                    ?: "Erro ao carregar os capítulos."
        }

        chaptersLoading = false
    }

    val normalizedQuery = chapterQuery.trim()

    val exactChapterNumber = normalizedQuery
        .replace(",", ".")
        .toDoubleOrNull()

    val filteredChapters = chapters
        .filter { chapter ->
            when {
                normalizedQuery.isBlank() -> {
                    true
                }

                exactChapterNumber != null -> {
                    chapter.number == exactChapterNumber
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

            ChapterItem(
                chapter = chapter,
                isViewed = isViewed,
                onClick = {
                    progressStore.markViewed(
                        chapter.id
                    )

                    progressStore.registerReading(
                        manga = detailedManga,
                        chapter = chapter
                    )

                    viewedChapterIds =
                        viewedChapterIds + chapter.id

                    onChapterClick(
                        chapter,
                        chapters
                    )
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