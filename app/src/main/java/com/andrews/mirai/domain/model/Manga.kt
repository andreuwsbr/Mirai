package com.andrews.mirai.domain.model

data class Manga(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String? = null,
    val author: String = "Não informado",
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val type: MangaType = MangaType.UNKNOWN,
    val genres: List<String> = emptyList()
)

enum class MangaStatus(
    val displayName: String
) {
    ONGOING("Em andamento"),
    COMPLETED("Completo"),
    HIATUS("Em hiato"),
    CANCELLED("Cancelado"),
    UNKNOWN("Não informado")
}

enum class MangaType(
    val displayName: String
) {
    MANGA("Mangá"),
    MANHWA("Manhwa"),
    MANHUA("Manhua"),
    UNKNOWN("Não informado")
}