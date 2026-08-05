package com.andrews.mirai.presentation.reader.settings

import android.content.Context
import com.andrews.mirai.presentation.reader.display.ReaderOrientationMode

class ReaderSettingsStore(
    context: Context
) {

    private val preferences =
        context.applicationContext
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

    fun load(): ReaderPreferences {
        return ReaderPreferences(
            mode =
                readEnum(
                    key = KEY_MODE,
                    defaultValue =
                        ReaderMode.LONG_STRIP
                ),

            background =
                readEnum(
                    key = KEY_BACKGROUND,
                    defaultValue =
                        ReaderBackground.BLACK
                ),

            showPageNumber =
                preferences.getBoolean(
                    KEY_SHOW_PAGE_NUMBER,
                    true
                ),

            fullscreen =
                preferences.getBoolean(
                    KEY_FULLSCREEN,
                    true
                ),

            keepScreenOn =
                preferences.getBoolean(
                    KEY_KEEP_SCREEN_ON,
                    false
                ),

            longStripGapDp =
                preferences
                    .getInt(
                        KEY_LONG_STRIP_GAP,
                        DEFAULT_LONG_STRIP_GAP
                    )
                    .coerceIn(
                        MINIMUM_LONG_STRIP_GAP,
                        MAXIMUM_LONG_STRIP_GAP
                    ),

            tapMode =
                readEnum(
                    key = KEY_TAP_MODE,
                    defaultValue =
                        ReaderTapMode.TAP_AND_SWIPE
                ),

            tapZoneSize =
                readEnum(
                    key = KEY_TAP_ZONE_SIZE,
                    defaultValue =
                        ReaderTapZoneSize.MEDIUM
                ),

            preloadMode =
                readEnum(
                    key = KEY_PRELOAD_MODE,
                    defaultValue =
                        ReaderPreloadMode.NORMAL
                ),

            orientationMode =
                readEnum(
                    key = KEY_ORIENTATION_MODE,
                    defaultValue =
                        ReaderOrientationMode.AUTOMATIC
                ),

            brightnessPercent =
                preferences
                    .getInt(
                        KEY_BRIGHTNESS_PERCENT,
                        SYSTEM_BRIGHTNESS
                    )
                    .coerceIn(
                        SYSTEM_BRIGHTNESS,
                        MAXIMUM_BRIGHTNESS_PERCENT
                    )
        )
    }

    fun save(
        value: ReaderPreferences
    ) {
        preferences
            .edit()
            .putString(
                KEY_MODE,
                value.mode.name
            )
            .putString(
                KEY_BACKGROUND,
                value.background.name
            )
            .putBoolean(
                KEY_SHOW_PAGE_NUMBER,
                value.showPageNumber
            )
            .putBoolean(
                KEY_FULLSCREEN,
                value.fullscreen
            )
            .putBoolean(
                KEY_KEEP_SCREEN_ON,
                value.keepScreenOn
            )
            .putInt(
                KEY_LONG_STRIP_GAP,
                value.longStripGapDp
                    .coerceIn(
                        MINIMUM_LONG_STRIP_GAP,
                        MAXIMUM_LONG_STRIP_GAP
                    )
            )
            .putString(
                KEY_TAP_MODE,
                value.tapMode.name
            )
            .putString(
                KEY_TAP_ZONE_SIZE,
                value.tapZoneSize.name
            )
            .putString(
                KEY_PRELOAD_MODE,
                value.preloadMode.name
            )
            .putString(
                KEY_ORIENTATION_MODE,
                value.orientationMode.name
            )
            .putInt(
                KEY_BRIGHTNESS_PERCENT,
                value.brightnessPercent
                    .coerceIn(
                        SYSTEM_BRIGHTNESS,
                        MAXIMUM_BRIGHTNESS_PERCENT
                    )
            )
            .apply()
    }

    private inline fun <
            reified T : Enum<T>
            > readEnum(
        key: String,
        defaultValue: T
    ): T {
        val savedName =
            preferences.getString(
                key,
                defaultValue.name
            )

        return enumValues<T>()
            .firstOrNull { value ->
                value.name == savedName
            }
            ?: defaultValue
    }

    private companion object {

        const val PREFERENCES_NAME =
            "mirai_reader_settings"

        const val KEY_MODE =
            "mode"

        const val KEY_BACKGROUND =
            "background"

        const val KEY_SHOW_PAGE_NUMBER =
            "show_page_number"

        const val KEY_FULLSCREEN =
            "fullscreen"

        const val KEY_KEEP_SCREEN_ON =
            "keep_screen_on"

        const val KEY_LONG_STRIP_GAP =
            "long_strip_gap"

        const val KEY_TAP_MODE =
            "tap_mode"

        const val KEY_TAP_ZONE_SIZE =
            "tap_zone_size"

        const val KEY_PRELOAD_MODE =
            "preload_mode"

        const val KEY_ORIENTATION_MODE =
            "orientation_mode"

        const val KEY_BRIGHTNESS_PERCENT =
            "brightness_percent"

        const val DEFAULT_LONG_STRIP_GAP =
            12

        const val MINIMUM_LONG_STRIP_GAP =
            0

        const val MAXIMUM_LONG_STRIP_GAP =
            40

        const val SYSTEM_BRIGHTNESS =
            0

        const val MAXIMUM_BRIGHTNESS_PERCENT =
            100
    }
}