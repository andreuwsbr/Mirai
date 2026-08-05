package com.andrews.mirai.presentation.reader.settings

enum class ReaderTapZoneSize(
    val label: String,
    val edgeFraction: Float
) {
    SMALL(
        label = "Pequena",
        edgeFraction = 0.22f
    ),

    MEDIUM(
        label = "Média",
        edgeFraction = 0.30f
    ),

    LARGE(
        label = "Grande",
        edgeFraction = 0.38f
    )
}