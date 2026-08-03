package com.andrews.mirai.presentation.reader.logic

import android.content.Context
import com.andrews.mirai.data.download.OfflineChapterPages
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.ReaderPage

class ReaderPageProvider(
    context: Context,
    private val sourceId: String
) {

    private val offlineChapterPages =
        OfflineChapterPages(
            context.applicationContext
        )

    suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        val offlinePages =
            offlineChapterPages.getPages(
                sourceId = sourceId,
                mangaId = chapter.mangaId,
                chapterId = chapter.id
            )

        if (offlinePages.isNotEmpty()) {
            return offlinePages
        }

        return SourceRepository
            .currentSource
            .getPages(
                chapter = chapter
            )
    }
}