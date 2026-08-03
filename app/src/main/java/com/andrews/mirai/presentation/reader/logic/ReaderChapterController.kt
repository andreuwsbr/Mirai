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

        screenState.markChapterLoading(
            chapter.id
        )

        try {
            val savedPageIndex =
                screenState.getSavedPage(
                    chapterId = chapter.id,
                    fallbackPage =
                        progressStore.getPage(
                            chapter.id
                        )
                )

            val loadResult =
                pageLoader.load(
                    chapter = chapter,
                    savedPageIndex =
                        savedPageIndex
                )

            screenState.setChapterLoaded(
                chapterId = chapter.id,
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
        } catch (throwable: Throwable) {
            screenState.setChapterError(
                chapterId = chapter.id,
                message =
                    throwable.message
                        ?: "Não foi possível carregar as páginas."
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
        isLongStripMode: Boolean,
        onChapterSelected: (Chapter) -> Unit
    ) {
        loadChapterPages(
            targetChapter
        )

        val pages =
            screenState
                .getChapterState(
                    targetChapter.id
                )
                ?.pages
                .orEmpty()

        val safeTargetPage =
            targetPage.coerceIn(
                minimumValue = 0,
                maximumValue =
                    pages
                        .lastIndex
                        .coerceAtLeast(0)
            )

        screenState.updatePage(
            chapterId = targetChapter.id,
            pageIndex = safeTargetPage
        )

        screenState.requestPage(
            chapterId = targetChapter.id,
            pageIndex = safeTargetPage
        )

        if (!isLongStripMode) {
            screenState.activeChapter =
                targetChapter

            onChapterSelected(
                targetChapter
            )
        }
    }

    private companion object {

        const val IMAGE_LOG_TAG =
            "MIRAI_IMAGE"
    }
}