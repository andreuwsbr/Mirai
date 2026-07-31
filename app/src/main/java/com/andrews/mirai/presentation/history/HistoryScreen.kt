package com.andrews.mirai.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.andrews.mirai.presentation.components.MiraiHeader

@Composable
fun HistoryScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        MiraiHeader("Histórico", "Continue exatamente de onde parou")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma leitura registrada.")
        }
    }
}
