package com.andrews.mirai.data.download

import android.content.Context
import com.andrews.mirai.domain.model.ReaderPage

class OfflineChapterPages(
    context: Context
) {

    private val repository =
        DownloadRepository(
            context.applicationContext
        )

    suspend fun getPages(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): List<ReaderPage> {
        return repository
            .getCompletedPageFiles(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )
            .mapIndexed { index, file ->
                ReaderPage(
                    index = index,
                    imageUrl =
                        file.toURI().toString()
                )
            }
    }
}