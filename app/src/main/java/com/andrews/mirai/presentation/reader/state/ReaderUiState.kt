package com.andrews.mirai.presentation.reader.state

import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.settings.ReaderPreferences

data class ReaderUiState(

    val pages: List<ReaderPage> = emptyList(),

    val currentPage: Int = 0,

    val isLoading: Boolean = false,

    val errorMessage: String? = null,

    val controlsVisible: Boolean = true,

    val preferences: ReaderPreferences = ReaderPreferences()

)