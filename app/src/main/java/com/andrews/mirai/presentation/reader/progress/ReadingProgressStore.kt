package com.andrews.mirai.presentation.reader.progress

import android.content.Context
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.json.JSONArray
import org.json.JSONObject

data class ReadingHistoryItem(
    val sourceId: String,
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String?,
    val chapterId: String,
    val chapterName: String,
    val pageIndex: Int,
    val totalPages: Int,
    val readAt: Long
)

class ReadingProgressStore(
    context: Context
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun getPage(
        chapterId: String,
        sourceId: String =
            SourceRepository.currentSource.id
    ): Int {
        val resolvedSourceId =
            normalizeSourceId(sourceId)

        val newKey = progressKey(
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
            normalizeSourceId(sourceId)

        val safePageIndex =
            pageIndex.coerceAtLeast(0)

        val safeTotalPages =
            totalPages.coerceAtLeast(0)

        preferences
            .edit()
            .putInt(
                progressKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                ),
                safePageIndex
            )
            .putBoolean(
                viewedKey(
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
            normalizeSourceId(sourceId)

        preferences
            .edit()
            .putBoolean(
                viewedKey(
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
            normalizeSourceId(sourceId)

        preferences
            .edit()
            .remove(
                viewedKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                )
            )
            .remove(
                progressKey(
                    sourceId = resolvedSourceId,
                    chapterId = chapterId
                )
            )
            .remove(
                legacyViewedKey(chapterId)
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
            normalizeSourceId(sourceId)

        val currentViewedKey =
            viewedKey(
                sourceId = resolvedSourceId,
                chapterId = chapterId
            )

        val currentProgressKey =
            progressKey(
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
                legacyViewedKey(chapterId),
                false
            )

        val legacyProgress =
            preferences.contains(chapterId)

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
            normalizeSourceId(sourceId)

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
                MAX_HISTORY_ITEMS
            )
        )
    }

    fun getHistory():
            List<ReadingHistoryItem> {
        val savedHistory =
            preferences.getString(
                HISTORY_KEY,
                null
            ) ?: return emptyList()

        return runCatching {
            val jsonArray =
                JSONArray(savedHistory)

            buildList {
                for (
                index in 0 until
                        jsonArray.length()
                ) {
                    val item =
                        jsonArray.optJSONObject(
                            index
                        ) ?: continue

                    val mangaId =
                        item.optString(
                            "mangaId"
                        )

                    val chapterId =
                        item.optString(
                            "chapterId"
                        )

                    if (
                        mangaId.isBlank() ||
                        chapterId.isBlank()
                    ) {
                        continue
                    }

                    val savedSourceId =
                        item.optString(
                            "sourceId"
                        )

                    val resolvedSourceId =
                        savedSourceId.ifBlank {
                            detectSourceId(
                                mangaId = mangaId,
                                chapterId = chapterId
                            )
                        }

                    add(
                        ReadingHistoryItem(
                            sourceId =
                                resolvedSourceId,
                            mangaId = mangaId,
                            mangaTitle =
                                item.optString(
                                    "mangaTitle"
                                ),
                            mangaCoverUrl =
                                item
                                    .optString(
                                        "mangaCoverUrl"
                                    )
                                    .takeIf { value ->
                                        value.isNotBlank()
                                    },
                            chapterId = chapterId,
                            chapterName =
                                item.optString(
                                    "chapterName"
                                ),
                            pageIndex =
                                item.optInt(
                                    "pageIndex"
                                ).coerceAtLeast(0),
                            totalPages =
                                item.optInt(
                                    "totalPages"
                                ).coerceAtLeast(0),
                            readAt =
                                item.optLong(
                                    "readAt"
                                )
                        )
                    )
                }
            }
                .distinctBy { item ->
                    historyKey(
                        sourceId = item.sourceId,
                        mangaId = item.mangaId
                    )
                }
                .sortedByDescending { item ->
                    item.readAt
                }
        }.getOrDefault(
            emptyList()
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
            normalizeSourceId(sourceId)

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
            .remove(HISTORY_KEY)
            .apply()
    }

    private fun updateHistoryPage(
        sourceId: String,
        chapterId: String,
        pageIndex: Int,
        totalPages: Int
    ) {
        val history = getHistory()

        val updatedHistory =
            history.map { item ->
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
            }.sortedByDescending { item ->
                item.readAt
            }

        saveHistory(updatedHistory)
    }

    private fun saveHistory(
        history: List<ReadingHistoryItem>
    ) {
        val jsonArray = JSONArray()

        history
            .distinctBy { item ->
                historyKey(
                    sourceId = item.sourceId,
                    mangaId = item.mangaId
                )
            }
            .take(MAX_HISTORY_ITEMS)
            .forEach { item ->
                jsonArray.put(
                    JSONObject().apply {
                        put(
                            "sourceId",
                            item.sourceId
                        )
                        put(
                            "mangaId",
                            item.mangaId
                        )
                        put(
                            "mangaTitle",
                            item.mangaTitle
                        )
                        put(
                            "mangaCoverUrl",
                            item.mangaCoverUrl
                                .orEmpty()
                        )
                        put(
                            "chapterId",
                            item.chapterId
                        )
                        put(
                            "chapterName",
                            item.chapterName
                        )
                        put(
                            "pageIndex",
                            item.pageIndex
                        )
                        put(
                            "totalPages",
                            item.totalPages
                        )
                        put(
                            "readAt",
                            item.readAt
                        )
                    }
                )
            }

        preferences
            .edit()
            .putString(
                HISTORY_KEY,
                jsonArray.toString()
            )
            .apply()
    }

    private fun normalizeSourceId(
        sourceId: String
    ): String {
        return sourceId
            .trim()
            .ifBlank {
                SourceRepository
                    .currentSource
                    .id
            }
    }

    private fun detectSourceId(
        mangaId: String,
        chapterId: String
    ): String {
        val combinedValue =
            "$mangaId $chapterId"
                .lowercase()

        return when {
            combinedValue.contains(
                "mangalivre.blog"
            ) -> {
                "mangalivre"
            }

            combinedValue.contains(
                "astratoons.com"
            ) -> {
                "astraltoons"
            }

            else -> {
                SourceRepository
                    .currentSource
                    .id
            }
        }
    }

    private fun progressKey(
        sourceId: String,
        chapterId: String
    ): String {
        return "page|$sourceId|$chapterId"
    }

    private fun viewedKey(
        sourceId: String,
        chapterId: String
    ): String {
        return "viewed|$sourceId|$chapterId"
    }

    private fun legacyViewedKey(
        chapterId: String
    ): String {
        return "viewed_$chapterId"
    }

    private fun historyKey(
        sourceId: String,
        mangaId: String
    ): String {
        return "$sourceId|$mangaId"
    }

    private companion object {
        const val PREFERENCES_NAME =
            "mirai_reading_progress"

        const val HISTORY_KEY =
            "reading_history"

        const val MAX_HISTORY_ITEMS =
            100
    }
}