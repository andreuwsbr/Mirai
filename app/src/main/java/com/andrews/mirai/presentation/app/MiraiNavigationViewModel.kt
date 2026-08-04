package com.andrews.mirai.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga

class MiraiNavigationViewModel :
    ViewModel() {

    var selectedManga by mutableStateOf<Manga?>(
        null
    )
        private set

    var selectedChapter by mutableStateOf<Chapter?>(
        null
    )
        private set

    var selectedChapters by mutableStateOf<
            List<Chapter>
            >(
        emptyList()
    )
        private set

    fun selectManga(
        manga: Manga
    ) {
        selectedManga =
            manga
    }

    fun selectChapter(
        chapter: Chapter,
        chapters: List<Chapter>
    ) {
        selectedChapter =
            chapter

        selectedChapters =
            chapters
    }

    fun updateSelectedChapter(
        chapter: Chapter
    ) {
        selectedChapter =
            chapter
    }

    fun updateSelectedChapters(
        chapters: List<Chapter>
    ) {
        selectedChapters =
            chapters
    }

    fun clearSelectedManga() {
        selectedManga =
            null
    }

    fun clearSelectedChapter() {
        selectedChapter =
            null

        selectedChapters =
            emptyList()
    }
}