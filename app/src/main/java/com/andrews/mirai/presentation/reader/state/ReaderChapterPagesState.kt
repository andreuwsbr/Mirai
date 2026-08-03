package com.andrews.mirai.presentation.reader.state

import com.andrews.mirai.domain.model.ReaderPage

data class ReaderChapterPagesState(
    val pages: List<ReaderPage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)