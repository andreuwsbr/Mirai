package com.andrews.mirai.data.remote.supabase

data class CloudFavorite(
    val userId: String,
    val sourceId: String,
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String?
)

data class CloudReadingHistory(
    val userId: String,
    val sourceId: String,
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String?,
    val chapterId: String,
    val chapterName: String,
    val pageIndex: Int,
    val totalPages: Int,
    val readAt: Long
)

data class CloudChapterProgress(
    val userId: String,
    val sourceId: String,
    val mangaId: String,
    val chapterId: String,
    val chapterName: String,
    val pageIndex: Int,
    val totalPages: Int,
    val isRead: Boolean,
    val updatedAt: Long
)

sealed interface CloudSyncResult {

    data object Success :
        CloudSyncResult

    data class Failure(
        val message: String,
        val statusCode: Int? = null
    ) : CloudSyncResult
}