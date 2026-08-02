package com.andrews.mirai.data.sync

import android.content.Context
import com.andrews.mirai.data.local.FavoriteEntry
import com.andrews.mirai.data.local.FavoriteStore
import com.andrews.mirai.data.repository.CloudSyncRepository
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore

class CloudSyncManager(
    context: Context
) {
    private val readingProgressStore =
        ReadingProgressStore(
            context.applicationContext
        )

    fun restoreFromCloud(): Result<Unit> {
        if (!CloudSyncRepository.isAvailable()) {
            return Result.failure(
                IllegalStateException(
                    "Usuário não autenticado."
                )
            )
        }

        return runCatching {
            restoreFavorites()
            restoreReadingHistory()
            restoreChapterProgress()
        }
    }

    fun uploadLocalData(): Result<Unit> {
        if (!CloudSyncRepository.isAvailable()) {
            return Result.failure(
                IllegalStateException(
                    "Usuário não autenticado."
                )
            )
        }

        return runCatching {
            val favoriteResult =
                CloudSyncRepository.uploadAllFavorites(
                    FavoriteStore.favoriteEntries.value
                )

            checkSyncResult(
                result = favoriteResult,
                operation = "favoritos"
            )

            val historyResult =
                CloudSyncRepository.uploadAllHistory(
                    readingProgressStore.getHistory()
                )

            checkSyncResult(
                result = historyResult,
                operation = "histórico"
            )
        }
    }

    fun hasLocalData(): Boolean {
        return FavoriteStore
            .favoriteEntries
            .value
            .isNotEmpty() ||
                readingProgressStore
                    .getHistory()
                    .isNotEmpty()
    }

    private fun restoreFavorites() {
        val cloudFavorites =
            CloudSyncRepository
                .downloadFavorites()
                .getOrThrow()

        val favoriteEntries =
            cloudFavorites.map { favorite ->
                FavoriteEntry(
                    sourceId =
                        favorite.sourceId,
                    manga =
                        Manga(
                            id =
                                favorite.mangaId,
                            title =
                                favorite.mangaTitle,
                            description = "",
                            coverUrl =
                                favorite.mangaCoverUrl,
                            author =
                                "Não informado",
                            status =
                                MangaStatus.UNKNOWN,
                            type =
                                MangaType.UNKNOWN,
                            genres =
                                emptyList()
                        )
                )
            }

        FavoriteStore.mergeFavoriteEntries(
            favoriteEntries
        )
    }

    private fun restoreReadingHistory() {
        val cloudHistory =
            CloudSyncRepository
                .downloadReadingHistory()
                .getOrThrow()

        val historyItems =
            cloudHistory.map { history ->
                ReadingHistoryItem(
                    sourceId =
                        history.sourceId,
                    mangaId =
                        history.mangaId,
                    mangaTitle =
                        history.mangaTitle,
                    mangaCoverUrl =
                        history.mangaCoverUrl,
                    chapterId =
                        history.chapterId,
                    chapterName =
                        history.chapterName,
                    pageIndex =
                        history.pageIndex,
                    totalPages =
                        history.totalPages,
                    readAt =
                        history.readAt
                )
            }

        readingProgressStore.mergeHistory(
            historyItems
        )
    }

    private fun restoreChapterProgress() {
        val cloudProgress =
            CloudSyncRepository
                .downloadChapterProgress()
                .getOrThrow()

        cloudProgress.forEach { progress ->
            readingProgressStore.importChapterProgress(
                sourceId =
                    progress.sourceId,
                chapterId =
                    progress.chapterId,
                pageIndex =
                    progress.pageIndex,
                isRead =
                    progress.isRead
            )
        }
    }

    private fun checkSyncResult(
        result:
        com.andrews.mirai.data.remote.supabase.CloudSyncResult,
        operation: String
    ) {
        if (
            result is
                    com.andrews.mirai.data.remote.supabase
                    .CloudSyncResult.Failure
        ) {
            throw IllegalStateException(
                "Falha ao sincronizar $operation: " +
                        result.message
            )
        }
    }
}