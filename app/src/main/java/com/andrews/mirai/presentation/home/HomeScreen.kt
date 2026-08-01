package com.andrews.mirai.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.home.components.ContinueReadingCard
import com.andrews.mirai.presentation.home.components.HomeSearchBar
import com.andrews.mirai.presentation.home.components.HomeSection
import com.andrews.mirai.presentation.home.components.MangaHorizontalRow
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    onMangaClick: (Manga) -> Unit = {},
    onContinueReadingClick: (
        chapter: Chapter,
        sourceId: String
    ) -> Unit = { _, _ -> }
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    val listState = rememberLazyListState()

    val source = SourceRepository.currentSource

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var searchExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var mangas by remember(source.id) {
        mutableStateOf<List<Manga>>(emptyList())
    }

    var loading by remember(source.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(source.id) {
        mutableStateOf<String?>(null)
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    val lastReading = progressStore
        .getHistory()
        .maxByOrNull { item ->
            item.readAt
        }

    LaunchedEffect(
        reloadKey,
        source.id
    ) {
        loading = true
        errorMessage = null

        try {
            mangas = withContext(Dispatchers.IO) {
                source.getPopular(
                    page = 1
                )
            }

            if (mangas.isEmpty()) {
                errorMessage =
                    "A fonte ${source.name} não retornou nenhuma obra."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
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

    val visibleMangas = remember(
        mangas,
        normalizedQuery
    ) {
        if (normalizedQuery.isBlank()) {
            mangas
        } else {
            mangas.filter { manga ->
                manga.title.contains(
                    other = normalizedQuery,
                    ignoreCase = true
                )
            }
        }
    }

    val featuredMangas =
        visibleMangas.take(8)

    val exploreMangas =
        visibleMangas
            .drop(featuredMangas.size)
            .take(12)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 40.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item {
            MiraiHeader(
                title = "MIRAI",
                subtitle = source.name
            ) {
                HomeSearchBar(
                    value = query,
                    onValueChange = { newQuery ->
                        query = newQuery
                    },
                    expanded = searchExpanded,
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

        if (lastReading != null) {
            item {
                ContinueReadingCard(
                    title =
                        lastReading.mangaTitle,
                    chapter =
                        lastReading.chapterName,
                    coverUrl =
                        lastReading.mangaCoverUrl,
                    currentPage =
                        lastReading.pageIndex + 1,
                    totalPages =
                        lastReading.totalPages,
                    onClick = {
                        val chapter = Chapter(
                            id =
                                lastReading.chapterId,
                            mangaId =
                                lastReading.mangaId,
                            name =
                                lastReading.chapterName,
                            number =
                                extractChapterNumber(
                                    lastReading.chapterName
                                ),
                            url =
                                lastReading.chapterId
                        )

                        onContinueReadingClick(
                            chapter,
                            lastReading.sourceId
                        )
                    }
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
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text =
                                "Carregando ${source.name}...",
                            modifier = Modifier.padding(
                                top = 12.dp
                            )
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
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                        Button(
                            onClick = {
                                reloadKey++
                            },
                            modifier = Modifier.padding(
                                top = 16.dp
                            )
                        ) {
                            Text(
                                text = "Tentar novamente"
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
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            else -> {
                if (featuredMangas.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "Destaques"
                        ) {
                            MangaHorizontalRow(
                                mangas =
                                    featuredMangas,
                                onMangaClick =
                                    onMangaClick
                            )
                        }
                    }
                }

                if (exploreMangas.isNotEmpty()) {
                    item {
                        HomeSection(
                            title =
                                "Explore na ${source.name}"
                        ) {
                            MangaHorizontalRow(
                                mangas =
                                    exploreMangas,
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

private fun extractChapterNumber(
    chapterName: String
): Double {
    return Regex(
        pattern = """\d+(?:[.,]\d+)?"""
    )
        .find(chapterName)
        ?.value
        ?.replace(",", ".")
        ?.toDoubleOrNull()
        ?: 0.0
}