package com.andrews.mirai.presentation.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun ReaderSystemUiEffects(
    keepScreenOn: Boolean,
    fullscreen: Boolean
) {
    val context = LocalContext.current
    val view = LocalView.current

    val activity = remember(context) {
        context.findReaderActivity()
    }

    DisposableEffect(
        keepScreenOn,
        view
    ) {
        val previousKeepScreenOn =
            view.keepScreenOn

        view.keepScreenOn =
            keepScreenOn

        onDispose {
            view.keepScreenOn =
                previousKeepScreenOn
        }
    }

    DisposableEffect(
        fullscreen,
        activity
    ) {
        val window = activity?.window

        if (window != null) {
            val controller =
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                )

            if (fullscreen) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    false
                )

                controller.hide(
                    WindowInsetsCompat.Type.systemBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )

                controller.show(
                    WindowInsetsCompat.Type.systemBars()
                )
            }
        }

        onDispose {
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )

                WindowCompat
                    .getInsetsController(
                        window,
                        window.decorView
                    )
                    .show(
                        WindowInsetsCompat.Type.systemBars()
                    )
            }
        }
    }
}

private tailrec fun Context.findReaderActivity(): Activity? {
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