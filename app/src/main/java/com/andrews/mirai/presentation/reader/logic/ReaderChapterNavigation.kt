package com.andrews.mirai.presentation.reader.logic

import com.andrews.mirai.domain.model.Chapter

object ReaderChapterNavigation {

    /*
     * A ordem recebida deve ser preservada.
     *
     * A tela de detalhes já organiza os capítulos
     * de acordo com a escolha do usuário.
     *
     * Não ordenar novamente aqui, pois isso cria
     * divergência entre a lista exibida e a lista
     * usada pelo leitor.
     */
    fun orderedChapters(
        chapters: List<Chapter>
    ): List<Chapter> {
        return chapters
            .distinctBy { chapter ->
                chapter.id
            }
    }

    fun indexOf(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Int {
        return chapters.indexOfFirst { item ->
            item.id == chapter.id
        }
    }

    fun previousChapter(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Chapter? {
        val chapterIndex =
            indexOf(
                chapters = chapters,
                chapter = chapter
            )

        if (chapterIndex <= 0) {
            return null
        }

        return chapters.getOrNull(
            chapterIndex - 1
        )
    }

    fun nextChapter(
        chapters: List<Chapter>,
        chapter: Chapter
    ): Chapter? {
        val chapterIndex =
            indexOf(
                chapters = chapters,
                chapter = chapter
            )

        if (chapterIndex < 0) {
            return null
        }

        return chapters.getOrNull(
            chapterIndex + 1
        )
    }
}