package com.andrews.mirai.data.download

import java.io.File
import java.net.URI

class MangaCoverDownloader(
    private val fileStore: DownloadFileStore,
    private val imageFetcher: DownloadImageFetcher
) {

    suspend fun downloadCover(
        request: ChapterDownloadRequest
    ): File? {
        val coverUrl =
            request
                .mangaCoverUrl
                ?.trim()
                ?.takeIf { url ->
                    url.isNotBlank()
                }
                ?: return null

        val extension =
            findImageExtension(
                coverUrl
            )

        val destinationFile =
            fileStore.getCoverFile(
                sourceId =
                    request.sourceId,
                mangaId =
                    request.mangaId,
                extension =
                    extension
            )

        imageFetcher.downloadToFile(
            imageUrl = coverUrl,
            destinationFile =
                destinationFile,
            referer =
                request.chapterUrl
                    .takeIf { url ->
                        url.isNotBlank()
                    }
        )

        return destinationFile
            .takeIf { file ->
                file.exists() &&
                        file.isFile &&
                        file.length() > 0L
            }
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

            else -> DEFAULT_COVER_EXTENSION
        }
    }

    private companion object {

        const val DEFAULT_COVER_EXTENSION =
            "webp"
    }
}