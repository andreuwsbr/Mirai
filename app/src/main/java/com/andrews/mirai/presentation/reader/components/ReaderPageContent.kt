package com.andrews.mirai.presentation.reader.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import java.io.File

@Composable
fun ReaderPageContent(
    page: ReaderPage,
    imageDownloader: ReaderImageDownloader,
    paged: Boolean,
    backgroundColor: Color,
    onTap: () -> Unit
) {
    var imageFile by remember(page.imageUrl) {
        mutableStateOf<File?>(null)
    }

    var aspectRatio by remember(page.imageUrl) {
        mutableStateOf(
            imageDownloader.getCachedAspectRatio(
                page.imageUrl
            )
        )
    }

    var isLoading by remember(page.imageUrl) {
        mutableStateOf(true)
    }

    var errorMessage by remember(page.imageUrl) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(page.imageUrl) {
        isLoading = true
        errorMessage = null

        runCatching {
            imageDownloader.downloadWithInfo(
                page.imageUrl
            )
        }.onSuccess { downloadedImage ->
            imageFile = downloadedImage.file
            aspectRatio = downloadedImage.aspectRatio
        }.onFailure { throwable ->
            errorMessage = throwable.message
                ?: "Não foi possível carregar esta página."
        }

        isLoading = false
    }

    val knownAspectRatio = aspectRatio

    Box(
        modifier = when {
            paged -> {
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            }

            knownAspectRatio != null -> {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(knownAspectRatio)
                    .background(backgroundColor)
            }

            else -> {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
                    .background(backgroundColor)
            }
        },
        contentAlignment = Alignment.Center
    ) {
        when {
            errorMessage != null -> {
                Text(
                    text = buildString {
                        append("Erro na página ")
                        append(page.index + 1)
                        append("\n")
                        append(errorMessage)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            imageFile != null &&
                    knownAspectRatio != null -> {
                MiraiReaderImage(
                    imageFileUri = Uri.fromFile(imageFile),
                    zoomEnabled = paged,
                    onTap = onTap,
                    modifier = Modifier.fillMaxSize()
                )
            }

            isLoading -> {
                PageLoadingContent(
                    pageNumber = page.index + 1
                )
            }
        }
    }
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
            text = "Carregando página $pageNumber...",
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}