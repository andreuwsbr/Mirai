package com.andrews.mirai.presentation.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.details.components.ChapterListControls
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }

                IconButton(
                    onClick = {
                        FavoriteStore.toggleFavorite(
                            detailedManga
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription =
                            if (isFavorite) {
                                "Remover dos favoritos"
                            } else {
                                "Adicionar aos favoritos"
                            },
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
            ) {
                if (detailsLoading) {
                    CircularProgressIndicator()

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

                if (detailsError != null) {
                    Text(
                        text =
                            "Não foi possível carregar todos os detalhes.\n$detailsError",
                        color =
                            MaterialTheme.colorScheme.error
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Start
                ) {
                    AsyncImage(
                        model = detailedManga.coverUrl,
                        contentDescription =
                            "Capa de ${detailedManga.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(140.dp)
                            .height(200.dp)
                            .clip(
                                RoundedCornerShape(16.dp)
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = detailedManga.title,
                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "Tipo: ${detailedManga.type.name}",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Autor: ${detailedManga.author}",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Status: ${detailedManga.status.name}",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Sinopse",
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        detailedManga.description.ifBlank {
                            "A sinopse não foi encontrada."
                        },
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                if (detailedManga.genres.isNotEmpty()) {
                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = "Gêneros",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            detailedManga.genres
                                .joinToString(", "),
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Capítulos",
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                ChapterListControls(
                    query = chapterQuery,
                    searchExpanded = searchExpanded,
                    descendingOrder = descendingOrder,

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

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = if (descendingOrder) {
                        "Ordem: mais recente primeiro"
                    } else {
                        "Ordem: mais antigo primeiro"
                    },
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                if (
                    !chaptersLoading &&
                    chapters.isNotEmpty()
                ) {
                    Text(
                        text = if (
                            normalizedQuery.isNotBlank()
                        ) {
                            "${filteredChapters.size} resultado(s)"
                        } else {
                            "${chapters.size} capítulos encontrados"
                        },
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (chaptersLoading) {
                    CircularProgressIndicator()
                }

                if (chaptersError != null) {
                    Text(
                        text = chaptersError!!,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }

                if (
                    !chaptersLoading &&
                    chaptersError == null &&
                    chapters.isEmpty()
                ) {
                    Text(
                        text =
                            "Nenhum capítulo foi encontrado.",
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                if (
                    !chaptersLoading &&
                    chapters.isNotEmpty() &&
                    filteredChapters.isEmpty()
                ) {
                    Text(
                        text =
                            "Nenhum capítulo corresponde à pesquisa.",
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
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

@Composable
private fun ChapterItem(
    chapter: Chapter,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(
                if (isViewed) {
                    0.55f
                } else {
                    1f
                }
            )
            .padding(
                horizontal = 20.dp,
                vertical = 5.dp
            )
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = chapter.name,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            if (chapter.uploadedAt.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = chapter.uploadedAt,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            if (isViewed) {
                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Visto",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }
    }
}