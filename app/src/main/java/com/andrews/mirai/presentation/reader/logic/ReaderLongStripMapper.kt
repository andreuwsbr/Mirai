package com.andrews.mirai.presentation.reader.logic

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.mode.LongStripChapterSection
import com.andrews.mirai.presentation.reader.state.ReaderChapterPagesState

internal data class ReaderLongStripState(
    val sections: List<LongStripChapterSection>,
    val showFinalCompletion: Boolean
)

internal object ReaderLongStripMapper {

    fun createState(
        orderedChapters: List<Chapter>,
        chapterStates:
        Map<String, ReaderChapterPagesState>,
        activeChapter: Chapter
    ): ReaderLongStripState {
        val knownChapters =
            if (orderedChapters.isEmpty()) {
                listOf(
                    activeChapter
                )
            } else {
                orderedChapters
            }

        val sections =
            knownChapters.mapNotNull { chapter ->
                chapterStates[
                    chapter.id
                ]?.let { state ->
                    LongStripChapterSection(
                        chapter = chapter,
                        pages = state.pages,
                        isLoading =
                            state.isLoading,
                        errorMessage =
                            state.errorMessage
                    )
                }
            }

        val lastLoadedChapter =
            sections
                .lastOrNull()
                ?.chapter

        val showFinalCompletion =
            lastLoadedChapter != null &&
                    (
                            orderedChapters.isEmpty() ||
                                    orderedChapters
                                        .lastOrNull()
                                        ?.id ==
                                    lastLoadedChapter.id
                            )

        return ReaderLongStripState(
            sections = sections,
            showFinalCompletion =
                showFinalCompletion
        )
    }
}