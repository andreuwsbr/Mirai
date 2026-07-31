package com.andrews.mirai.domain.model

data class Manga(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String? = null,
    val author: String = "Não informado",
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val type: MangaType = MangaType.MANHWA,
    val genres: List<String> = emptyList()
)

enum class MangaStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    UNKNOWN
}

enum class MangaType {
    MANGA,
    MANHWA,
    MANHUA
}
