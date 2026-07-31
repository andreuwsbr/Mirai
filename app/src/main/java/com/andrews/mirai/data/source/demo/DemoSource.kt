package com.andrews.mirai.data.source.demo

import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import com.andrews.mirai.domain.model.ReaderPage

class DemoSource : MangaSource {
    override val id: String = "demo"
    override val name: String = "Fonte de demonstração"
    override val baseUrl: String = "local://demo"

    private val works = listOf(
        Manga(
            id = "torre-celestial",
            title = "Torre Celestial",
            description = "Um jovem desperta em uma torre misteriosa e precisa descobrir por que foi escolhido.",
            author = "Equipe Mirai",
            status = MangaStatus.ONGOING,
            type = MangaType.MANHWA,
            genres = listOf("Ação", "Fantasia")
        ),
        Manga(
            id = "lua-vermelha",
            title = "Lua Vermelha",
            description = "Uma aventura sobrenatural em uma cidade onde a noite nunca termina.",
            author = "Equipe Mirai",
            status = MangaStatus.COMPLETED,
            type = MangaType.MANGA,
            genres = listOf("Mistério", "Drama")
        ),
        Manga(
            id = "imperador-renascido",
            title = "O Imperador Renascido",
            description = "Após voltar no tempo, um antigo imperador tenta mudar seu destino.",
            author = "Equipe Mirai",
            status = MangaStatus.ONGOING,
            type = MangaType.MANHUA,
            genres = listOf("Aventura", "Cultivo")
        )
    )

    override suspend fun getPopular(page: Int): List<Manga> = works

    override suspend fun search(query: String, page: Int): List<Manga> {
        if (query.isBlank()) return works
        return works.filter { it.title.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(manga: Manga): Manga = manga

    override suspend fun getChapters(manga: Manga): List<Chapter> =
        (1..12).map { number ->
            Chapter(
                id = "${manga.id}-$number",
                mangaId = manga.id,
                name = "Capítulo $number",
                number = number.toDouble(),
                uploadedAt = "Demonstração"
            )
        }

    override suspend fun getPages(chapter: Chapter): List<ReaderPage> =
        (1..8).map { index ->
            ReaderPage(index = index, imageUrl = "")
        }
}
