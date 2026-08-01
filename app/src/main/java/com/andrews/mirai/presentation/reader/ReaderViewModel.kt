package com.andrews.mirai.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.reader.settings.ReaderPreferences
import com.andrews.mirai.presentation.reader.state.ReaderUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReaderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReaderUiState()
    )

    val uiState: StateFlow<ReaderUiState> =
        _uiState.asStateFlow()

    private var loadedChapterId: String? = null

    fun loadChapter(chapter: Chapter) {
        if (
            loadedChapterId == chapter.id &&
            _uiState.value.pages.isNotEmpty()
        ) {
            return
        }

        loadedChapterId = chapter.id

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    pages = emptyList(),
                    currentPage = 0,
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val pages = withContext(Dispatchers.IO) {
                    SourceRepository.currentSource.getPages(
                        chapter = chapter
                    )
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        pages = pages,
                        currentPage = 0,
                        isLoading = false,
                        errorMessage = if (pages.isEmpty()) {
                            "Nenhuma página foi encontrada."
                        } else {
                            null
                        }
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { currentState ->
                    currentState.copy(
                        pages = emptyList(),
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: "Não foi possível carregar o capítulo."
                    )
                }
            }
        }
    }

    fun setCurrentPage(pageIndex: Int) {
        val lastPageIndex =
            _uiState.value.pages.lastIndex

        val safePageIndex = if (lastPageIndex >= 0) {
            pageIndex.coerceIn(
                minimumValue = 0,
                maximumValue = lastPageIndex
            )
        } else {
            0
        }

        _uiState.update { currentState ->
            currentState.copy(
                currentPage = safePageIndex
            )
        }
    }

    fun toggleControls() {
        _uiState.update { currentState ->
            currentState.copy(
                controlsVisible =
                    !currentState.controlsVisible
            )
        }
    }

    fun showControls() {
        _uiState.update { currentState ->
            currentState.copy(
                controlsVisible = true
            )
        }
    }

    fun hideControls() {
        _uiState.update { currentState ->
            currentState.copy(
                controlsVisible = false
            )
        }
    }

    fun updatePreferences(
        preferences: ReaderPreferences
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                preferences = preferences
            )
        }
    }

    fun retry(chapter: Chapter) {
        loadedChapterId = null
        loadChapter(chapter)
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }
}