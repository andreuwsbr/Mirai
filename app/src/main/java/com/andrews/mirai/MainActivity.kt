package com.andrews.mirai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.andrews.mirai.data.local.AppSettingsStore
import com.andrews.mirai.presentation.app.MiraiApp
import com.andrews.mirai.ui.theme.MiraiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        AppSettingsStore.initialize(
            applicationContext
        )

        enableEdgeToEdge()

        setContent {
            MiraiTheme {
                MiraiApp()
            }
        }
    }
}