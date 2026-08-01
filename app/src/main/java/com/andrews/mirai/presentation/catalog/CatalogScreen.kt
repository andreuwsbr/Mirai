package com.andrews.mirai.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.MangaCard
import com.andrews.mirai.presentation.components.MiraiHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.andrews.mirai.data.source.SourceRegistry
import com.andrews.mirai.presentation.catalog.components.SourceSelector
import com.andrews.mirai.presentation.catalog.components.ExpandableSearchBar

private const val SEARCH_DELAY_MILLIS = 500L

@Composable
fun CatalogScreen(
    onMangaClick: (Manga) -> Unit = {}
) {
    var query by remember {
        mutableStateOf("")
    }
    var selectedSource by remember {
        mutableStateOf(SourceRepository.currentSource)
    }

    var mangas by remember {
        mutableStateOf<List<Manga>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(query, selectedSource) {
        val normalizedQuery = query.trim()

        loading = true
        error = null

        if (normalizedQuery.isNotBlank()) {
            delay(SEARCH_DELAY_MILLIS)
        }

        try {
            val result = withContext(Dispatchers.IO) {
                if (normalizedQuery.isBlank()) {
                    selectedSource.getPopular(
                        page = 1
                    )
                } else {
                    selectedSource.search(
                        query = normalizedQuery,
                        page = 1
                    )
                }
            }

            mangas = result
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            mangas = emptyList()

            error = throwable.message
                ?: throwable.javaClass.simpleName
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Catálogo",
            subtitle = "Escolha uma fonte"
        )

        SourceSelector(

            sources = SourceRepository.sources,

            selectedSource = selectedSource,

            onSourceSelected = {

                selectedSource = it

                SourceRepository.currentSource = it

                query = ""
            }

        )

        ExpandableSearchBar(

            value = query,

            onValueChange = {

                query = it

            }

        )

        when {
            loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()

                    Text(
                        text = if (query.isBlank()) {
                            "Carregando catálogo..."
                        } else {
                            "Pesquisando..."
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            error != null -> {
                Text(
                    text = "Erro ao carregar o catálogo:\n$error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(20.dp)
                )
            }

            mangas.isEmpty() -> {
                Text(
                    text = if (query.isBlank()) {
                        "Nenhuma obra encontrada."
                    } else {
                        "Nenhum resultado para \"${query.trim()}\"."
                    },
                    modifier = Modifier.padding(20.dp)
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = mangas,
                        key = { manga ->
                            manga.id
                        }
                    ) { manga ->
                        MangaCard(
                            manga = manga,
                            onClick = {
                                onMangaClick(manga)
                            }
                        )
                    }
                }
            }
        }
    }
}