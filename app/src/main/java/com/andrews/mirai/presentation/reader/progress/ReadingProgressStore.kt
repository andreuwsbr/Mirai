package com.andrews.mirai.presentation.reader.progress

import android.content.Context
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga

class ReadingProgressStore(
    context: Context
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            READING_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun getPage(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ): Int {
        val resolvedSourceId =
            normalizeReadingSourceId(sourceId)

        val newKey =
            readingProgressKey(
                sourceId = resolvedSourceId,
                chapterId = chapterId
            )

        if (preferences.contains(newKey)) {
            return preferences.getInt(
                newKey,
                0
            )
        }

        if (preferences.contains(chapterId)) {
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
            normalizeReadingSourceId(sourceId)

        val safePageIndex =
            pageIndex.coerceAtLeast(0)

        val safeTotalPages =
            totalPages.coerceAtLeast(0)

        preferences
            .edit()
            .putInt(
                readingProgressKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                ),
                safePageIndex
            )
            .putBoolean(
                readingViewedKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                ),
                true
            )
            .apply()

        updateHistoryPage(
            sourceId = resolvedSourceId,
            chapterId = chapterId,
            pageIndex = safePageIndex,
            totalPages = safeTotalPages
        )
    }

    fun markViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(sourceId)

        preferences
            .edit()
            .putBoolean(
                readingViewedKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                ),
                true
            )
            .apply()
    }

    fun markNotViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(sourceId)

        preferences
            .edit()
            .remove(
                readingViewedKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                )
            )
            .remove(
                readingProgressKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                )
            )
            .remove(
                legacyReadingViewedKey(
                    chapterId
                )
            )
            .remove(chapterId)
            .apply()
    }

    fun isViewed(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ): Boolean {
        val resolvedSourceId =
            normalizeReadingSourceId(sourceId)

        val currentViewedKey =
            readingViewedKey(
                sourceId = resolvedSourceId,
                chapterId = chapterId
            )

        val currentProgressKey =
            readingProgressKey(
                sourceId = resolvedSourceId,
                chapterId = chapterId
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
            normalizeReadingSourceId(sourceId)

        val updatedHistory =
            getHistory()
                .filterNot { item ->
                    item.sourceId ==
                            resolvedSourceId &&
                            item.mangaId ==
                            manga.id
                }
                .toMutableList()

        updatedHistory.add(
            index = 0,
            element = ReadingHistoryItem(
                sourceId = resolvedSourceId,
                mangaId = manga.id,
                mangaTitle = manga.title,
                mangaCoverUrl = manga.coverUrl,
                chapterId = chapter.id,
                chapterName = chapter.name,
                pageIndex = getPage(
                    chapterId = chapter.id,
                    sourceId = resolvedSourceId
                ),
                totalPages = 0,
                readAt =
                    System.currentTimeMillis()
            )
        )

        saveHistory(
            updatedHistory.take(
                MAX_READING_HISTORY_ITEMS
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
            getHistory().filterNot { item ->
                item.mangaId == mangaId
            }

        saveHistory(updatedHistory)
    }

    fun removeHistoryItem(
        mangaId: String,
        sourceId: String
    ) {
        val resolvedSourceId =
            normalizeReadingSourceId(sourceId)

        val updatedHistory =
            getHistory().filterNot { item ->
                item.sourceId ==
                        resolvedSourceId &&
                        item.mangaId ==
                        mangaId
            }

        saveHistory(updatedHistory)
    }

    fun clearHistory() {
        preferences
            .edit()
            .remove(
                READING_HISTORY_KEY
            )
            .apply()
    }

    private fun updateHistoryPage(
        sourceId: String,
        chapterId: String,
        pageIndex: Int,
        totalPages: Int
    ) {
        val updatedHistory =
            getHistory()
                .map { item ->
                    if (
                        item.sourceId == sourceId &&
                        item.chapterId == chapterId
                    ) {
                        item.copy(
                            pageIndex = pageIndex,
                            totalPages = totalPages,
                            readAt =
                                System.currentTimeMillis()
                        )
                    } else {
                        item
                    }
                }
                .sortedByDescending { item ->
                    item.readAt
                }

        saveHistory(updatedHistory)
    }

    private fun saveHistory(
        history: List<ReadingHistoryItem>
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
}