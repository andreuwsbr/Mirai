package com.andrews.mirai.domain.model

data class Chapter(
    val id: String,
    val mangaId: String,
    val name: String,
    val number: Double,
    val url: String = "",
    val uploadedAt: String = ""
)
