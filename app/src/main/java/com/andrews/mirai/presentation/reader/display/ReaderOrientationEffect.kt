package com.andrews.mirai.presentation.reader.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun ReaderOrientationEffect(
    orientationMode: ReaderOrientationMode
) {
    val context =
        LocalContext.current

    val activity =
        remember(context) {
            context.findReaderActivity()
        }

    DisposableEffect(
        activity,
        orientationMode
    ) {
        if (activity == null) {
            return@DisposableEffect onDispose {
                Unit
            }
        }

        val previousOrientation =
            activity.requestedOrientation

        activity.requestedOrientation =
            when (orientationMode) {
                ReaderOrientationMode.AUTOMATIC -> {
                    ActivityInfo
                        .SCREEN_ORIENTATION_UNSPECIFIED
                }

                ReaderOrientationMode.PORTRAIT -> {
                    ActivityInfo
                        .SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }

                ReaderOrientationMode.LANDSCAPE -> {
                    ActivityInfo
                        .SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }

        onDispose {
            activity.requestedOrientation =
                previousOrientation
        }
    }
}

private tailrec fun Context.findReaderActivity():
        Activity? {
    return when (this) {
        is Activity -> {
            this
        }

        is ContextWrapper -> {
            baseContext.findReaderActivity()
        }

        else -> {
            null
        }
    }
}