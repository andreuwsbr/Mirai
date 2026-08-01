package com.andrews.mirai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.andrews.mirai.data.local.AppSettingsStore
import com.andrews.mirai.data.local.AppThemeMode

private val DarkColors = darkColorScheme(
    primary = MiraiPurple,
    background = MiraiDark,
    surface = MiraiSurface
)

private val LightColors = lightColorScheme(
    primary = MiraiPurple
)

@Composable
fun MiraiTheme(
    content: @Composable () -> Unit
) {

    val savedTheme by AppSettingsStore.themeMode.collectAsState()

    val darkTheme = when (savedTheme) {

        AppThemeMode.SYSTEM ->
            isSystemInDarkTheme()

        AppThemeMode.DARK ->
            true

        AppThemeMode.LIGHT ->
            false
    }

    val colors =
        if (darkTheme) DarkColors
        else LightColors

    val view = LocalView.current

    if (!view.isInEditMode) {

        val window =
            (view.context as Activity).window

        window.statusBarColor =
            colors.background.toArgb()

        window.navigationBarColor =
            colors.background.toArgb()

        WindowCompat
            .getInsetsController(window, view)
            .isAppearanceLightStatusBars = !darkTheme

        WindowCompat
            .getInsetsController(window, view)
            .isAppearanceLightNavigationBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MiraiTypography,
        content = content
    )
}