package com.andrews.mirai.presentation.reader.progress

import android.content.Context
import android.util.Log
import com.andrews.mirai.data.repository.CloudSyncRepository
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.data.remote.supabase.CloudSyncResult
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReadingProgressStore(
    context: Context
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            READING_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    private val syncScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    fun getPage(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ): Int {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val newKey =
            readingProgressKey(
                sourceId = resolvedSourceId,
                chapterId = chapterId
            )

        if (
            preferences.contains(
                newKey
            )
        ) {
            return preferences.getInt(
                newKey,
                0
            )
        }

        if (
            preferences.contains(
                chapterId
            )
        ) {
            val legacyPage =
                preferences.getInt(
                    chapterId,
                    0
                )

            preferences
                .edit()
                .putInt(
                    newKey,
                    legacyPage
                )
                .apply()

            return legacyPage
        }

        return 0
    }

    fun savePage(
        chapterId: String,
        pageIndex: Int,
        totalPages: Int = 0,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val safePageIndex =
            pageIndex.coerceAtLeast(
                0
            )

        val safeTotalPages =
            totalPages.coerceAtLeast(
                0
            )

        preferences
            .edit()
            .putInt(
                readingProgressKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                ),
                safePageIndex
            )
            .putBoolean(
                readingViewedKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                ),
                true
            )
            .apply()

        val updatedItem =
            updateHistoryPage(
                sourceId =
                    resolvedSourceId,
                chapterId =
                    chapterId,
                pageIndex =
                    safePageIndex,
                totalPages =
                    safeTotalPages
            )

        if (updatedItem != null) {
            synchronizeHistory(
                updatedItem
            )

            synchronizeChapterProgress(
                item = updatedItem,
                isRead =
                    isChapterFinished(
                        pageIndex =
                            safePageIndex,
                        totalPages =
                            safeTotalPages
                    )
            )
        }
    }

    fun markViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        preferences
            .edit()
            .putBoolean(
                readingViewedKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                ),
                true
            )
            .apply()

        val item =
            getHistory()
                .firstOrNull {
                        historyItem ->
                    historyItem.sourceId ==
                            resolvedSourceId &&
                            historyItem.chapterId ==
                            chapterId
                }

        if (item != null) {
            synchronizeChapterProgress(
                item = item,
                isRead = true
            )
        }
    }

    fun markNotViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        preferences
            .edit()
            .remove(
                readingViewedKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                )
            )
            .remove(
                readingProgressKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                )
            )
            .remove(
                legacyReadingViewedKey(
                    chapterId
                )
            )
            .remove(
                chapterId
            )
            .apply()

        val item =
            getHistory()
                .firstOrNull {
                        historyItem ->
                    historyItem.sourceId ==
                            resolvedSourceId &&
                            historyItem.chapterId ==
                            chapterId
                }

        if (item != null) {
            synchronizeChapterProgress(
                item = item.copy(
                    pageIndex = 0
                ),
                isRead = false
            )
        }
    }

    fun isViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ): Boolean {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val currentViewedKey =
            readingViewedKey(
                sourceId =
                    resolvedSourceId,
                chapterId =
                    chapterId
            )

        val currentProgressKey =
            readingProgressKey(
                sourceId =
                    resolvedSourceId,
                chapterId =
                    chapterId
            )

        if (
            preferences.getBoolean(
                currentViewedKey,
                false
            ) ||
            preferences.contains(
                currentProgressKey
            )
        ) {
            return true
        }

        val legacyViewed =
            preferences.getBoolean(
                legacyReadingViewedKey(
                    chapterId
                ),
                false
            )

        val legacyProgress =
            preferences.contains(
                chapterId
            )

        if (
            legacyViewed ||
            legacyProgress
        ) {
            preferences
                .edit()
                .putBoolean(
                    currentViewedKey,
                    true
                )
                .apply()

            return true
        }

        return false
    }

    fun registerReading(
        manga: Manga,
        chapter: Chapter,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val historyItem =
            ReadingHistoryItem(
                sourceId =
                    resolvedSourceId,
                mangaId =
                    manga.id,
                mangaTitle =
                    manga.title,
                mangaCoverUrl =
                    manga.coverUrl,
                chapterId =
                    chapter.id,
                chapterName =
                    chapter.name,
                pageIndex =
                    getPage(
                        chapterId =
                            chapter.id,
                        sourceId =
                            resolvedSourceId
                    ),
                totalPages = 0,
                readAt =
                    System.currentTimeMillis()
            )

        val updatedHistory =
            getHistory()
                .filterNot {
                        item ->
                    item.sourceId ==
                            resolvedSourceId &&
                            item.mangaId ==
                            manga.id
                }
                .toMutableList()

        updatedHistory.add(
            index = 0,
            element =
                historyItem
        )

        saveHistory(
            updatedHistory.take(
                MAX_READING_HISTORY_ITEMS
            )
        )

        synchronizeHistory(
            historyItem
        )

        synchronizeChapterProgress(
            item =
                historyItem,
            isRead =
                isViewed(
                    chapterId =
                        chapter.id,
                    sourceId =
                        resolvedSourceId
                )
        )
    }

    fun getHistory():
            List<ReadingHistoryItem> {
        val savedHistory =
            preferences.getString(
                READING_HISTORY_KEY,
                null
            ) ?: return emptyList()

        return decodeReadingHistory(
            savedHistory
        )
    }

    fun removeHistoryItem(
        mangaId: String
    ) {
        val updatedHistory =
            getHistory()
                .filterNot {
                        item ->
                    item.mangaId ==
                            mangaId
                }

        saveHistory(
            updatedHistory
        )
    }

    fun removeHistoryItem(
        mangaId: String,
        sourceId: String
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val updatedHistory =
            getHistory()
                .filterNot {
                        item ->
                    item.sourceId ==
                            resolvedSourceId &&
                            item.mangaId ==
                            mangaId
                }

        saveHistory(
            updatedHistory
        )
    }

    fun clearHistory() {
        preferences
            .edit()
            .remove(
                READING_HISTORY_KEY
            )
            .apply()
    }

    fun mergeHistory(
        cloudHistory:
        List<ReadingHistoryItem>
    ) {
        val merged =
            (
                    getHistory() +
                            cloudHistory
                    )
                .groupBy {
                        item ->
                    readingHistoryIdentityKey(
                        sourceId =
                            item.sourceId,
                        mangaId =
                            item.mangaId
                    )
                }
                .mapNotNull {
                        (_, items) ->
                    items.maxByOrNull {
                            item ->
                        item.readAt
                    }
                }
                .sortedByDescending {
                        item ->
                    item.readAt
                }
                .take(
                    MAX_READING_HISTORY_ITEMS
                )

        saveHistory(
            merged
        )
    }

    fun importChapterProgress(
        sourceId: String,
        chapterId: String,
        pageIndex: Int,
        isRead: Boolean
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(
                sourceId
            )

        val editor =
            preferences
                .edit()
                .putInt(
                    readingProgressKey(
                        sourceId =
                            resolvedSourceId,
                        chapterId =
                            chapterId
                    ),
                    pageIndex.coerceAtLeast(
                        0
                    )
                )

        if (isRead) {
            editor.putBoolean(
                readingViewedKey(
                    sourceId =
                        resolvedSourceId,
                    chapterId =
                        chapterId
                ),
                true
            )
        }

        editor.apply()
    }

    private fun updateHistoryPage(
        sourceId: String,
        chapterId: String,
        pageIndex: Int,
        totalPages: Int
    ): ReadingHistoryItem? {
        var updatedItem:
                ReadingHistoryItem? =
            null

        val updatedHistory =
            getHistory()
                .map {
                        item ->
                    if (
                        item.sourceId ==
                        sourceId &&
                        item.chapterId ==
                        chapterId
                    ) {
                        item.copy(
                            pageIndex =
                                pageIndex,
                            totalPages =
                                totalPages,
                            readAt =
                                System
                                    .currentTimeMillis()
                        ).also {
                                changedItem ->
                            updatedItem =
                                changedItem
                        }
                    } else {
                        item
                    }
                }
                .sortedByDescending {
                        item ->
                    item.readAt
                }

        saveHistory(
            updatedHistory
        )

        return updatedItem
    }

    private fun saveHistory(
        history:
        List<ReadingHistoryItem>
    ) {
        preferences
            .edit()
            .putString(
                READING_HISTORY_KEY,
                encodeReadingHistory(
                    history
                )
            )
            .apply()
    }

    private fun synchronizeHistory(
        item: ReadingHistoryItem
    ) {
        if (
            !CloudSyncRepository
                .isAvailable()
        ) {
            return
        }

        syncScope.launch {
            when (
                val result =
                    CloudSyncRepository
                        .uploadReadingHistory(
                            item
                        )
            ) {
                CloudSyncResult.Success -> {
                    Log.d(
                        "MiraiCloudSync",
                        "Histórico enviado: " +
                                item.mangaTitle
                    )
                }

                is CloudSyncResult.Failure -> {
                    Log.e(
                        "MiraiCloudSync",
                        "Falha no histórico. " +
                                "Código: " +
                                "${result.statusCode}. " +
                                "Erro: " +
                                result.message
                    )
                }
            }
        }
    }

    private fun synchronizeChapterProgress(
        item: ReadingHistoryItem,
        isRead: Boolean
    ) {
        if (
            !CloudSyncRepository
                .isAvailable()
        ) {
            return
        }

        syncScope.launch {
            when (
                val result =
                    CloudSyncRepository
                        .uploadChapterProgress(
                            sourceId =
                                item.sourceId,
                            mangaId =
                                item.mangaId,
                            chapterId =
                                item.chapterId,
                            chapterName =
                                item.chapterName,
                            pageIndex =
                                item.pageIndex,
                            totalPages =
                                item.totalPages,
                            isRead =
                                isRead
                        )
            ) {
                CloudSyncResult.Success -> {
                    Log.d(
                        "MiraiCloudSync",
                        "Progresso enviado: " +
                                item.chapterName
                    )
                }

                is CloudSyncResult.Failure -> {
                    Log.e(
                        "MiraiCloudSync",
                        "Falha no progresso. " +
                                "Código: " +
                                "${result.statusCode}. " +
                                "Erro: " +
                                result.message
                    )
                }
            }
        }
    }

    private fun isChapterFinished(
        pageIndex: Int,
        totalPages: Int
    ): Boolean {
        if (totalPages <= 0) {
            return false
        }

        return pageIndex >=
                totalPages - 1
    }
}