package com.andrews.mirai.presentation.reader.progress

import com.andrews.mirai.data.repository.SourceRepository

internal const val READING_PREFERENCES_NAME =
    "mirai_reading_progress"

internal const val READING_HISTORY_KEY =
    "reading_history"

internal const val MAX_READING_HISTORY_ITEMS =
    100

internal fun normalizeReadingSourceId(
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

internal fun detectReadingSourceId(
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

internal fun readingProgressKey(
    sourceId: String,
    chapterId: String
): String {
    return "page|$sourceId|$chapterId"
}

internal fun readingViewedKey(
    sourceId: String,
    chapterId: String
): String {
    return "viewed|$sourceId|$chapterId"
}

internal fun legacyReadingViewedKey(
    chapterId: String
): String {
    return "viewed_$chapterId"
}

internal fun readingHistoryIdentityKey(
    sourceId: String,
    mangaId: String
): String {
    return "$sourceId|$mangaId"
}