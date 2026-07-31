package com.andrews.mirai.presentation.reader.progress

import android.content.Context

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
        pageIndex: Int
    ) {
        preferences
            .edit()
            .putInt(chapterId, pageIndex)
            .putBoolean(viewedKey(chapterId), true)
            .apply()
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

    private fun viewedKey(chapterId: String): String {
        return "viewed_$chapterId"
    }
}