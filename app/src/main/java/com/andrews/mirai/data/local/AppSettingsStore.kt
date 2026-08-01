package com.andrews.mirai.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object AppSettingsStore {

    private const val PREFERENCES_NAME = "mirai_app_settings"
    private const val THEME_MODE_KEY = "theme_mode"

    private var initialized = false

    private lateinit var applicationContext: Context

    private val _themeMode =
        MutableStateFlow(AppThemeMode.SYSTEM)

    val themeMode: StateFlow<AppThemeMode> =
        _themeMode.asStateFlow()

    fun initialize(context: Context) {
        if (initialized) {
            return
        }

        applicationContext =
            context.applicationContext

        initialized = true

        val preferences =
            applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val savedMode = preferences.getString(
            THEME_MODE_KEY,
            AppThemeMode.SYSTEM.name
        )

        _themeMode.value = runCatching {
            AppThemeMode.valueOf(
                savedMode ?: AppThemeMode.SYSTEM.name
            )
        }.getOrDefault(
            AppThemeMode.SYSTEM
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        ensureInitialized()

        _themeMode.value = mode

        applicationContext
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                THEME_MODE_KEY,
                mode.name
            )
            .apply()
    }

    private fun ensureInitialized() {
        check(initialized) {
            "AppSettingsStore não foi inicializado."
        }
    }
}