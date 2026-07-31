package com.andrews.mirai.presentation.reader.components

import android.net.Uri
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun MiraiReaderImage(
    imageFileUri: Uri,
    modifier: Modifier = Modifier
) {
    var scale by remember(imageFileUri) {
        mutableFloatStateOf(1f)
    }

    var offset by remember(imageFileUri) {
        mutableStateOf(Offset.Zero)
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = imageFileUri,
            contentDescription = "Página do capítulo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()

                // Toque duplo: alterna entre 1x e 2,5x.
                .pointerInput(imageFileUri) {
                    detectTapGestures(
                        onDoubleTap = { tapPosition ->
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f

                                offset = Offset(
                                    x = size.width / 2f - tapPosition.x,
                                    y = size.height / 2f - tapPosition.y
                                )
                            }
                        }
                    )
                }

                // Dois dedos: zoom e movimentação da imagem.
                .pointerInput(imageFileUri) {
                    awaitEachGesture {
                        awaitFirstDown(
                            requireUnconsumed = false
                        )

                        do {
                            val event = awaitPointerEvent()

                            val activePointers = event.changes.count {
                                it.pressed
                            }

                            if (activePointers >= 2) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                val newScale = (scale * zoomChange)
                                    .coerceIn(
                                        minimumValue = 1f,
                                        maximumValue = 5f
                                    )

                                scale = newScale

                                offset = if (newScale > 1f) {
                                    offset + panChange
                                } else {
                                    Offset.Zero
                                }

                                event.changes.forEach {
                                    it.consume()
                                }
                            } else if (scale > 1f) {
                                val panChange = event.calculatePan()

                                offset += panChange

                                event.changes.forEach {
                                    it.consume()
                                }
                            }
                        } while (
                            event.changes.any {
                                it.pressed
                            }
                        )
                    }
                }

                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    clip = true
                }
        )
    }
}