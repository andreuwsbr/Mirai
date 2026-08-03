package com.andrews.mirai.data.download

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.andrews.mirai.data.local.download.DownloadStatus
import java.util.concurrent.TimeUnit

class ChapterDownloadManager(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val workManager =
        WorkManager.getInstance(
            applicationContext
        )

    private val repository =
        DownloadRepository(
            applicationContext
        )

    suspend fun enqueue(
        request: ChapterDownloadRequest
    ) {
        repository.registerQueuedDownload(
            request
        )

        val workRequest =
            createWorkRequest(
                request
            )

        workManager.enqueueUniqueWork(
            uniqueWorkName(request),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    suspend fun retry(
        request: ChapterDownloadRequest
    ) {
        repository.updateStatus(
            request = request,
            status = DownloadStatus.QUEUED,
            errorMessage = null
        )

        val workRequest =
            createWorkRequest(
                request
            )

        workManager.enqueueUniqueWork(
            uniqueWorkName(request),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    suspend fun pause(
        request: ChapterDownloadRequest
    ) {
        workManager.cancelUniqueWork(
            uniqueWorkName(request)
        )

        repository.updateStatus(
            request = request,
            status = DownloadStatus.PAUSED,
            errorMessage =
                "Download pausado."
        )
    }

    fun cancelWork(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ) {
        workManager.cancelUniqueWork(
            uniqueWorkName(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )
        )
    }

    fun cancelAllDownloads() {
        workManager.cancelAllWorkByTag(
            DOWNLOAD_WORK_TAG
        )
    }

    private fun createWorkRequest(
        request: ChapterDownloadRequest
    ): OneTimeWorkRequest {
        val networkConstraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        return OneTimeWorkRequestBuilder<
                ChapterDownloadWorker
                >()
            .setInputData(
                DownloadWorkData.fromRequest(
                    request
                )
            )
            .setConstraints(
                networkConstraints
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                MINIMUM_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(
                DOWNLOAD_WORK_TAG
            )
            .addTag(
                mangaWorkTag(
                    sourceId =
                        request.sourceId,
                    mangaId =
                        request.mangaId
                )
            )
            .addTag(
                chapterWorkTag(
                    sourceId =
                        request.sourceId,
                    mangaId =
                        request.mangaId,
                    chapterId =
                        request.chapterId
                )
            )
            .build()
    }

    private fun uniqueWorkName(
        request: ChapterDownloadRequest
    ): String {
        return uniqueWorkName(
            sourceId = request.sourceId,
            mangaId = request.mangaId,
            chapterId = request.chapterId
        )
    }

    private fun uniqueWorkName(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): String {
        return listOf(
            UNIQUE_WORK_PREFIX,
            safeValue(sourceId),
            safeValue(mangaId),
            safeValue(chapterId)
        ).joinToString(
            separator = "_"
        )
    }

    private fun mangaWorkTag(
        sourceId: String,
        mangaId: String
    ): String {
        return listOf(
            MANGA_WORK_TAG_PREFIX,
            safeValue(sourceId),
            safeValue(mangaId)
        ).joinToString(
            separator = "_"
        )
    }

    private fun chapterWorkTag(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): String {
        return listOf(
            CHAPTER_WORK_TAG_PREFIX,
            safeValue(sourceId),
            safeValue(mangaId),
            safeValue(chapterId)
        ).joinToString(
            separator = "_"
        )
    }

    private fun safeValue(
        value: String
    ): String {
        return value
            .trim()
            .replace(
                INVALID_NAME_CHARACTERS,
                "_"
            )
            .take(
                MAXIMUM_NAME_LENGTH
            )
            .ifBlank {
                "unknown"
            }
    }

    private companion object {

        const val DOWNLOAD_WORK_TAG =
            "mirai_chapter_download"

        const val UNIQUE_WORK_PREFIX =
            "mirai_download"

        const val MANGA_WORK_TAG_PREFIX =
            "mirai_download_manga"

        const val CHAPTER_WORK_TAG_PREFIX =
            "mirai_download_chapter"

        const val MINIMUM_BACKOFF_SECONDS =
            10L

        const val MAXIMUM_NAME_LENGTH =
            100

        val INVALID_NAME_CHARACTERS =
            Regex(
                pattern =
                    """[^a-zA-Z0-9._-]"""
            )
    }
}