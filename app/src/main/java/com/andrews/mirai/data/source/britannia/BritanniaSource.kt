package com.andrews.mirai.data.source.britannia

import android.util.Log
import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.source.MangaSource
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import com.andrews.mirai.domain.model.ReaderPage
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BritanniaSource : MangaSource {

    companion object {
        private const val LOG_TAG = "BRITANNIA"
    }

    override val id: String = "britannia"

    override val name: String = "Império da Britannia"

    override val baseUrl: String =
        "https://imperiodabritannia.net"

    private val apiUrl =
        "https://api.imperiodabritannia.net"

    private val cdnUrl =
        "https://cdn.imperiodabritannia.net"

    private val http = HttpClient

    override suspend fun getPopular(
        page: Int
    ): List<Manga> {
        val safePage = page.coerceAtLeast(1)

        val url = buildString {
            append(apiUrl)
            append("/api/obras")
            append("?pagina=")
            append(safePage)
            append("&limite=24")
            append("&formato_ids=17%2C20%2C24%2C25%2C26")
        }

        Log.d(LOG_TAG, "GET POPULAR: $url")

        val response = http.getJson(url)

        logResponse(
            operation = "POPULAR",
            responseCode = response.code,
            responseBody = response.body,
            errorMessage = response.errorMessage
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return parseMangaList(response.body)
    }

    override suspend fun search(
        query: String,
        page: Int
    ): List<Manga> {
        val normalizedQuery = query.trim()

        if (normalizedQuery.isBlank()) {
            return getPopular(page)
        }

        val encodedQuery = URLEncoder.encode(
            normalizedQuery,
            StandardCharsets.UTF_8.toString()
        )

        val safePage = page.coerceAtLeast(1)

        val url = buildString {
            append(apiUrl)
            append("/api/obras")
            append("?pagina=")
            append(safePage)
            append("&limite=24")
            append("&busca=")
            append(encodedQuery)
            append("&formato_ids=17%2C20%2C24%2C25%2C26")
        }

        Log.d(LOG_TAG, "GET SEARCH: $url")

        val response = http.getJson(url)

        logResponse(
            operation = "SEARCH",
            responseCode = response.code,
            responseBody = response.body,
            errorMessage = response.errorMessage
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return parseMangaList(response.body)
    }

    override suspend fun getDetails(
        manga: Manga
    ): Manga {
        val slug = extractSlug(manga.id)

        if (slug.isBlank()) {
            Log.e(
                LOG_TAG,
                "Não foi possível extrair o slug de: ${manga.id}"
            )

            return manga
        }

        val url =
            "$apiUrl/api/obras/by-slug/$slug"

        Log.d(LOG_TAG, "GET DETAILS: $url")

        val response = http.getJson(url)

        logResponse(
            operation = "DETAILS",
            responseCode = response.code,
            responseBody = response.body,
            errorMessage = response.errorMessage
        )

        if (!response.isSuccessful) {
            return manga
        }

        return runCatching {
            val root = JSONObject(response.body)

            if (!root.optBoolean("sucesso", false)) {
                return@runCatching manga
            }

            val obra = root.optJSONObject("obra")
                ?: return@runCatching manga

            parseMangaObject(obra)
        }.onFailure { throwable ->
            Log.e(
                LOG_TAG,
                "Erro ao processar os detalhes",
                throwable
            )
        }.getOrDefault(manga)
    }

    override suspend fun getChapters(
        manga: Manga
    ): List<Chapter> {
        val slug = extractSlug(manga.id)

        if (slug.isBlank()) {
            Log.e(
                LOG_TAG,
                "Slug vazio ao buscar capítulos: ${manga.id}"
            )

            return emptyList()
        }

        val url =
            "$apiUrl/api/obras/by-slug/$slug"

        Log.d(LOG_TAG, "GET CHAPTERS: $url")

        val response = http.getJson(url)

        logResponse(
            operation = "CHAPTERS",
            responseCode = response.code,
            responseBody = response.body,
            errorMessage = response.errorMessage
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return runCatching {
            val root = JSONObject(response.body)

            if (!root.optBoolean("sucesso", false)) {
                return@runCatching emptyList()
            }

            val obra = root.optJSONObject("obra")
                ?: return@runCatching emptyList()

            val obraId = obra
                .optLong("id", 0L)
                .toString()

            val chaptersArray = obra.optJSONArray("capitulos")
                ?: return@runCatching emptyList()

            parseChapters(
                chaptersArray = chaptersArray,
                obraId = obraId
            )
        }.onFailure { throwable ->
            Log.e(
                LOG_TAG,
                "Erro ao processar os capítulos",
                throwable
            )
        }.getOrDefault(emptyList())
    }

    override suspend fun getPages(
        chapter: Chapter
    ): List<ReaderPage> {
        val chapterUrl = chapter.url.ifBlank {
            buildChapterApiUrl(chapter)
        }

        if (chapterUrl.isBlank()) {
            Log.e(
                LOG_TAG,
                "URL do capítulo vazia: ${chapter.name}"
            )

            return emptyList()
        }

        Log.d(LOG_TAG, "GET PAGES: $chapterUrl")

        val response = http.getJson(chapterUrl)

        logResponse(
            operation = "PAGES",
            responseCode = response.code,
            responseBody = response.body,
            errorMessage = response.errorMessage
        )

        if (!response.isSuccessful) {
            return emptyList()
        }

        return runCatching {
            val root = JSONObject(response.body)

            if (!root.optBoolean("sucesso", false)) {
                return@runCatching emptyList()
            }

            val chapterObject = root.optJSONObject("capitulo")
                ?: return@runCatching emptyList()

            val paywallBlocked = chapterObject.optBoolean(
                "paywall_bloqueado",
                false
            )

            if (paywallBlocked) {
                Log.e(
                    LOG_TAG,
                    "Capítulo bloqueado por paywall: ${chapter.name}"
                )

                return@runCatching emptyList()
            }

            val pagesArray = chapterObject.optJSONArray("paginas")
                ?: return@runCatching emptyList()

            parsePages(pagesArray)
        }.onFailure { throwable ->
            Log.e(
                LOG_TAG,
                "Erro ao processar as páginas",
                throwable
            )
        }.getOrDefault(emptyList())
    }

    private fun parseMangaList(
        json: String
    ): List<Manga> {
        return runCatching {
            val root = JSONObject(json)

            if (!root.optBoolean("sucesso", false)) {
                Log.e(
                    LOG_TAG,
                    "A API retornou sucesso=false"
                )

                return@runCatching emptyList()
            }

            val obrasArray = root.optJSONArray("obras")
                ?: return@runCatching emptyList()

            val mangas = buildList {
                for (index in 0 until obrasArray.length()) {
                    val obra = obrasArray.optJSONObject(index)
                        ?: continue

                    val manga = parseMangaObject(obra)

                    if (manga.id.isNotBlank()) {
                        add(manga)
                    }
                }
            }

            Log.d(
                LOG_TAG,
                "Quantidade de obras processadas: ${mangas.size}"
            )

            mangas
        }.onFailure { throwable ->
            Log.e(
                LOG_TAG,
                "Erro ao processar a lista de obras. JSON: $json",
                throwable
            )
        }.getOrDefault(emptyList())
    }

    private fun parseMangaObject(
        obra: JSONObject
    ): Manga {
        val slug = obra
            .optString("slug")
            .trim()

        val obraId = obra.optLong("id", 0L)

        val mangaId = when {
            slug.isNotBlank() && slug != "null" -> slug
            obraId > 0L -> obraId.toString()
            else -> ""
        }

        val title = obra
            .optString("nome")
            .trim()
            .takeUnless {
                it.isBlank() || it == "null"
            }
            ?: "Título não informado"

        val description = obra
            .optString("descricao")
            .trim()
            .takeUnless {
                it.isBlank() || it == "null"
            }
            .orEmpty()

        val imagePath = obra
            .optString("imagem")
            .trim()
            .takeUnless {
                it.isBlank() || it == "null"
            }

        val coverUrl = imagePath?.let {
            resolveCdnUrl(it)
        }

        val genres = parseTags(
            obra.optJSONArray("tags")
        )

        val type = parseMangaType(
            obra.optString("formato_nome")
        )

        val status = parseMangaStatus(
            obra.optString("status_nome")
        )

        return Manga(
            id = mangaId,
            title = title,
            description = description,
            coverUrl = coverUrl,
            author = "Não informado",
            status = status,
            type = type,
            genres = genres
        )
    }

    private fun parseChapters(
        chaptersArray: JSONArray,
        obraId: String
    ): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        for (index in 0 until chaptersArray.length()) {
            val item = chaptersArray.optJSONObject(index)
                ?: continue

            val deletedAt = item
                .optString("deleted_at")
                .trim()

            if (
                deletedAt.isNotBlank() &&
                deletedAt != "null"
            ) {
                continue
            }

            val numberText = item
                .optString("numero")
                .replace(',', '.')
                .trim()

            val number = numberText
                .toDoubleOrNull()
                ?: continue

            val name = item
                .optString("nome")
                .trim()
                .takeUnless {
                    it.isBlank() || it == "null"
                }
                ?: formatChapterName(number)

            val chapterId = item
                .optLong("id", 0L)
                .toString()

            val uploadedAt = item
                .optString("criado_em")
                .trim()
                .takeUnless {
                    it.isBlank() || it == "null"
                }
                .orEmpty()

            val chapterUrl =
                "$apiUrl/api/obras/$obraId/capitulos/$numberText"

            chapters += Chapter(
                id = chapterId,
                mangaId = obraId,
                name = name,
                number = number,
                url = chapterUrl,
                uploadedAt = uploadedAt
            )
        }

        Log.d(
            LOG_TAG,
            "Quantidade de capítulos processados: ${chapters.size}"
        )

        return chapters.sortedByDescending {
            it.number
        }
    }

    private fun parsePages(
        pagesArray: JSONArray
    ): List<ReaderPage> {
        val pages = mutableListOf<ReaderPage>()

        for (position in 0 until pagesArray.length()) {
            val pageObject = pagesArray.optJSONObject(position)
                ?: continue

            val rawImageUrl = pageObject
                .optString("cdn_id")
                .trim()

            if (
                rawImageUrl.isBlank() ||
                rawImageUrl == "null"
            ) {
                continue
            }

            val pageNumber = pageObject
                .optInt(
                    "numero",
                    position + 1
                )

            pages += ReaderPage(
                index = (pageNumber - 1).coerceAtLeast(0),
                imageUrl = resolveCdnUrl(rawImageUrl)
            )
        }

        val result = pages
            .distinctBy { page ->
                page.imageUrl
            }
            .sortedBy { page ->
                page.index
            }

        Log.d(
            LOG_TAG,
            "Quantidade de páginas processadas: ${result.size}"
        )

        return result
    }

    private fun parseTags(
        tagsArray: JSONArray?
    ): List<String> {
        if (tagsArray == null) {
            return emptyList()
        }

        val tags = linkedSetOf<String>()

        for (index in 0 until tagsArray.length()) {
            val tagObject = tagsArray.optJSONObject(index)
                ?: continue

            val tagName = tagObject
                .optString("nome")
                .trim()

            if (
                tagName.isNotBlank() &&
                tagName != "null"
            ) {
                tags += tagName
            }
        }

        return tags.toList()
    }

    private fun parseMangaType(
        formatName: String
    ): MangaType {
        return when {
            formatName.contains(
                "manhua",
                ignoreCase = true
            ) -> MangaType.MANHUA

            formatName.contains(
                "manhwa",
                ignoreCase = true
            ) -> MangaType.MANHWA

            formatName.contains(
                "webtoon",
                ignoreCase = true
            ) -> MangaType.MANHWA

            formatName.contains(
                "manga",
                ignoreCase = true
            ) -> MangaType.MANGA

            formatName.contains(
                "mangá",
                ignoreCase = true
            ) -> MangaType.MANGA

            else -> MangaType.MANHWA
        }
    }

    private fun parseMangaStatus(
        statusName: String
    ): MangaStatus {
        return when {
            statusName.contains(
                "concluído",
                ignoreCase = true
            ) -> MangaStatus.COMPLETED

            statusName.contains(
                "concluido",
                ignoreCase = true
            ) -> MangaStatus.COMPLETED

            statusName.contains(
                "ativo",
                ignoreCase = true
            ) -> MangaStatus.ONGOING

            statusName.contains(
                "andamento",
                ignoreCase = true
            ) -> MangaStatus.ONGOING

            statusName.contains(
                "hiato",
                ignoreCase = true
            ) -> MangaStatus.HIATUS

            else -> MangaStatus.UNKNOWN
        }
    }

    private fun extractSlug(
        mangaId: String
    ): String {
        return mangaId
            .substringAfterLast("/manga/")
            .substringBefore("/")
            .substringBefore("?")
            .trim()
    }

    private fun buildChapterApiUrl(
        chapter: Chapter
    ): String {
        if (chapter.mangaId.isBlank()) {
            return ""
        }

        val chapterNumber = formatChapterNumber(
            chapter.number
        )

        return "$apiUrl/api/obras/" +
                "${chapter.mangaId}/capitulos/" +
                chapterNumber
    }

    private fun resolveCdnUrl(
        path: String
    ): String {
        val normalizedPath = path.trim()

        return when {
            normalizedPath.startsWith(
                "https://",
                ignoreCase = true
            ) -> normalizedPath

            normalizedPath.startsWith(
                "http://",
                ignoreCase = true
            ) -> normalizedPath

            else -> {
                "$cdnUrl/${normalizedPath.trimStart('/')}"
            }
        }
    }

    private fun formatChapterName(
        number: Double
    ): String {
        return "Capítulo ${formatChapterNumber(number)}"
    }

    private fun formatChapterNumber(
        number: Double
    ): String {
        return if (number % 1.0 == 0.0) {
            number.toInt().toString()
        } else {
            number.toString()
        }
    }

    private fun logResponse(
        operation: String,
        responseCode: Int,
        responseBody: String,
        errorMessage: String?
    ) {
        Log.d(
            LOG_TAG,
            """
            OPERAÇÃO: $operation
            CÓDIGO: $responseCode
            ERRO: ${errorMessage.orEmpty()}
            RESPOSTA: $responseBody
            """.trimIndent()
        )
    }
}