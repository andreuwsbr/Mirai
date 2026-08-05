package com.andrews.mirai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.andrews.mirai.data.local.AppSettingsStore
import com.andrews.mirai.data.local.AuthSessionStore
import com.andrews.mirai.presentation.app.MiraiApp
import com.andrews.mirai.presentation.splash.MiraiSplashScreen
import com.andrews.mirai.ui.theme.MiraiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        installSplashScreen()

        super.onCreate(
            savedInstanceState
        )

        AppSettingsStore.initialize(
            applicationContext
        )

        AuthSessionStore.initialize(
            applicationContext
        )

        enableEdgeToEdge()

        setContent {
            MiraiTheme {
                var showCustomSplash by remember {
                    mutableStateOf(
                        true
                    )
                }

                LaunchedEffect(
                    Unit
                ) {
                    delay(
                        CUSTOM_SPLASH_DURATION_MILLIS
                    )

                    showCustomSplash =
                        false
                }

                if (showCustomSplash) {
                    MiraiSplashScreen()
                } else {
                    MiraiApp()
                }
            }
        }
    }

    private companion object {

        const val CUSTOM_SPLASH_DURATION_MILLIS =
            1_000L
    }
}