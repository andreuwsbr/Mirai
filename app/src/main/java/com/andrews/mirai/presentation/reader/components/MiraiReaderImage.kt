package com.andrews.mirai.presentation.reader.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * Exibe uma página grande do leitor usando carregamento por partes.
 *
 * A imagem precisa estar salva localmente para que a biblioteca
 * consiga preservar a resolução e evitar o borrado.
 */
@Composable
fun MiraiReaderImage(
    imageFileUri: Uri,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            SubsamplingScaleImageView(context).apply {
                setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)

                setDoubleTapZoomStyle(
                    SubsamplingScaleImageView.ZOOM_FOCUS_CENTER
                )

                setDoubleTapZoomScale(2f)
                maxScale = 5f
            }
        },
        update = { imageView ->
            imageView.setImage(
                ImageSource.uri(
                    imageView.context,
                    imageFileUri
                )
            )
        }
    )
}