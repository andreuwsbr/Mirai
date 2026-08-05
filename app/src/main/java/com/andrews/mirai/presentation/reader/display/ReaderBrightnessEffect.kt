package com.andrews.mirai.presentation.reader.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun ReaderBrightnessEffect(
    brightnessPercent: Int
) {
    val context =
        LocalContext.current

    val activity =
        remember(context) {
            context.findBrightnessActivity()
        }

    val window =
        activity?.window

    /*
     * Guarda o brilho que estava configurado antes
     * de o leitor ser aberto.
     *
     * Esse valor não é recriado quando o usuário
     * movimenta o controle de brilho.
     */
    val originalBrightness =
        remember(window) {
            window
                ?.attributes
                ?.screenBrightness
        }

    /*
     * Atualiza o brilho imediatamente sempre que
     * o usuário movimentar o controle.
     */
    LaunchedEffect(
        window,
        brightnessPercent
    ) {
        if (window == null) {
            return@LaunchedEffect
        }

        val attributes =
            window.attributes

        attributes.screenBrightness =
            brightnessValue(
                brightnessPercent
            )

        window.attributes =
            attributes
    }

    /*
     * Restaura o brilho original somente quando
     * o usuário realmente sair do leitor.
     */
    DisposableEffect(
        window,
        originalBrightness
    ) {
        onDispose {
            if (
                window != null &&
                originalBrightness != null
            ) {
                val attributes =
                    window.attributes

                attributes.screenBrightness =
                    originalBrightness

                window.attributes =
                    attributes
            }
        }
    }
}

private fun brightnessValue(
    brightnessPercent: Int
): Float {
    val safeBrightness =
        brightnessPercent.coerceIn(
            minimumValue = 0,
            maximumValue = 100
        )

    if (safeBrightness == 0) {
        return WindowManager
            .LayoutParams
            .BRIGHTNESS_OVERRIDE_NONE
    }

    return (
            safeBrightness.toFloat() /
                    100f
            ).coerceIn(
            minimumValue = 0.01f,
            maximumValue = 1f
        )
}

private tailrec fun Context.findBrightnessActivity():
        Activity? {
    return when (this) {
        is Activity -> {
            this
        }

        is ContextWrapper -> {
            baseContext.findBrightnessActivity()
        }

        else -> {
            null
        }
    }
}