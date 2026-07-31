package com.andrews.mirai.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    onContinueReadingClick: (Chapter) -> Unit = {}
) {
    val applicationContext =
        LocalContext.current.applicationContext

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    val listState = rememberLazyListState()

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var mangas by remember {
        mutableStateOf<List<Manga>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    val source = SourceRepository.currentSource

    val lastReading = progressStore
        .getHistory()
        .maxByOrNull { item ->
            item.readAt
        }

    LaunchedEffect(reloadKey) {
        loading = true
        errorMessage = null

        try {
            mangas = withContext(Dispatchers.IO) {
                source.getPopular(page = 1)
            }

            if (mangas.isEmpty()) {
                errorMessage =
                    "A fonte não retornou nenhuma obra."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            mangas = emptyList()
            errorMessage = throwable.message
                ?: "Não foi possível carregar as obras."
        } finally {
            loading = false
        }
    }

    val normalizedQuery = query.trim()

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

    val recentMangas =
        visibleMangas.drop(8).take(8)
            .ifEmpty { featuredMangas }

    val popularMangas =
        visibleMangas.drop(16).take(8)
            .ifEmpty { featuredMangas }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            MiraiHeader(
                title = "MIRAI",
                subtitle = source.name
            )
        }

        item {
            HomeSearchBar(
                value = query,
                onValueChange = { newQuery ->
                    query = newQuery
                }
            )
        }

        if (lastReading != null) {
            item {
                ContinueReadingCard(
                    title = lastReading.mangaTitle,
                    chapter = lastReading.chapterName,
                    coverUrl = lastReading.mangaCoverUrl,
                    currentPage = lastReading.pageIndex + 1,
                    totalPages = lastReading.totalPages,
                    onClick = {
                        onContinueReadingClick(
                            Chapter(
                                id = lastReading.chapterId,
                                mangaId = lastReading.mangaId,
                                name = lastReading.chapterName,
                                number = 0.0,
                                url = lastReading.chapterId
                            )
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
                            text = "Carregando obras...",
                            modifier = Modifier.padding(top = 12.dp)
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
                                MaterialTheme.colorScheme.error
                        )

                        Button(
                            onClick = {
                                reloadKey++
                            },
                            modifier =
                                Modifier.padding(top = 16.dp)
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            visibleMangas.isEmpty() -> {
                item {
                    Text(
                        text = "Nenhuma obra encontrada para \"$normalizedQuery\".",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            else -> {
                item {
                    HomeSection(
                        title = "Em destaque"
                    ) {
                        MangaHorizontalRow(
                            mangas = featuredMangas,
                            onMangaClick = onMangaClick
                        )
                    }
                }

                item {
                    HomeSection(
                        title = "Recentes"
                    ) {
                        MangaHorizontalRow(
                            mangas = recentMangas,
                            onMangaClick = onMangaClick
                        )
                    }
                }

                item {
                    HomeSection(
                        title = "Populares"
                    ) {
                        MangaHorizontalRow(
                            mangas = popularMangas,
                            onMangaClick = onMangaClick
                        )
                    }
                }
            }
        }
    }
}