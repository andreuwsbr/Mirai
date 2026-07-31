package com.andrews.mirai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.andrews.mirai.presentation.app.MiraiApp
import com.andrews.mirai.ui.theme.MiraiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiraiTheme {
                MiraiApp()
            }
        }
    }
}
