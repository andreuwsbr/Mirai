package com.andrews.mirai.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrews.mirai.data.download.DownloadRepository
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.components.resolveMangaCoverModel
import com.andrews.mirai.presentation.home.components.ContinueReadingCard
import com.andrews.mirai.presentation.home.components.HomeSearchBar
import com.andrews.mirai.presentation.home.components.HomeSection
import com.andrews.mirai.presentation.home.components.HomeSectionHeader
import com.andrews.mirai.presentation.home.components.HomeSourceSelector
import com.andrews.mirai.presentation.home.components.MangaHorizontalRow
import com.andrews.mirai.presentation.home.components.RecentHistorySection
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val RECENT_HISTORY_LIMIT =
    10

@Composable
fun HomeScreen(
    onMangaClick: (Manga) -> Unit = {},
    onSavedMangaClick: (
        manga: Manga,
        sourceId: String
    ) -> Unit = { _, _ -> },
    onContinueReadingClick: (
        chapter: Chapter,
        sourceId: String
    ) -> Unit = { _, _ -> },
    onOpenHistoryClick: () -> Unit = {}
) {
    val applicationContext =
        LocalContext
            .current
            .applicationContext

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

    val downloadedMangas by
    downloadRepository
        .observeDownloadedMangas()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    val listState =
        rememberLazyListState()

    var selectedSourceId by
    rememberSaveable {
        mutableStateOf(
            SourceRepository
                .currentSource
                .id
        )
    }

    val source =
        SourceRepository
            .sources
            .firstOrNull { sourceItem ->
                sourceItem.id ==
                        selectedSourceId
            }
            ?: SourceRepository
                .currentSource

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var searchExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var mangas by
    remember(source.id) {
        mutableStateOf<List<Manga>>(
            emptyList()
        )
    }

    var loading by
    remember(source.id) {
        mutableStateOf(true)
    }

    var errorMessage by
    remember(source.id) {
        mutableStateOf<String?>(
            null
        )
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    val history =
        progressStore
            .getHistory()
            .sortedByDescending { item ->
                item.readAt
            }

    val lastReading =
        history.firstOrNull()

    val recentHistory =
        history.take(
            RECENT_HISTORY_LIMIT
        )

    val continueReadingCoverModel =
        lastReading?.let { item ->
            resolveMangaCoverModel(
                sourceId =
                    item.sourceId,
                mangaId =
                    item.mangaId,
                remoteCoverUrl =
                    item.mangaCoverUrl,
                downloadedMangas =
                    downloadedMangas
            )
        }

    LaunchedEffect(
        reloadKey,
        source.id
    ) {
        loading = true
        errorMessage = null

        try {
            mangas =
                withContext(
                    Dispatchers.IO
                ) {
                    source.getPopular(
                        page = 1
                    )
                }

            if (mangas.isEmpty()) {
                errorMessage =
                    "A fonte ${source.name} não retornou nenhuma obra."
            }
        } catch (
            exception: CancellationException
        ) {
            throw exception
        } catch (
            throwable: Throwable
        ) {
            mangas = emptyList()

            errorMessage =
                throwable.message
                    ?: "Não foi possível carregar as obras."
        } finally {
            loading = false
        }
    }

    val normalizedQuery =
        query.trim()

    val visibleMangas =
        remember(
            mangas,
            normalizedQuery
        ) {
            if (
                normalizedQuery.isBlank()
            ) {
                mangas
            } else {
                mangas.filter { manga ->
                    manga.title.contains(
                        other =
                            normalizedQuery,
                        ignoreCase = true
                    )
                }
            }
        }

    val featuredMangas =
        visibleMangas.take(8)

    val exploreMangas =
        visibleMangas
            .drop(
                featuredMangas.size
            )
            .take(12)

    LazyColumn(
        state = listState,
        modifier =
            Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                bottom = 40.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {
        item {
            MiraiHeader(
                title = "MIRAI",
                subtitle = "未来"
            ) {
                HomeSearchBar(
                    value = query,
                    onValueChange = {
                            newQuery ->
                        query = newQuery
                    },
                    expanded =
                        searchExpanded,
                    onExpand = {
                        searchExpanded = true
                    },
                    onClose = {
                        query = ""
                        searchExpanded = false
                    }
                )
            }
        }

        item {
            HomeSourceSelector(
                sources =
                    SourceRepository.sources,
                selectedSourceId =
                    source.id,
                onSourceSelected = {
                        newSourceId ->

                    val selected =
                        SourceRepository
                            .selectSource(
                                newSourceId
                            )

                    if (selected) {
                        selectedSourceId =
                            newSourceId

                        query = ""
                        searchExpanded =
                            false
                    }
                }
            )
        }

        if (lastReading != null) {
            item {
                ContinueReadingCard(
                    title =
                        lastReading
                            .mangaTitle,
                    chapter =
                        lastReading
                            .chapterName,
                    sourceName =
                        sourceNameFor(
                            lastReading
                                .sourceId
                        ),
                    coverModel =
                        continueReadingCoverModel,
                    currentPage =
                        lastReading
                            .pageIndex + 1,
                    totalPages =
                        lastReading
                            .totalPages,
                    onContinueClick = {
                        onContinueReadingClick(
                            createChapter(
                                lastReading
                            ),
                            lastReading
                                .sourceId
                        )
                    },
                    onDetailsClick = {
                        onSavedMangaClick(
                            createManga(
                                lastReading
                            ),
                            lastReading
                                .sourceId
                        )
                    }
                )
            }
        }

        if (recentHistory.isNotEmpty()) {
            item {
                RecentHistorySection(
                    items =
                        recentHistory,
                    coverModelProvider = {
                            item ->
                        resolveMangaCoverModel(
                            sourceId =
                                item.sourceId,
                            mangaId =
                                item.mangaId,
                            remoteCoverUrl =
                                item.mangaCoverUrl,
                            downloadedMangas =
                                downloadedMangas
                        )
                    },
                    onDetailsClick = {
                            item ->
                        onSavedMangaClick(
                            createManga(item),
                            item.sourceId
                        )
                    },
                    onContinueClick = {
                            item ->
                        onContinueReadingClick(
                            createChapter(item),
                            item.sourceId
                        )
                    },
                    onViewAllClick =
                        onOpenHistoryClick
                )
            }
        }

        when {
            loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(32.dp),
                        horizontalAlignment =
                            Alignment
                                .CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        CircularProgressIndicator()

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "Carregando ${source.name}..."
                        )
                    }
                }
            }

            errorMessage != null -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment
                                .CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            text =
                                errorMessage!!,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                        Button(
                            onClick = {
                                reloadKey++
                            },
                            modifier =
                                Modifier.padding(
                                    top = 16.dp
                                )
                        ) {
                            Text(
                                text =
                                    "Tentar novamente"
                            )
                        }
                    }
                }
            }

            visibleMangas.isEmpty() -> {
                item {
                    Text(
                        text =
                            "Nenhuma obra encontrada para \"$normalizedQuery\".",
                        modifier =
                            Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            )
                    )
                }
            }

            else -> {
                if (
                    featuredMangas
                        .isNotEmpty()
                ) {
                    item {
                        if (
                            normalizedQuery
                                .isBlank()
                        ) {
                            HomeSection(
                                title =
                                    "Destaques da ${source.name}"
                            ) {
                                MangaHorizontalRow(
                                    mangas =
                                        featuredMangas,
                                    coverModelProvider = {
                                            manga ->
                                        resolveMangaCoverModel(
                                            sourceId =
                                                source.id,
                                            mangaId =
                                                manga.id,
                                            remoteCoverUrl =
                                                manga.coverUrl,
                                            downloadedMangas =
                                                downloadedMangas
                                        )
                                    },
                                    onMangaClick =
                                        onMangaClick
                                )
                            }
                        } else {
                            HomeSectionHeader(
                                title =
                                    "Resultados da pesquisa"
                            )

                            MangaHorizontalRow(
                                mangas =
                                    featuredMangas,
                                coverModelProvider = {
                                        manga ->
                                    resolveMangaCoverModel(
                                        sourceId =
                                            source.id,
                                        mangaId =
                                            manga.id,
                                        remoteCoverUrl =
                                            manga.coverUrl,
                                        downloadedMangas =
                                            downloadedMangas
                                    )
                                },
                                onMangaClick =
                                    onMangaClick
                            )
                        }
                    }
                }

                if (
                    normalizedQuery.isBlank() &&
                    exploreMangas.isNotEmpty()
                ) {
                    item {
                        HomeSection(
                            title =
                                "Explore na ${source.name}"
                        ) {
                            MangaHorizontalRow(
                                mangas =
                                    exploreMangas,
                                coverModelProvider = {
                                        manga ->
                                    resolveMangaCoverModel(
                                        sourceId =
                                            source.id,
                                        mangaId =
                                            manga.id,
                                        remoteCoverUrl =
                                            manga.coverUrl,
                                        downloadedMangas =
                                            downloadedMangas
                                    )
                                },
                                onMangaClick =
                                    onMangaClick
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun createManga(
    item: ReadingHistoryItem
): Manga {
    return Manga(
        id = item.mangaId,
        title = item.mangaTitle,
        description = "",
        coverUrl = item.mangaCoverUrl
    )
}

private fun createChapter(
    item: ReadingHistoryItem
): Chapter {
    return Chapter(
        id = item.chapterId,
        mangaId = item.mangaId,
        name = item.chapterName,
        number =
            extractChapterNumber(
                item.chapterName
            ),
        url = item.chapterId
    )
}

private fun sourceNameFor(
    sourceId: String
): String {
    return SourceRepository
        .sources
        .firstOrNull { source ->
            source.id == sourceId
        }
        ?.name
        ?: "Fonte salva"
}

private fun extractChapterNumber(
    chapterName: String
): Double {
    return Regex(
        pattern =
            """\d+(?:[.,]\d+)?"""
    )
        .find(chapterName)
        ?.value
        ?.replace(
            oldValue = ",",
            newValue = "."
        )
        ?.toDoubleOrNull()
        ?: 0.0
}