package com.andrews.mirai.presentation.reader.settings

import com.andrews.mirai.presentation.reader.display.ReaderOrientationMode

data class ReaderPreferences(
    val mode: ReaderMode =
        ReaderMode.LONG_STRIP,

    val background: ReaderBackground =
        ReaderBackground.BLACK,

    val showPageNumber: Boolean =
        true,

    val fullscreen: Boolean =
        true,

    val keepScreenOn: Boolean =
        false,

    val longStripGapDp: Int =
        12,

    val tapMode: ReaderTapMode =
        ReaderTapMode.TAP_AND_SWIPE,

    val tapZoneSize: ReaderTapZoneSize =
        ReaderTapZoneSize.MEDIUM,

    val preloadMode: ReaderPreloadMode =
        ReaderPreloadMode.NORMAL,

    val orientationMode: ReaderOrientationMode =
        ReaderOrientationMode.AUTOMATIC,

    /*
     * 0 significa usar o brilho normal do sistema.
     *
     * Valores entre 1 e 100 representam o brilho
     * aplicado somente durante a leitura.
     */
    val brightnessPercent: Int =
        0
)