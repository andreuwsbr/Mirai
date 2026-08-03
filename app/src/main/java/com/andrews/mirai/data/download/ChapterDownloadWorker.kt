package com.andrews.mirai.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.data.source.SourceRegistry
import com.andrews.mirai.domain.model.Chapter
import java.io.IOException
import java.net.URI

class ChapterDownloadWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
) {

    private val repository =
        DownloadRepository(
            applicationContext
        )

    private val fileStore =
        repository.getFileStore()

    private val imageFetcher =
        DownloadImageFetcher()

    override suspend fun doWork():
            Result {
        val workKey =
            DownloadWorkData.toWorkKey(
                inputData
            )
                ?: return Result.failure()

        val mangaEntity =
            repository.getDownloadedManga(
                sourceId = workKey.sourceId,
                mangaId = workKey.mangaId
            )
                ?: return Result.failure()

        val chapterEntity =
            repository.getDownloadedChapter(
                sourceId = workKey.sourceId,
                mangaId = workKey.mangaId,
                chapterId = workKey.chapterId
            )
                ?: return Result.failure()

        val request =
            ChapterDownloadRequest(
                sourceId = mangaEntity.sourceId,
                mangaId = mangaEntity.mangaId,
                mangaTitle = mangaEntity.title,
                mangaDescription =
                    mangaEntity.description,
                mangaCoverUrl =
                    mangaEntity.coverUrl,
                mangaAuthor =
                    mangaEntity.author,
                mangaStatus =
                    mangaEntity.status,
                mangaType =
                    mangaEntity.type,
                mangaGenres =
                    mangaEntity.genres,
                chapterId =
                    chapterEntity.chapterId,
                chapterName =
                    chapterEntity.chapterName,
                chapterNumber =
                    chapterEntity.chapterNumber,
                chapterUrl =
                    chapterEntity.chapterUrl,
                chapterUploadedAt =
                    chapterEntity.uploadedAt
            )

        val source =
            SourceRegistry.findById(
                workKey.sourceId
            )
                ?: return failDownload(
                    request = request,
                    message =
                        "A fonte deste capítulo não foi encontrada."
                )

        return try {
            repository.updateStatus(
                request = request,
                status =
                    DownloadStatus.DOWNLOADING
            )

            val chapter =
                Chapter(
                    id = chapterEntity.chapterId,
                    mangaId =
                        chapterEntity.mangaId,
                    name =
                        chapterEntity.chapterName,
                    number =
                        chapterEntity.chapterNumber,
                    url =
                        chapterEntity.chapterUrl,
                    uploadedAt =
                        chapterEntity.uploadedAt
                )

            /*
             * Mantemos exatamente a ordem retornada
             * pela fonte. Não ordenamos pelos nomes
             * ou URLs das imagens.
             */
            val pages =
                source.getPages(
                    chapter
                )

            if (pages.isEmpty()) {
                return failDownload(
                    request = request,
                    message =
                        "A fonte não retornou páginas para este capítulo."
                )
            }

            var downloadedPages = 0
            var downloadedSizeBytes = 0L

            pages.forEachIndexed {
                    pagePosition,
                    page ->

                if (isStopped) {
                    repository.updateStatus(
                        request = request,
                        status =
                            DownloadStatus.PAUSED,
                        errorMessage =
                            "Download interrompido."
                    )

                    return Result.failure()
                }

                val extension =
                    findImageExtension(
                        page.imageUrl
                    )

                val destinationFile =
                    fileStore.getPageFile(
                        sourceId =
                            workKey.sourceId,
                        mangaId =
                            workKey.mangaId,
                        chapterId =
                            workKey.chapterId,
                        pageIndex =
                            pagePosition,
                        extension =
                            extension
                    )

                imageFetcher.downloadToFile(
                    imageUrl =
                        page.imageUrl,
                    destinationFile =
                        destinationFile,
                    referer =
                        chapterEntity.chapterUrl
                )

                downloadedPages += 1

                downloadedSizeBytes +=
                    destinationFile.length()

                repository.updateProgress(
                    request = request,
                    downloadedPages =
                        downloadedPages,
                    totalPages =
                        pages.size,
                    sizeBytes =
                        downloadedSizeBytes
                )

                setProgress(
                    workDataOf(
                        PROGRESS_DOWNLOADED_PAGES to
                                downloadedPages,
                        PROGRESS_TOTAL_PAGES to
                                pages.size,
                        PROGRESS_PERCENT to
                                calculateProgress(
                                    downloadedPages =
                                        downloadedPages,
                                    totalPages =
                                        pages.size
                                )
                    )
                )
            }

            repository.markCompleted(
                request
            )

            Result.success(
                workDataOf(
                    PROGRESS_DOWNLOADED_PAGES to
                            pages.size,
                    PROGRESS_TOTAL_PAGES to
                            pages.size,
                    PROGRESS_PERCENT to 100
                )
            )
        } catch (exception: IOException) {
            repository.updateStatus(
                request = request,
                status =
                    DownloadStatus.QUEUED,
                errorMessage =
                    exception.message
                        ?: "Falha de conexão."
            )

            Result.retry()
        } catch (exception: Exception) {
            failDownload(
                request = request,
                message =
                    exception.message
                        ?: "Não foi possível baixar o capítulo."
            )
        }
    }

    private suspend fun failDownload(
        request: ChapterDownloadRequest,
        message: String
    ): Result {
        repository.updateStatus(
            request = request,
            status =
                DownloadStatus.FAILED,
            errorMessage = message
        )

        return Result.failure(
            workDataOf(
                OUTPUT_ERROR_MESSAGE to
                        message
            )
        )
    }

    private fun calculateProgress(
        downloadedPages: Int,
        totalPages: Int
    ): Int {
        if (totalPages <= 0) {
            return 0
        }

        return (
                downloadedPages *
                        100 /
                        totalPages
                ).coerceIn(
                minimumValue = 0,
                maximumValue = 100
            )
    }

    private fun findImageExtension(
        imageUrl: String
    ): String {
        val path =
            runCatching {
                URI(imageUrl).path
            }.getOrNull()
                .orEmpty()

        val extension =
            path
                .substringAfterLast(
                    delimiter = '.',
                    missingDelimiterValue = ""
                )
                .lowercase()

        return when (extension) {
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif",
            "avif" -> extension

            else -> DEFAULT_IMAGE_EXTENSION
        }
    }

    companion object {

        const val PROGRESS_DOWNLOADED_PAGES =
            "downloaded_pages"

        const val PROGRESS_TOTAL_PAGES =
            "total_pages"

        const val PROGRESS_PERCENT =
            "progress_percent"

        const val OUTPUT_ERROR_MESSAGE =
            "error_message"

        private const val DEFAULT_IMAGE_EXTENSION =
            "img"
    }
}