package com.andrews.mirai.presentation.downloads

import com.andrews.mirai.data.local.download.DownloadStatus

data class DownloadedChapterUiModel(
    val sourceId: String,
    val mangaId: String,
    val chapterId: String,
    val name: String,
    val number: Double,
    val sizeBytes: Long,
    val totalPages: Int,
    val status: DownloadStatus,
    val progressPercent: Int
)

data class DownloadedMangaUiModel(
    val sourceId: String,
    val mangaId: String,
    val title: String,
    val coverUrl: String?,
    val coverLocalPath: String?,
    val chapters: List<DownloadedChapterUiModel>,
    val sizeBytes: Long,
    val completedChaptersCount: Int
)

data class DownloadsUiState(
    val mangas: List<DownloadedMangaUiModel> =
        emptyList(),

    val totalSizeBytes: Long = 0L,

    val totalCompletedChapters: Int = 0,

    val isLoading: Boolean = true,

    val errorMessage: String? = null,

    val mangaPendingDeletion:
    DownloadedMangaUiModel? = null,

    val chapterPendingDeletion:
    DownloadedChapterUiModel? = null,

    val showDeleteAllConfirmation: Boolean =
        false
)