package com.andrews.mirai.presentation.reader.settings

enum class ReaderTapMode(
    val label: String,
    val description: String
) {
    TAP_AND_SWIPE(
        label = "Toque e deslize",
        description =
            "Troque páginas tocando nas laterais ou deslizando."
    ),

    SWIPE_ONLY(
        label = "Somente deslize",
        description =
            "Troque páginas somente deslizando."
    )
}