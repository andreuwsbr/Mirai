package com.andrews.mirai.presentation.reader.retry

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.cache.ReaderImageCache
import com.andrews.mirai.presentation.reader.logic.ReaderChapterController

class ReaderRetryController(
    private val chapterController:
    ReaderChapterController,
    private val imageCache:
    ReaderImageCache
) {

    suspend fun retryChapter(
        chapter: Chapter
    ) {
        chapterController.retryChapter(
            chapter
        )
    }

    fun clearFailedPage(
        imageUrl: String
    ) {
        imageCache.deleteImage(
            imageUrl
        )

        imageCache.deleteTemporaryImage(
            imageUrl
        )
    }
}