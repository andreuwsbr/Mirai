package com.andrews.mirai.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.andrews.mirai.R

@Composable
fun MiraiSplashScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Image(
            painter =
                painterResource(
                    id =
                        R.drawable.mirai_splash
                ),
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )
    }
}