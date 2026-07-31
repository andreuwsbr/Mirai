package com.andrews.mirai.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.presentation.components.MiraiHeader

@Composable
fun SettingsScreen() {
    var amoled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        MiraiHeader("Ajustes", "Personalize a experiência do Mirai")

        ListItem(
            headlineContent = { Text("Tema AMOLED") },
            supportingContent = { Text("Base preparada para o tema preto verdadeiro") },
            trailingContent = {
                Switch(
                    checked = amoled,
                    onCheckedChange = { amoled = it }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        ListItem(
            headlineContent = { Text("Fontes") },
            supportingContent = { Text("A arquitetura de fontes já está criada") }
        )
    }
}
