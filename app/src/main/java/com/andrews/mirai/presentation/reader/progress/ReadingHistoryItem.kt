package com.andrews.mirai.presentation.reader.progress

data class ReadingHistoryItem(
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