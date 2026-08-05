package com.andrews.mirai.presentation.reader.session

import com.andrews.mirai.domain.model.Chapter

object ReaderChapterOrderResolver {

    /**
     * Cria a ordem cronológica usada durante a leitura.
     *
     * A ordem original recebida das fontes pode variar:
     *
     * 20, 19, 18, 17
     *
     * ou:
     *
     * 17, 18, 19, 20
     *
     * O leitor sempre precisa trabalhar com:
     *
     * 17, 18, 19, 20
     */
    fun resolve(
        chapters: List<Chapter>,
        activeChapter: Chapter? = null
    ): List<Chapter> {
        val chaptersWithActive =
            buildList {
                addAll(chapters)

                if (
                    activeChapter != null &&
                    chapters.none { chapter ->
                        chapter.id ==
                                activeChapter.id
                    }
                ) {
                    add(activeChapter)
                }
            }

        return chaptersWithActive
            .distinctBy { chapter ->
                chapter.id
            }
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<Chapter>> { indexedChapter ->
                    chapterOrderGroup(
                        indexedChapter.value
                    )
                }.thenBy { indexedChapter ->
                    safeChapterNumber(
                        indexedChapter.value
                    )
                }.thenBy { indexedChapter ->
                    indexedChapter.index
                }
            )
            .map { indexedChapter ->
                indexedChapter.value
            }
    }

    private fun chapterOrderGroup(
        chapter: Chapter
    ): Int {
        return if (
            chapter.number.isFinite()
        ) {
            NUMBERED_CHAPTER_GROUP
        } else {
            UNKNOWN_CHAPTER_GROUP
        }
    }

    private fun safeChapterNumber(
        chapter: Chapter
    ): Double {
        return if (
            chapter.number.isFinite()
        ) {
            chapter.number
        } else {
            Double.MAX_VALUE
        }
    }

    private const val NUMBERED_CHAPTER_GROUP =
        0

    private const val UNKNOWN_CHAPTER_GROUP =
        1
}