package com.andrews.mirai.presentation.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andrews.mirai.data.download.ChapterDownloadManager
import com.andrews.mirai.data.download.DownloadRepository
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.data.local.download.DownloadedMangaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        DownloadRepository(application)

    private val downloadManager =
        ChapterDownloadManager(application)

    private val dialogState =
        MutableStateFlow(
            DownloadsDialogState()
        )

    private val downloadedMangas =
        repository
            .observeDownloadedMangas()
            .flatMapLatest { mangas ->
                createMangaModelsFlow(
                    mangas
                )
            }

    val uiState: StateFlow<DownloadsUiState> =
        combine(
            downloadedMangas,
            dialogState
        ) { mangas, dialogs ->
            DownloadsUiState(
                mangas = mangas,
                totalSizeBytes =
                    mangas.sumOf { manga ->
                        manga.sizeBytes
                    },
                totalCompletedChapters =
                    mangas.sumOf { manga ->
                        manga.completedChaptersCount
                    },
                isLoading = false,
                errorMessage =
                    dialogs.errorMessage,
                mangaPendingDeletion =
                    dialogs.mangaPendingDeletion,
                chapterPendingDeletion =
                    dialogs.chapterPendingDeletion,
                showDeleteAllConfirmation =
                    dialogs.showDeleteAllConfirmation
            )
        }.stateIn(
            scope = viewModelScope,
            started =
                SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 5_000L
                ),
            initialValue =
                DownloadsUiState()
        )

    fun requestDeleteChapter(
        chapter: DownloadedChapterUiModel
    ) {
        dialogState.value =
            dialogState.value.copy(
                chapterPendingDeletion =
                    chapter,
                mangaPendingDeletion = null,
                showDeleteAllConfirmation =
                    false,
                errorMessage = null
            )
    }

    fun requestDeleteManga(
        manga: DownloadedMangaUiModel
    ) {
        dialogState.value =
            dialogState.value.copy(
                mangaPendingDeletion =
                    manga,
                chapterPendingDeletion =
                    null,
                showDeleteAllConfirmation =
                    false,
                errorMessage = null
            )
    }

    fun requestDeleteAll() {
        dialogState.value =
            dialogState.value.copy(
                showDeleteAllConfirmation =
                    true,
                mangaPendingDeletion = null,
                chapterPendingDeletion =
                    null,
                errorMessage = null
            )
    }

    fun dismissDialog() {
        dialogState.value =
            dialogState.value.copy(
                mangaPendingDeletion = null,
                chapterPendingDeletion =
                    null,
                showDeleteAllConfirmation =
                    false
            )
    }

    fun clearError() {
        dialogState.value =
            dialogState.value.copy(
                errorMessage = null
            )
    }

    fun confirmDeleteChapter() {
        val chapter =
            dialogState
                .value
                .chapterPendingDeletion
                ?: return

        dismissDialog()

        viewModelScope.launch {
            downloadManager.cancelWork(
                sourceId =
                    chapter.sourceId,
                mangaId =
                    chapter.mangaId,
                chapterId =
                    chapter.chapterId
            )

            val deleted =
                repository.deleteChapter(
                    sourceId =
                        chapter.sourceId,
                    mangaId =
                        chapter.mangaId,
                    chapterId =
                        chapter.chapterId
                )

            if (!deleted) {
                showError(
                    "Não foi possível excluir o capítulo."
                )
            }
        }
    }

    fun confirmDeleteManga() {
        val manga =
            dialogState
                .value
                .mangaPendingDeletion
                ?: return

        dismissDialog()

        viewModelScope.launch {
            manga.chapters.forEach { chapter ->
                downloadManager.cancelWork(
                    sourceId =
                        chapter.sourceId,
                    mangaId =
                        chapter.mangaId,
                    chapterId =
                        chapter.chapterId
                )
            }

            val deleted =
                repository.deleteManga(
                    sourceId =
                        manga.sourceId,
                    mangaId =
                        manga.mangaId
                )

            if (!deleted) {
                showError(
                    "Não foi possível excluir a obra."
                )
            }
        }
    }

    fun confirmDeleteAll() {
        dismissDialog()

        viewModelScope.launch {
            downloadManager
                .cancelAllDownloads()

            val deleted =
                repository
                    .deleteAllDownloads()

            if (!deleted) {
                showError(
                    "Não foi possível excluir todos os downloads."
                )
            }
        }
    }

    private fun createMangaModelsFlow(
        mangas: List<DownloadedMangaEntity>
    ) = if (mangas.isEmpty()) {
        flowOf(
            emptyList()
        )
    } else {
        combine(
            mangas.map { manga ->
                repository
                    .observeDownloadedChapters(
                        sourceId =
                            manga.sourceId,
                        mangaId =
                            manga.mangaId
                    )
                    .map { chapters ->
                        val chapterModels =
                            chapters
                                .map { chapter ->
                                    DownloadedChapterUiModel(
                                        sourceId =
                                            chapter.sourceId,
                                        mangaId =
                                            chapter.mangaId,
                                        chapterId =
                                            chapter.chapterId,
                                        name =
                                            chapter.chapterName,
                                        number =
                                            chapter.chapterNumber,
                                        sizeBytes =
                                            chapter.sizeBytes,
                                        totalPages =
                                            chapter.totalPages,
                                        status =
                                            chapter.status,
                                        progressPercent =
                                            chapter.progressPercent
                                    )
                                }
                                .sortedByDescending {
                                        chapter ->
                                    chapter.number
                                }

                        DownloadedMangaUiModel(
                            sourceId =
                                manga.sourceId,
                            mangaId =
                                manga.mangaId,
                            title =
                                manga.title,
                            coverUrl =
                                manga.coverUrl,
                            coverLocalPath =
                                manga.coverLocalPath,
                            chapters =
                                chapterModels,
                            sizeBytes =
                                chapterModels.sumOf {
                                        chapter ->
                                    chapter.sizeBytes
                                },
                            completedChaptersCount =
                                chapterModels.count {
                                        chapter ->
                                    chapter.status ==
                                            DownloadStatus.COMPLETED
                                }
                        )
                    }
            }
        ) { mangaModels ->
            mangaModels
                .toList()
                .sortedBy { manga ->
                    manga.title.lowercase()
                }
        }
    }

    private fun showError(
        message: String
    ) {
        dialogState.value =
            dialogState.value.copy(
                errorMessage = message
            )
    }
}

private data class DownloadsDialogState(
    val mangaPendingDeletion:
    DownloadedMangaUiModel? = null,

    val chapterPendingDeletion:
    DownloadedChapterUiModel? = null,

    val showDeleteAllConfirmation:
    Boolean = false,

    val errorMessage: String? = null
)