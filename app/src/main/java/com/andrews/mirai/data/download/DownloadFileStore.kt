package com.andrews.mirai.data.download

import android.content.Context
import java.io.File

class DownloadFileStore(
    context: Context
) {

    private val downloadsRoot =
        File(
            context.applicationContext.filesDir,
            DOWNLOADS_DIRECTORY_NAME
        )

    fun getMangaDirectory(
        sourceId: String,
        mangaId: String
    ): File {
        return File(
            downloadsRoot,
            "${safeName(sourceId)}/${safeName(mangaId)}"
        )
    }

    fun getChapterDirectory(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): File {
        return File(
            getMangaDirectory(
                sourceId = sourceId,
                mangaId = mangaId
            ),
            "chapters/${safeName(chapterId)}"
        )
    }

    fun prepareChapterDirectory(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): File {
        val directory =
            getChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        if (!directory.exists()) {
            check(directory.mkdirs()) {
                "Não foi possível criar a pasta do capítulo."
            }
        }

        return directory
    }

    fun getPageFile(
        sourceId: String,
        mangaId: String,
        chapterId: String,
        pageIndex: Int,
        extension: String
    ): File {
        val directory =
            prepareChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        val normalizedExtension =
            extension
                .trim()
                .removePrefix(".")
                .ifBlank {
                    DEFAULT_IMAGE_EXTENSION
                }

        val fileName =
            pageIndex
                .plus(1)
                .toString()
                .padStart(
                    length = PAGE_NUMBER_LENGTH,
                    padChar = '0'
                ) +
                    "." +
                    normalizedExtension

        return File(
            directory,
            fileName
        )
    }

    fun getCoverFile(
        sourceId: String,
        mangaId: String,
        extension: String
    ): File {
        val mangaDirectory =
            getMangaDirectory(
                sourceId = sourceId,
                mangaId = mangaId
            )

        if (!mangaDirectory.exists()) {
            check(mangaDirectory.mkdirs()) {
                "Não foi possível criar a pasta da obra."
            }
        }

        val normalizedExtension =
            extension
                .trim()
                .removePrefix(".")
                .ifBlank {
                    DEFAULT_IMAGE_EXTENSION
                }

        return File(
            mangaDirectory,
            "cover.$normalizedExtension"
        )
    }

    fun markChapterAsCompleted(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ) {
        val directory =
            prepareChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        File(
            directory,
            COMPLETED_FILE_NAME
        ).writeText(
            text = "completed"
        )
    }

    fun isChapterCompleted(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): Boolean {
        val directory =
            getChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        return File(
            directory,
            COMPLETED_FILE_NAME
        ).exists()
    }

    fun getChapterPageFiles(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): List<File> {
        val directory =
            getChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        if (!directory.exists()) {
            return emptyList()
        }

        return directory
            .listFiles()
            .orEmpty()
            .filter { file ->
                file.isFile &&
                        file.name !=
                        COMPLETED_FILE_NAME
            }
            .sortedBy { file ->
                file.name
            }
    }

    fun calculateChapterSize(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): Long {
        return directorySize(
            getChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )
        )
    }

    fun calculateMangaSize(
        sourceId: String,
        mangaId: String
    ): Long {
        return directorySize(
            getMangaDirectory(
                sourceId = sourceId,
                mangaId = mangaId
            )
        )
    }

    fun calculateTotalSize(): Long {
        return directorySize(
            downloadsRoot
        )
    }

    fun deleteChapter(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): Boolean {
        val directory =
            getChapterDirectory(
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId
            )

        return !directory.exists() ||
                directory.deleteRecursively()
    }

    fun deleteManga(
        sourceId: String,
        mangaId: String
    ): Boolean {
        val directory =
            getMangaDirectory(
                sourceId = sourceId,
                mangaId = mangaId
            )

        return !directory.exists() ||
                directory.deleteRecursively()
    }

    fun deleteAllDownloads(): Boolean {
        if (!downloadsRoot.exists()) {
            return true
        }

        val deleted =
            downloadsRoot.deleteRecursively()

        if (deleted) {
            downloadsRoot.mkdirs()
        }

        return deleted
    }

    private fun directorySize(
        file: File?
    ): Long {
        if (
            file == null ||
            !file.exists()
        ) {
            return 0L
        }

        if (file.isFile) {
            return file.length()
        }

        return file
            .listFiles()
            ?.sumOf(::directorySize)
            ?: 0L
    }

    private fun safeName(
        value: String
    ): String {
        return value
            .trim()
            .replace(
                SAFE_NAME_REGEX,
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

        const val DOWNLOADS_DIRECTORY_NAME =
            "mirai_downloads"

        const val COMPLETED_FILE_NAME =
            ".download_complete"

        const val DEFAULT_IMAGE_EXTENSION =
            "webp"

        const val PAGE_NUMBER_LENGTH =
            4

        const val MAXIMUM_NAME_LENGTH =
            120

        val SAFE_NAME_REGEX =
            Regex(
                pattern =
                    """[^a-zA-Z0-9._-]"""
            )
    }
}