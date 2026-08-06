package com.andrews.mirai.presentation.reader.logic

import android.util.Log
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.state.ReaderScreenState
import kotlinx.coroutines.CancellationException

class ReaderChapterController(
    private val screenState: ReaderScreenState,
    private val pageLoader: ReaderChapterPageLoader,
    private val progressStore: ReadingProgressStore
) {

    suspend fun loadChapterPages(
        chapter: Chapter
    ) {
        if (
            screenState.isLoadingOrLoaded(
                chapter.id
            )
        ) {
            return
        }

        loadChapterPagesInternal(
            chapter
        )
    }

    suspend fun retryChapter(
        chapter: Chapter
    ) {
        screenState.removeChapterState(
            chapter.id
        )

        loadChapterPagesInternal(
            chapter
        )
    }

    private suspend fun loadChapterPagesInternal(
        chapter: Chapter
    ) {
        screenState.markChapterLoading(
            chapter.id
        )

        try {
            val savedPageIndex =
                screenState.getSavedPage(
                    chapterId =
                        chapter.id,
                    fallbackPage =
                        progressStore.getPage(
                            chapter.id
                        )
                )

            val loadResult =
                pageLoader.load(
                    chapter =
                        chapter,
                    savedPageIndex =
                        savedPageIndex
                )

            screenState.setChapterLoaded(
                chapterId =
                    chapter.id,
                chapterState =
                    loadResult.state,
                safePageIndex =
                    loadResult.safePageIndex
            )

            Log.d(
                IMAGE_LOG_TAG,
                "${chapter.name}: " +
                        "${loadResult.state.pages.size} páginas"
            )
        } catch (
            exception: CancellationException
        ) {
            screenState.removeChapterState(
                chapter.id
            )

            throw exception
        } catch (
            throwable: Throwable
        ) {
            screenState.setChapterError(
                chapterId =
                    chapter.id,
                message =
                    readableErrorMessage(
                        throwable
                    )
            )

            Log.e(
                IMAGE_LOG_TAG,
                "Erro ao carregar ${chapter.name}",
                throwable
            )
        }
    }

    suspend fun openChapterFromControls(
        targetChapter: Chapter,
        targetPage: Int,
        onChapterSelected: (Chapter) -> Unit
    ) {
        val chapterChangeStarted =
            screenState.beginChapterChange()

        if (!chapterChangeStarted) {
            return
        }

        try {
            screenState.updatePage(
                chapterId =
                    targetChapter.id,
                pageIndex =
                    targetPage
            )

            screenState.activeChapter =
                targetChapter

            screenState.requestPage(
                chapterId =
                    targetChapter.id,
                pageIndex =
                    targetPage
            )

            onChapterSelected(
                targetChapter
            )

            loadChapterPages(
                targetChapter
            )

            val chapterState =
                screenState.getChapterState(
                    targetChapter.id
                )

            if (
                chapterState?.errorMessage != null
            ) {
                return
            }

            val pages =
                chapterState
                    ?.pages
                    .orEmpty()

            val safeTargetPage =
                targetPage.coerceIn(
                    minimumValue = 0,
                    maximumValue =
                        pages
                            .lastIndex
                            .coerceAtLeast(
                                0
                            )
                )

            screenState.updatePage(
                chapterId =
                    targetChapter.id,
                pageIndex =
                    safeTargetPage
            )

            screenState.requestPage(
                chapterId =
                    targetChapter.id,
                pageIndex =
                    safeTargetPage
            )
        } finally {
            screenState.finishChapterChange()
        }
    }

    private fun readableErrorMessage(
        throwable: Throwable
    ): String {
        val originalMessage =
            throwable.message
                ?.trim()
                .orEmpty()

        if (originalMessage.isNotBlank()) {
            return originalMessage
        }

        return "Verifique sua conexão e tente novamente."
    }

    private companion object {

        const val IMAGE_LOG_TAG =
            "MIRAI_IMAGE"
    }
}