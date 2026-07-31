package com.andrews.mirai.presentation.reader.progress

import android.content.Context
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import org.json.JSONArray
import org.json.JSONObject

data class ReadingHistoryItem(
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
    private val preferences = context.getSharedPreferences(
        "mirai_reading_progress",
        Context.MODE_PRIVATE
    )

    fun getPage(chapterId: String): Int {
        return preferences.getInt(chapterId, 0)
    }

    fun savePage(
        chapterId: String,
        pageIndex: Int,
        totalPages: Int = 0
    ) {
        preferences
            .edit()
            .putInt(chapterId, pageIndex)
            .putBoolean(viewedKey(chapterId), true)
            .apply()

        updateHistoryPage(
            chapterId = chapterId,
            pageIndex = pageIndex,
            totalPages = totalPages
        )
    }

    fun markViewed(chapterId: String) {
        preferences
            .edit()
            .putBoolean(viewedKey(chapterId), true)
            .apply()
    }

    fun isViewed(chapterId: String): Boolean {
        return preferences.getBoolean(
            viewedKey(chapterId),
            false
        ) || preferences.contains(chapterId)
    }

    fun registerReading(
        manga: Manga,
        chapter: Chapter
    ) {
        val history = getHistory()
            .filterNot { item ->
                item.chapterId == chapter.id
            }
            .toMutableList()

        history.add(
            index = 0,
            element = ReadingHistoryItem(
                mangaId = manga.id,
                mangaTitle = manga.title,
                mangaCoverUrl = manga.coverUrl,
                chapterId = chapter.id,
                chapterName = chapter.name,
                pageIndex = getPage(chapter.id),
                totalPages = 0,
                readAt = System.currentTimeMillis()
            )
        )

        saveHistory(
            history = history.take(MAX_HISTORY_ITEMS)
        )
    }

    fun getHistory(): List<ReadingHistoryItem> {
        val savedHistory = preferences.getString(
            HISTORY_KEY,
            null
        ) ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(savedHistory)

            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)

                    add(
                        ReadingHistoryItem(
                            mangaId = item.optString("mangaId"),
                            mangaTitle = item.optString("mangaTitle"),
                            mangaCoverUrl = item
                                .optString("mangaCoverUrl")
                                .takeIf { it.isNotBlank() },
                            chapterId = item.optString("chapterId"),
                            chapterName = item.optString("chapterName"),
                            pageIndex = item.optInt("pageIndex"),
                            totalPages = item.optInt("totalPages"),
                            readAt = item.optLong("readAt")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun clearHistory() {
        preferences
            .edit()
            .remove(HISTORY_KEY)
            .apply()
    }

    private fun updateHistoryPage(
        chapterId: String,
        pageIndex: Int,
        totalPages: Int
    ) {
        val history = getHistory()

        if (history.none { item ->
                item.chapterId == chapterId
            }
        ) {
            return
        }

        val updatedHistory = history
            .map { item ->
                if (item.chapterId == chapterId) {
                    item.copy(
                        pageIndex = pageIndex,
                        totalPages = totalPages,
                        readAt = System.currentTimeMillis()
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
        val jsonArray = JSONArray()

        history.forEach { item ->
            jsonArray.put(
                JSONObject().apply {
                    put("mangaId", item.mangaId)
                    put("mangaTitle", item.mangaTitle)
                    put(
                        "mangaCoverUrl",
                        item.mangaCoverUrl.orEmpty()
                    )
                    put("chapterId", item.chapterId)
                    put("chapterName", item.chapterName)
                    put("pageIndex", item.pageIndex)
                    put("totalPages", item.totalPages)
                    put("readAt", item.readAt)
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

    private fun viewedKey(chapterId: String): String {
        return "viewed_$chapterId"
    }

    private companion object {
        const val HISTORY_KEY = "reading_history"
        const val MAX_HISTORY_ITEMS = 100
    }
}