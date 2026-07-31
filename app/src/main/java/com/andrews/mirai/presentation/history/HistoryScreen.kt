package com.andrews.mirai.presentation.history

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore

@Composable
fun HistoryScreen() {
    val applicationContext =
        LocalContext.current.applicationContext

    val progressStore = remember(applicationContext) {
        ReadingProgressStore(applicationContext)
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    val history = remember(refreshKey) {
        progressStore.getHistory()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Histórico",
            subtitle = "Continue exatamente de onde parou"
        )

        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Nenhuma leitura registrada.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    progressStore.clearHistory()
                    refreshKey++
                }
            ) {
                Text("Limpar histórico")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = history,
                key = { item ->
                    item.chapterId
                }
            ) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        AsyncImage(
                            model = item.mangaCoverUrl,
                            contentDescription =
                                "Capa de ${item.mangaTitle}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(72.dp)
                                .height(104.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column {
                            Text(
                                text = item.mangaTitle,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = item.chapterName,
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Página ${item.pageIndex + 1}",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}