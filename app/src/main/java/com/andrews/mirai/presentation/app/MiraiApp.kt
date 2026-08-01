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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.navigation.MiraiDestination
import com.andrews.mirai.presentation.catalog.CatalogScreen
import com.andrews.mirai.presentation.details.DetailsScreen
import com.andrews.mirai.presentation.history.HistoryScreen
import com.andrews.mirai.presentation.home.HomeScreen
import com.andrews.mirai.presentation.library.LibraryScreen
import com.andrews.mirai.presentation.reader.ReaderScreen
import com.andrews.mirai.presentation.settings.SettingsScreen

private const val DETAILS_ROUTE = "details"
private const val READER_ROUTE = "reader"

@Composable
fun MiraiApp() {
    val context =
        LocalContext.current.applicationContext

    remember(context) {
        FavoriteStore.initialize(context)
        SourceRepository.initialize(context)
        true
    }

    val navController =
        rememberNavController()

    var selectedManga by remember {
        mutableStateOf<Manga?>(null)
    }

    var selectedChapter by remember {
        mutableStateOf<Chapter?>(null)
    }

    var selectedChapters by remember {
        mutableStateOf<List<Chapter>>(
            emptyList()
        )
    }

    val backStack by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStack?.destination?.route

    val showBottomBar =
        currentRoute != DETAILS_ROUTE &&
                currentRoute != READER_ROUTE

    fun openSavedChapter(
        chapter: Chapter,
        sourceId: String
    ) {
        val sourceSelected =
            SourceRepository.selectSource(
                sourceId
            )

        if (!sourceSelected) {
            return
        }

        selectedChapter = chapter
        selectedChapters = emptyList()

        navController.navigate(
            READER_ROUTE
        ) {
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MiraiDestination.bottomItems
                        .forEach { destination ->
                            NavigationBarItem(
                                selected =
                                    currentRoute ==
                                            destination.route,
                                onClick = {
                                    navController.navigate(
                                        destination.route
                                    ) {
                                        popUpTo(
                                            MiraiDestination
                                                .Home
                                                .route
                                        ) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector =
                                            destination.icon,
                                        contentDescription =
                                            destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text =
                                            destination.label
                                    )
                                },
                                colors =
                                    NavigationBarItemDefaults
                                        .colors()
                            )
                        }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination =
                MiraiDestination.Home.route,
            modifier =
                Modifier.padding(innerPadding)
        ) {
            composable(
                MiraiDestination.Home.route
            ) {
                HomeScreen(
                    onMangaClick = {
                            manga: Manga ->

                        selectedManga = manga

                        navController.navigate(
                            DETAILS_ROUTE
                        )
                    },
                    onContinueReadingClick = {
                            chapter: Chapter,
                            sourceId: String ->

                        openSavedChapter(
                            chapter = chapter,
                            sourceId = sourceId
                        )
                    }
                )
            }

            composable(
                MiraiDestination.Catalog.route
            ) {
                CatalogScreen(
                    onMangaClick = {
                            manga: Manga ->

                        selectedManga = manga

                        navController.navigate(
                            DETAILS_ROUTE
                        )
                    }
                )
            }

            composable(
                MiraiDestination.Library.route
            ) {
                LibraryScreen(
                    onMangaClick = {
                            manga: Manga ->

                        selectedManga = manga

                        navController.navigate(
                            DETAILS_ROUTE
                        )
                    }
                )
            }

            composable(
                MiraiDestination.History.route
            ) {
                HistoryScreen(
                    onContinueReading = {
                            chapter: Chapter,
                            sourceId: String ->

                        openSavedChapter(
                            chapter = chapter,
                            sourceId = sourceId
                        )
                    }
                )
            }

            composable(
                MiraiDestination.Settings.route
            ) {
                SettingsScreen()
            }

            composable(
                DETAILS_ROUTE
            ) {
                val manga =
                    selectedManga

                if (manga != null) {
                    DetailsScreen(
                        manga = manga,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onChapterClick = {
                                chapter: Chapter,
                                chapters: List<Chapter> ->

                            selectedChapter = chapter
                            selectedChapters = chapters

                            navController.navigate(
                                READER_ROUTE
                            )
                        }
                    )
                } else {
                    Text(
                        text =
                            "Não foi possível carregar a obra."
                    )
                }
            }

            composable(
                READER_ROUTE
            ) {
                val chapter =
                    selectedChapter

                if (chapter != null) {
                    ReaderScreen(
                        chapter = chapter,
                        chapters = selectedChapters,
                        onChapterSelected = {
                                newChapter: Chapter ->

                            selectedChapter =
                                newChapter
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Text(
                        text =
                            "Não foi possível carregar o capítulo."
                    )
                }
            }
        }
    }
}