package com.andrews.mirai.data.download

import android.content.Context
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.data.local.download.DownloadedChapterEntity
import com.andrews.mirai.data.local.download.DownloadedMangaEntity
import com.andrews.mirai.data.local.download.MiraiDownloadDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class DownloadRepository(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val downloadDao =
        MiraiDownloadDatabase
            .getInstance(applicationContext)
            .downloadDao()

    private val fileStore =
        DownloadFileStore(applicationContext)

    fun observeDownloadedMangas():
            Flow<List<DownloadedMangaEntity>> {
        return downloadDao.observeMangas()
    }

    fun observeDownloadedChapters(
        sourceId: String,
        mangaId: String
    ): Flow<List<DownloadedChapterEntity>> {
        return downloadDao.observeChapters(
            sourceId = sourceId,
            mangaId = mangaId
        )
    }

    fun observeTotalSizeBytes():
            Flow<Long> {
        return downloadDao
            .observeTotalSizeBytes()
    }

    suspend fun getDownloadedManga(
        sourceId: String,
        mangaId: String
    ): DownloadedMangaEntity? {
        return downloadDao.getManga(
            sourceId = sourceId,
            mangaId = mangaId
        )
    }

    suspend fun getDownloadedChapter(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): DownloadedChapterEntity? {
        return downloadDao.getChapter(
            sourceId = sourceId,
            mangaId = mangaId,
            chapterId = chapterId
        )
    }

    suspend fun registerQueuedDownload(
        request: ChapterDownloadRequest
    ) {
        val currentTime =
            System.currentTimeMillis()

        val existingManga =
            downloadDao.getManga(
                sourceId = request.sourceId,
                mangaId = request.mangaId
            )

        val mangaEntity =
            DownloadedMangaEntity(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                title = request.mangaTitle,
                description =
                    request.mangaDescription,
                coverUrl =
                    request.mangaCoverUrl,
                coverLocalPath =
                    existingManga
                        ?.coverLocalPath,
                author =
                    request.mangaAuthor,
                status =
                    request.mangaStatus,
                type =
                    request.mangaType,
                genres =
                    request.mangaGenres,
                downloadedAt =
                    existingManga
                        ?.downloadedAt
                        ?: currentTime,
                updatedAt = currentTime
            )

        val chapterDirectory =
            fileStore.getChapterDirectory(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                chapterId = request.chapterId
            )

        val existingChapter =
            downloadDao.getChapter(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                chapterId = request.chapterId
            )

        val chapterEntity =
            DownloadedChapterEntity(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                chapterId = request.chapterId,
                chapterName =
                    request.chapterName,
                chapterNumber =
                    request.chapterNumber,
                chapterUrl =
                    request.chapterUrl,
                uploadedAt =
                    request.chapterUploadedAt,
                totalPages =
                    existingChapter
                        ?.totalPages
                        ?: 0,
                downloadedPages =
                    existingChapter
                        ?.downloadedPages
                        ?: 0,
                sizeBytes =
                    existingChapter
                        ?.sizeBytes
                        ?: 0L,
                localDirectoryPath =
                    chapterDirectory
                        .absolutePath,
                status =
                    DownloadStatus.QUEUED,
                progressPercent =
                    existingChapter
                        ?.progressPercent
                        ?: 0,
                createdAt =
                    existingChapter
                        ?.createdAt
                        ?: currentTime,
                updatedAt = currentTime,
                errorMessage = null
            )

        downloadDao.saveManga(
            mangaEntity
        )

        downloadDao.saveChapter(
            chapterEntity
        )
    }

    suspend fun updateCoverLocalPath(
        sourceId: String,
        mangaId: String,
        coverLocalPath: String?
    ) {
        downloadDao.updateCoverLocalPath(
            sourceId = sourceId,
            mangaId = mangaId,
            coverLocalPath = coverLocalPath,
            updatedAt =
                System.currentTimeMillis()
        )
    }

    suspend fun updateProgress(
        request: ChapterDownloadRequest,
        downloadedPages: Int,
        totalPages: Int,
        sizeBytes: Long
    ) {
        val safeTotalPages =
            totalPages.coerceAtLeast(0)

        val safeDownloadedPages =
            downloadedPages.coerceIn(
                minimumValue = 0,
                maximumValue =
                    safeTotalPages
                        .coerceAtLeast(
                            downloadedPages
                        )
            )

        val progressPercent =
            if (safeTotalPages <= 0) {
                0
            } else {
                (
                        safeDownloadedPages *
                                100 /
                                safeTotalPages
                        ).coerceIn(
                        minimumValue = 0,
                        maximumValue = 100
                    )
            }

        downloadDao.updateProgress(
            sourceId = request.sourceId,
            mangaId = request.mangaId,
            chapterId = request.chapterId,
            downloadedPages =
                safeDownloadedPages,
            totalPages =
                safeTotalPages,
            progressPercent =
                progressPercent,
            sizeBytes =
                sizeBytes.coerceAtLeast(0L),
            updatedAt =
                System.currentTimeMillis()
        )
    }

    suspend fun updateStatus(
        request: ChapterDownloadRequest,
        status: DownloadStatus,
        errorMessage: String? = null
    ) {
        downloadDao.updateStatus(
            sourceId = request.sourceId,
            mangaId = request.mangaId,
            chapterId = request.chapterId,
            status = status,
            errorMessage = errorMessage,
            updatedAt =
                System.currentTimeMillis()
        )
    }

    suspend fun markCompleted(
        request: ChapterDownloadRequest
    ) {
        fileStore.markChapterAsCompleted(
            sourceId = request.sourceId,
            mangaId = request.mangaId,
            chapterId = request.chapterId
        )

        val totalSize =
            fileStore.calculateChapterSize(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                chapterId = request.chapterId
            )

        val chapter =
            downloadDao.getChapter(
                sourceId = request.sourceId,
                mangaId = request.mangaId,
                chapterId = request.chapterId
            )

        val totalPages =
            chapter
                ?.totalPages
                ?.coerceAtLeast(0)
                ?: 0

        downloadDao.updateProgress(
            sourceId = request.sourceId,
            mangaId = request.mangaId,
            chapterId = request.chapterId,
            downloadedPages = totalPages,
            totalPages = totalPages,
            progressPercent = 100,
            sizeBytes = totalSize,
            updatedAt =
                System.currentTimeMillis()
        )

        updateStatus(
            request = request,
            status =
                DownloadStatus.COMPLETED
        )
    }

    suspend fun getCompletedPageFiles(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): List<File> {
        if (
            !fileStore.isChapterCompleted(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )
        ) {
            return emptyList()
        }

        return fileStore.getChapterPageFiles(
            sourceId = sourceId,
            mangaId = mangaId,
            chapterId = chapterId
        )
    }

    suspend fun deleteChapter(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): Boolean = withContext(
        Dispatchers.IO
    ) {
        val filesDeleted =
            fileStore.deleteChapter(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        if (!filesDeleted) {
            return@withContext false
        }

        downloadDao.deleteChapter(
            sourceId = sourceId,
            mangaId = mangaId,
            chapterId = chapterId
        )

        val remainingChapters =
            downloadDao.countCompletedChapters(
                sourceId = sourceId,
                mangaId = mangaId
            )

        if (remainingChapters == 0) {
            fileStore.deleteManga(
                sourceId = sourceId,
                mangaId = mangaId
            )

            downloadDao.deleteManga(
                sourceId = sourceId,
                mangaId = mangaId
            )
        }

        true
    }

    suspend fun deleteManga(
        sourceId: String,
        mangaId: String
    ): Boolean = withContext(
        Dispatchers.IO
    ) {
        val filesDeleted =
            fileStore.deleteManga(
                sourceId = sourceId,
                mangaId = mangaId
            )

        if (!filesDeleted) {
            return@withContext false
        }

        downloadDao.deleteChaptersFromManga(
            sourceId = sourceId,
            mangaId = mangaId
        )

        downloadDao.deleteManga(
            sourceId = sourceId,
            mangaId = mangaId
        )

        true
    }

    suspend fun deleteAllDownloads():
            Boolean = withContext(
        Dispatchers.IO
    ) {
        val filesDeleted =
            fileStore.deleteAllDownloads()

        if (!filesDeleted) {
            return@withContext false
        }

        downloadDao.deleteAllChapters()
        downloadDao.deleteAllMangas()

        true
    }

    fun getFileStore():
            DownloadFileStore {
        return fileStore
    }
}