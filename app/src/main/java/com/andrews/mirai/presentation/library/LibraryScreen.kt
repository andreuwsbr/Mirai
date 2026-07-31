package com.andrews.mirai.presentation.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.andrews.mirai.presentation.components.MiraiHeader
import androidx.compose.foundation.layout.Column

@Composable
fun LibraryScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        MiraiHeader("Biblioteca", "Suas obras favoritas e salvas")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sua biblioteca ainda está vazia.")
        }
    }
}
