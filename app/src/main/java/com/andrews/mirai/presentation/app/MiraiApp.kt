package com.andrews.mirai.presentation.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.navigation.MiraiDestination
import com.andrews.mirai.presentation.catalog.CatalogScreen
import com.andrews.mirai.presentation.details.DetailsScreen
import com.andrews.mirai.presentation.history.HistoryScreen
import com.andrews.mirai.presentation.home.HomeScreen
import com.andrews.mirai.presentation.library.LibraryScreen
import com.andrews.mirai.presentation.networktest.NetworkTestScreen
import com.andrews.mirai.presentation.reader.ReaderScreen

private const val DETAILS_ROUTE = "details"
private const val READER_ROUTE = "reader"

@Composable
fun MiraiApp() {
    val navController = rememberNavController()

    var selectedManga by remember {
        mutableStateOf<Manga?>(null)
    }

    var selectedChapter by remember {
        mutableStateOf<Chapter?>(null)
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar =
        currentRoute != DETAILS_ROUTE &&
                currentRoute != READER_ROUTE

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MiraiDestination.bottomItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(MiraiDestination.Home.route) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(destination.label)
                            },
                            colors = NavigationBarItemDefaults.colors()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MiraiDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MiraiDestination.Home.route) {
                HomeScreen()
            }

            composable(MiraiDestination.Catalog.route) {
                CatalogScreen(
                    onMangaClick = { manga ->
                        selectedManga = manga
                        navController.navigate(DETAILS_ROUTE)
                    }
                )
            }

            composable(MiraiDestination.Library.route) {
                LibraryScreen()
            }

            composable(MiraiDestination.History.route) {
                HistoryScreen()
            }

            composable(MiraiDestination.Settings.route) {
                NetworkTestScreen()
            }

            composable(DETAILS_ROUTE) {
                val manga = selectedManga

                if (manga != null) {
                    DetailsScreen(
                        manga = manga,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onChapterClick = { chapter ->
                            selectedChapter = chapter
                            navController.navigate(READER_ROUTE)
                        }
                    )
                } else {
                    Text("Não foi possível carregar a obra.")
                }
            }

            composable(READER_ROUTE) {
                val chapter = selectedChapter

                if (chapter != null) {
                    ReaderScreen(
                        chapter = chapter,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Text("Não foi possível carregar o capítulo.")
                }
            }
        }
    }
}