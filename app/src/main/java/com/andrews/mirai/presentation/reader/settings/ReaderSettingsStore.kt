package com.andrews.mirai.presentation.reader.settings

import android.content.Context

class ReaderSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "mirai_reader_settings",
        Context.MODE_PRIVATE
    )

    fun load(): ReaderPreferences {
        val mode = runCatching {
            ReaderMode.valueOf(
                preferences.getString("mode", ReaderMode.LONG_STRIP.name)
                    ?: ReaderMode.LONG_STRIP.name
            )
        }.getOrDefault(ReaderMode.LONG_STRIP)

        val background = runCatching {
            ReaderBackground.valueOf(
                preferences.getString("background", ReaderBackground.BLACK.name)
                    ?: ReaderBackground.BLACK.name
            )
        }.getOrDefault(ReaderBackground.BLACK)

        return ReaderPreferences(
            mode = mode,
            background = background,
            showPageNumber = preferences.getBoolean("show_page_number", true),
            fullscreen = preferences.getBoolean("fullscreen", true),
            keepScreenOn = preferences.getBoolean("keep_screen_on", false),
            longStripGapDp = preferences.getInt("long_strip_gap", 12)
        )
    }

    fun save(value: ReaderPreferences) {
        preferences.edit()
            .putString("mode", value.mode.name)
            .putString("background", value.background.name)
            .putBoolean("show_page_number", value.showPageNumber)
            .putBoolean("fullscreen", value.fullscreen)
            .putBoolean("keep_screen_on", value.keepScreenOn)
            .putInt("long_strip_gap", value.longStripGapDp)
            .apply()
    }
}
