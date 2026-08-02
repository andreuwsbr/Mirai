package com.andrews.mirai.presentation.reader.settings

data class ReaderPreferences(
    val mode: ReaderMode = ReaderMode.LONG_STRIP,
    val background: ReaderBackground = ReaderBackground.BLACK,
    val showPageNumber: Boolean = true,
    val fullscreen: Boolean = true,
    val keepScreenOn: Boolean = false,
    val longStripGapDp: Int = 12
)
