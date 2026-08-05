package com.andrews.mirai.presentation.reader.settings

enum class ReaderPreloadMode(
    val label: String,
    val currentChapterPages: Int,
    val nextChapterPages: Int
) {
    LOW(
        label = "Baixo",
        currentChapterPages = 3,
        nextChapterPages = 1
    ),

    NORMAL(
        label = "Normal",
        currentChapterPages = 6,
        nextChapterPages = 2
    ),

    HIGH(
        label = "Alto",
        currentChapterPages = 10,
        nextChapterPages = 4
    )
}