package com.andrews.mirai.presentation.reader.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@Composable
fun MiraiReaderImage(
    imageFileUri: Uri,
    zoomEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    if (zoomEnabled) {
        ZoomableAsyncImage(
            model = imageFileUri,
            contentDescription = "Página do capítulo",
            contentScale = ContentScale.Fit,
            modifier = modifier,
            onClick = { _ ->
                onTap()
            }
        )
    } else {
        AsyncImage(
            model = imageFileUri,
            contentDescription = "Página do capítulo",
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.High,
            modifier = modifier
        )
    }
}