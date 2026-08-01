package com.andrews.mirai.presentation.reader.components

import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        mutableStateOf<Float?>(null)
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
            val downloadedFile = imageDownloader.download(
                page.imageUrl
            )

            val imageAspectRatio = readImageAspectRatio(
                downloadedFile
            )

            downloadedFile to imageAspectRatio
        }.onSuccess { result ->
            imageFile = result.first
            aspectRatio = result.second
        }.onFailure { throwable ->
            errorMessage = throwable.message
                ?: "Não foi possível carregar esta página."
        }

        isLoading = false
    }

    Box(
        modifier = if (paged) {
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
        } else {
            Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        },
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                PageLoadingContent(
                    pageNumber = page.index + 1
                )
            }

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

            imageFile != null && aspectRatio != null -> {
                MiraiReaderImage(
                    imageFileUri = Uri.fromFile(imageFile),
                    zoomEnabled = paged,
                    onTap = onTap,
                    modifier = if (paged) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio!!)
                    }
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
            color = Color.White
        )
    }
}

private suspend fun readImageAspectRatio(
    file: File
): Float {
    return withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(
            file.absolutePath,
            options
        )

        if (
            options.outWidth <= 0 ||
            options.outHeight <= 0
        ) {
            throw IllegalStateException(
                "Não foi possível identificar o tamanho da imagem."
            )
        }

        options.outWidth.toFloat() /
                options.outHeight.toFloat()
    }
}