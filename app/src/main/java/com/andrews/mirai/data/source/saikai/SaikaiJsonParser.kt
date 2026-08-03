package com.andrews.mirai.data.source.saikai

import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import com.andrews.mirai.domain.model.ReaderPage
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class SaikaiJsonParser {

    fun parseMangas(
        json: String
    ): List<Manga> {
        val root =
            parseRoot(json)
                ?: return emptyList()

        val data =
            root.optJSONArray("data")
                ?: return emptyList()

        return buildList {
            for (index in 0 until data.length()) {
                val story =
                    data.optJSONObject(index)
                        ?: continue

                parseManga(story)
                    ?.let { manga ->
                        add(manga)
                    }
            }
        }
    }

    fun parseDetails(
        json: String,
        fallbackManga: Manga
    ): Manga {
        val root =
            parseRoot(json)
                ?: return fallbackManga

        val data =
            root.optJSONArray("data")
                ?: return fallbackManga

        val story =
            data.optJSONObject(0)
                ?: return fallbackManga

        return parseManga(story)
            ?: fallbackManga
    }

    fun parseChapters(
        json: String,
        mangaId: String
    ): List<Chapter> {
        val root =
            parseRoot(json)
                ?: return emptyList()

        val data =
            root.optJSONArray("data")
                ?: return emptyList()

        val story =
            data.optJSONObject(0)
                ?: return emptyList()

        val storySlug =
            story
                .optString("slug")
                .ifBlank {
                    SaikaiUrls.normalizeSlug(
                        mangaId
                    )
                }

        val releases =
            story.optJSONArray("releases")
                ?: return emptyList()

        val chapters =
            buildList {
                for (
                index in 0 until releases.length()
                ) {
                    val release =
                        releases.optJSONObject(index)
                            ?: continue

                    val isActive =
                        release.optInt(
                            "is_active",
                            1
                        )

                    if (isActive != 1) {
                        continue
                    }

                    val releaseId =
                        release
                            .optLong(
                                "id",
                                -1L
                            )

                    if (releaseId <= 0L) {
                        continue
                    }

                    val chapterText =
                        release
                            .optString("chapter")
                            .trim()

                    val chapterNumber =
                        chapterText
                            .replace(
                                oldValue = ",",
                                newValue = "."
                            )
                            .toDoubleOrNull()
                            ?: -1.0

                    val releaseSlug =
                        release
                            .optString("slug")
                            .trim()

                    val optionalTitle =
                        release
                            .optString("title")
                            .trim()
                            .takeIf { title ->
                                title.isNotBlank() &&
                                        title.lowercase() !=
                                        "null"
                            }

                    val chapterName =
                        buildString {
                            append("Capítulo ")

                            append(
                                chapterText.ifBlank {
                                    chapterNumber
                                        .takeIf { number ->
                                            number >= 0.0
                                        }
                                        ?.toString()
                                        ?: releaseId.toString()
                                }
                            )

                            if (optionalTitle != null) {
                                append(" - ")
                                append(optionalTitle)
                            }
                        }

                    val chapterWebUrl =
                        buildString {
                            append(
                                SaikaiUrls.BASE_URL
                            )

                            append("/ler/comics/")
                            append(storySlug)
                            append("/")
                            append(releaseId)
                            append("/")
                            append(
                                releaseSlug.ifBlank {
                                    "capitulo-$chapterText"
                                }
                            )
                        }

                    add(
                        Chapter(
                            id =
                                releaseId.toString(),
                            mangaId =
                                SaikaiUrls.normalizeSlug(
                                    mangaId
                                ),
                            name =
                                chapterName,
                            number =
                                chapterNumber,
                            url =
                                chapterWebUrl,
                            uploadedAt =
                                formatPublishedDate(
                                    release.optString(
                                        "published_at"
                                    )
                                )
                        )
                    )
                }
            }

        return chapters
            .distinctBy { chapter ->
                chapter.id
            }
            .sortedByDescending { chapter ->
                chapter.number
            }
    }

    fun parsePages(
        json: String
    ): List<ReaderPage> {
        val root =
            parseRoot(json)
                ?: return emptyList()

        val release =
            root.optJSONObject("data")
                ?: return emptyList()

        val releaseImages =
            release.optJSONArray(
                "release_images"
            )
                ?: return emptyList()

        /*
         * Regra obrigatória:
         * preservamos exatamente a ordem
         * recebida pela API da Saikai.
         *
         * Não ordenamos por URL, nome,
         * número ou qualquer outro campo.
         */
        return buildList {
            for (
            position in
            0 until releaseImages.length()
            ) {
                val imageObject =
                    releaseImages
                        .optJSONObject(position)
                        ?: continue

                val imagePath =
                    imageObject
                        .optString("image")
                        .trim()

                val imageUrl =
                    SaikaiUrls.image(
                        imagePath
                    )

                if (imageUrl.isBlank()) {
                    continue
                }

                add(
                    ReaderPage(
                        index = position,
                        imageUrl = imageUrl
                    )
                )
            }
        }
    }

    private fun parseManga(
        story: JSONObject
    ): Manga? {
        val slug =
            story
                .optString("slug")
                .trim()

        val title =
            story
                .optString("title")
                .trim()

        if (
            slug.isBlank() ||
            title.isBlank()
        ) {
            return null
        }

        val description =
            parseSynopsis(
                story.optString(
                    "synopsis"
                )
            )

        val authors =
            parseNames(
                story.optJSONArray(
                    "authors"
                )
            )

        val artists =
            parseNames(
                story.optJSONArray(
                    "artists"
                )
            )

        val author =
            (authors + artists)
                .distinct()
                .joinToString(
                    separator = ", "
                )
                .ifBlank {
                    "Não informado"
                }

        val genres =
            parseNames(
                story.optJSONArray(
                    "genres"
                )
            )

        val imagePath =
            story
                .optString("image")
                .trim()

        val statusName =
            story
                .optJSONObject("status")
                ?.optString("name")
                .orEmpty()

        val typeName =
            story
                .optJSONObject("type")
                ?.optString("name")
                .orEmpty()

        return Manga(
            id = slug,
            title = title,
            description =
                description.ifBlank {
                    "Sinopse não informada."
                },
            coverUrl =
                SaikaiUrls
                    .image(imagePath)
                    .takeIf { url ->
                        url.isNotBlank()
                    },
            author = author,
            status =
                mapStatus(
                    statusName
                ),
            type =
                mapType(
                    typeName
                ),
            genres = genres
        )
    }

    private fun parseNames(
        array: JSONArray?
    ): List<String> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (
            index in 0 until array.length()
            ) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                val name =
                    item
                        .optString("name")
                        .trim()

                if (name.isNotBlank()) {
                    add(name)
                }
            }
        }
    }

    private fun parseSynopsis(
        synopsisHtml: String
    ): String {
        val normalizedHtml =
            synopsisHtml.trim()

        if (normalizedHtml.isBlank()) {
            return ""
        }

        val document =
            Jsoup.parseBodyFragment(
                normalizedHtml
            )

        val paragraphs =
            document
                .select("p")
                .map { paragraph ->
                    paragraph.text().trim()
                }
                .filter { text ->
                    text.isNotBlank()
                }

        if (paragraphs.isNotEmpty()) {
            return paragraphs.joinToString(
                separator = "\n\n"
            )
        }

        return document
            .text()
            .trim()
    }

    private fun mapStatus(
        statusName: String
    ): MangaStatus {
        return when (
            normalizeText(statusName)
        ) {
            "concluido",
            "completo",
            "finalizado" ->
                MangaStatus.COMPLETED

            "em andamento",
            "andamento",
            "ongoing" ->
                MangaStatus.ONGOING

            "hiato",
            "pausado" ->
                MangaStatus.HIATUS

            "cancelado",
            "dropado" ->
                MangaStatus.CANCELLED

            else ->
                MangaStatus.UNKNOWN
        }
    }

    private fun mapType(
        typeName: String
    ): MangaType {
        return when (
            normalizeText(typeName)
        ) {
            "manga" ->
                MangaType.MANGA

            "manhwa",
            "webtoon",
            "webtoon coreano" ->
                MangaType.MANHWA

            "manhua",
            "webtoon chines" ->
                MangaType.MANHUA

            else ->
                MangaType.UNKNOWN
        }
    }

    private fun normalizeText(
        value: String
    ): String {
        return java.text.Normalizer
            .normalize(
                value.trim().lowercase(),
                java.text.Normalizer.Form.NFD
            )
            .replace(
                Regex("\\p{Mn}+"),
                ""
            )
    }

    private fun formatPublishedDate(
        publishedAt: String
    ): String {
        val normalizedValue =
            publishedAt.trim()

        if (normalizedValue.isBlank()) {
            return ""
        }

        return runCatching {
            val instant =
                Instant.parse(
                    normalizedValue
                )

            DATE_FORMATTER.format(
                instant.atZone(
                    ZoneId.systemDefault()
                )
            )
        }.getOrElse {
            normalizedValue
                .substringBefore("T")
                .split("-")
                .takeIf { parts ->
                    parts.size == 3
                }
                ?.let { parts ->
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                }
                ?: normalizedValue
        }
    }

    private fun parseRoot(
        json: String
    ): JSONObject? {
        val normalizedJson =
            json.trim()

        if (normalizedJson.isBlank()) {
            return null
        }

        return runCatching {
            JSONObject(
                normalizedJson
            )
        }.getOrNull()
    }

    private companion object {

        val DATE_FORMATTER:
                DateTimeFormatter =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy",
                Locale(
                    "pt",
                    "BR"
                )
            )
    }
}