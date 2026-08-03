package com.andrews.mirai.data.local.download

import androidx.room.Entity

@Entity(
    tableName = "downloaded_mangas",
    primaryKeys = [
        "sourceId",
        "mangaId"
    ]
)
data class DownloadedMangaEntity(
    val sourceId: String,
    val mangaId: String,
    val title: String,
    val description: String,
    val coverUrl: String?,
    val coverLocalPath: String?,
    val author: String,
    val status: String,
    val type: String,
    val genres: String,
    val downloadedAt: Long,
    val updatedAt: Long
)