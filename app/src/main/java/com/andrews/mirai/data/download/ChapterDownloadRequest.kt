package com.andrews.mirai.data.download

data class ChapterDownloadRequest(
    val sourceId: String,
    val mangaId: String,
    val mangaTitle: String,
    val mangaDescription: String,
    val mangaCoverUrl: String?,
    val mangaAuthor: String,
    val mangaStatus: String,
    val mangaType: String,
    val mangaGenres: String,
    val chapterId: String,
    val chapterName: String,
    val chapterNumber: Double,
    val chapterUrl: String,
    val chapterUploadedAt: String
)