package com.andrews.mirai.data.download

import androidx.work.Data
import androidx.work.workDataOf

object DownloadWorkData {

    private const val KEY_SOURCE_ID =
        "source_id"

    private const val KEY_MANGA_ID =
        "manga_id"

    private const val KEY_CHAPTER_ID =
        "chapter_id"

    fun fromRequest(
        request: ChapterDownloadRequest
    ): Data {
        return workDataOf(
            KEY_SOURCE_ID to request.sourceId,
            KEY_MANGA_ID to request.mangaId,
            KEY_CHAPTER_ID to request.chapterId
        )
    }

    fun toWorkKey(
        data: Data
    ): DownloadWorkKey? {
        val sourceId =
            data.getString(
                KEY_SOURCE_ID
            ) ?: return null

        val mangaId =
            data.getString(
                KEY_MANGA_ID
            ) ?: return null

        val chapterId =
            data.getString(
                KEY_CHAPTER_ID
            ) ?: return null

        return DownloadWorkKey(
            sourceId = sourceId,
            mangaId = mangaId,
            chapterId = chapterId
        )
    }
}