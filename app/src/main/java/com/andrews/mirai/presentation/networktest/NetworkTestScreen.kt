package com.andrews.mirai.presentation.networktest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.source.mangalivre.MangaLivreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NetworkTestScreen() {

    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    var result by remember {
        mutableStateOf("Nenhum teste realizado.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Teste da Fonte Manga Livre",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Este teste utiliza o MangaLivreSource e o HomeParser.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            onClick = {

                coroutineScope.launch {

                    isLoading = true

                    result = withContext(Dispatchers.IO) {

                        runCatching {

                            val source = MangaLivreSource()

                            val mangas = source.getPopular(1)

                            buildString {

                                appendLine("Fonte carregada com sucesso")
                                appendLine()

                                appendLine("Mangás encontrados: ${mangas.size}")
                                appendLine()

                                mangas
                                    .take(15)
                                    .forEachIndexed { index, manga ->

                                        appendLine("${index + 1}. ${manga.title}")

                                    }

                            }

                        }.getOrElse {

                            "Erro:\n\n${it.stackTraceToString()}"

                        }

                    }

                    isLoading = false

                }

            }
        ) {

            Text(
                if (isLoading)
                    "Carregando..."
                else
                    "Carregar Mangás"
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {

            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(16.dp))

        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = result,
                modifier = Modifier.padding(16.dp)
            )

        }

    }

}