package com.andrews.mirai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.data.source.SourceRegistry

object SourceRepository {

    private const val PREFERENCES_NAME =
        "mirai_source_preferences"

    private const val CURRENT_SOURCE_KEY =
        "current_source_id"

    private var preferences: SharedPreferences? = null

    val sources: List<MangaSource>
        get() = SourceRegistry.all()

    var currentSource: MangaSource =
        SourceRegistry.default()
        set(value) {
            field = value

            preferences
                ?.edit()
                ?.putString(
                    CURRENT_SOURCE_KEY,
                    value.id
                )
                ?.apply()
        }

    fun initialize(
        context: Context
    ) {
        preferences = context
            .applicationContext
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val savedSourceId = preferences
            ?.getString(
                CURRENT_SOURCE_KEY,
                null
            )

        currentSource = savedSourceId
            ?.let(SourceRegistry::findById)
            ?: SourceRegistry.default()
    }

    fun selectSource(
        sourceId: String
    ): Boolean {
        val source = SourceRegistry.findById(
            sourceId
        ) ?: return false

        currentSource = source

        return true
    }
}