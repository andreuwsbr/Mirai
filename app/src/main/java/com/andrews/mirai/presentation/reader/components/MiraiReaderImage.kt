package com.andrews.mirai.presentation.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import java.io.File

@Composable
fun MiraiReaderImage(
    imageFile: File,
    zoomEnabled: Boolean,
    longPage: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    ZoomableAsyncImage(
        model = imageFile,
        contentDescription = "Página do capítulo",
        contentScale = when {
            !zoomEnabled -> {
                ContentScale.FillWidth
            }

            longPage -> {
                ContentScale.FillWidth
            }

            else -> {
                ContentScale.Fit
            }
        },
        modifier = modifier,
        onClick = { _ ->
            if (zoomEnabled) {
                onTap()
            }
        }
    )
}