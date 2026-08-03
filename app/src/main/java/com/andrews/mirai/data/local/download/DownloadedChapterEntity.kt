package com.andrews.mirai.data.local.download

import androidx.room.Entity

@Entity(
    tableName = "downloaded_chapters",
    primaryKeys = [
        "sourceId",
        "mangaId",
        "chapterId"
    ]
)
data class DownloadedChapterEntity(
    val sourceId: String,
    val mangaId: String,
    val chapterId: String,
    val chapterName: String,
    val chapterNumber: Double,
    val chapterUrl: String,
    val uploadedAt: String,
    val totalPages: Int,
    val downloadedPages: Int,
    val sizeBytes: Long,
    val localDirectoryPath: String,
    val status: DownloadStatus,
    val progressPercent: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val errorMessage: String?
)