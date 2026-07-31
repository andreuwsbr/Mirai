package com.andrews.mirai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MiraiDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : MiraiDestination("home", "Início", Icons.Outlined.Home)
    data object Catalog : MiraiDestination("catalog", "Catálogo", Icons.Outlined.Explore)
    data object Library : MiraiDestination("library", "Biblioteca", Icons.Outlined.MenuBook)
    data object History : MiraiDestination("history", "Histórico", Icons.Outlined.History)
    data object Settings : MiraiDestination("settings", "Ajustes", Icons.Outlined.Settings)

    companion object {
        val bottomItems = listOf(Home, Catalog, Library, History, Settings)
    }
}
