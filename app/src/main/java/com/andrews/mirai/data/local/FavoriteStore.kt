package com.andrews.mirai.data.local

import android.content.Context
import com.andrews.mirai.data.repository.SourceRepository
import com.andrews.mirai.data.source.SourceRegistry
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class FavoriteEntry(
    val sourceId: String,
    val manga: Manga
)

object FavoriteStore {

    private const val PREFERENCES_NAME =
        "mirai_favorites"

    private const val FAVORITES_KEY =
        "favorite_mangas"

    private var initialized = false

    private lateinit var applicationContext: Context

    private val _favoriteEntries =
        MutableStateFlow<List<FavoriteEntry>>(
            emptyList()
        )

    val favoriteEntries:
            StateFlow<List<FavoriteEntry>> =
        _favoriteEntries.asStateFlow()

    /*
     * Mantido temporariamente para que as telas antigas
     * continuem funcionando durante a reorganização.
     */
    private val _favorites =
        MutableStateFlow<List<Manga>>(
            emptyList()
        )

    val favorites: StateFlow<List<Manga>> =
        _favorites.asStateFlow()

    fun initialize(
        context: Context
    ) {
        if (initialized) {
            return
        }

        applicationContext =
            context.applicationContext

        initialized = true

        publish(
            loadFavoriteEntries()
        )
    }

    fun isFavorite(
        mangaId: String
    ): Boolean {
        return _favoriteEntries.value.any { entry ->
            entry.manga.id == mangaId
        }
    }

    fun isFavorite(
        mangaId: String,
        sourceId: String
    ): Boolean {
        return _favoriteEntries.value.any { entry ->
            entry.manga.id == mangaId &&
                    entry.sourceId == sourceId
        }
    }

    fun toggleFavorite(
        manga: Manga
    ): Boolean {
        return toggleFavorite(
            manga = manga,
            sourceId =
                SourceRepository.currentSource.id
        )
    }

    fun toggleFavorite(
        manga: Manga,
        sourceId: String
    ): Boolean {
        ensureInitialized()

        val resolvedSourceId =
            resolveSourceId(
                savedSourceId = sourceId,
                mangaId = manga.id
            )

        val currentEntries =
            _favoriteEntries
                .value
                .toMutableList()

        val existingIndex =
            currentEntries.indexOfFirst { entry ->
                entry.manga.id == manga.id &&
                        entry.sourceId ==
                        resolvedSourceId
            }

        val isNowFavorite: Boolean

        if (existingIndex >= 0) {
            currentEntries.removeAt(
                existingIndex
            )

            isNowFavorite = false
        } else {
            currentEntries.add(
                index = 0,
                element = FavoriteEntry(
                    sourceId =
                        resolvedSourceId,
                    manga = manga
                )
            )

            isNowFavorite = true
        }

        publish(currentEntries)
        saveFavoriteEntries(currentEntries)

        return isNowFavorite
    }

    fun removeFavorite(
        mangaId: String
    ) {
        ensureInitialized()

        val updatedEntries =
            _favoriteEntries.value.filterNot { entry ->
                entry.manga.id == mangaId
            }

        publish(updatedEntries)
        saveFavoriteEntries(updatedEntries)
    }

    fun removeFavorite(
        mangaId: String,
        sourceId: String
    ) {
        ensureInitialized()

        val updatedEntries =
            _favoriteEntries.value.filterNot { entry ->
                entry.manga.id == mangaId &&
                        entry.sourceId == sourceId
            }

        publish(updatedEntries)
        saveFavoriteEntries(updatedEntries)
    }

    fun clearFavorites() {
        ensureInitialized()

        publish(emptyList())
        saveFavoriteEntries(emptyList())
    }

    private fun publish(
        entries: List<FavoriteEntry>
    ) {
        val distinctEntries =
            entries.distinctBy { entry ->
                favoriteKey(
                    sourceId = entry.sourceId,
                    mangaId = entry.manga.id
                )
            }

        _favoriteEntries.value =
            distinctEntries

        _favorites.value =
            distinctEntries.map { entry ->
                entry.manga
            }
    }

    private fun loadFavoriteEntries():
            List<FavoriteEntry> {
        val preferences =
            applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val json =
            preferences.getString(
                FAVORITES_KEY,
                null
            ) ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(json)

            buildList {
                for (
                index in 0 until
                        jsonArray.length()
                ) {
                    val mangaObject =
                        jsonArray.optJSONObject(index)
                            ?: continue

                    val manga =
                        jsonObjectToManga(
                            mangaObject
                        )

                    if (
                        manga.id.isBlank() ||
                        manga.title.isBlank()
                    ) {
                        continue
                    }

                    val sourceId =
                        resolveSourceId(
                            savedSourceId =
                                mangaObject.optString(
                                    "sourceId"
                                ),
                            mangaId = manga.id
                        )

                    add(
                        FavoriteEntry(
                            sourceId = sourceId,
                            manga = manga
                        )
                    )
                }
            }.distinctBy { entry ->
                favoriteKey(
                    sourceId = entry.sourceId,
                    mangaId = entry.manga.id
                )
            }
        }.getOrDefault(
            emptyList()
        )
    }

    private fun saveFavoriteEntries(
        entries: List<FavoriteEntry>
    ) {
        val preferences =
            applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val jsonArray = JSONArray()

        entries.forEach { entry ->
            jsonArray.put(
                favoriteEntryToJsonObject(
                    entry
                )
            )
        }

        preferences
            .edit()
            .putString(
                FAVORITES_KEY,
                jsonArray.toString()
            )
            .apply()
    }

    private fun favoriteEntryToJsonObject(
        entry: FavoriteEntry
    ): JSONObject {
        val manga = entry.manga

        return JSONObject().apply {
            put(
                "sourceId",
                entry.sourceId
            )

            put("id", manga.id)
            put("title", manga.title)
            put(
                "description",
                manga.description
            )
            put("coverUrl", manga.coverUrl)
            put("author", manga.author)
            put("status", manga.status.name)
            put("type", manga.type.name)

            put(
                "genres",
                JSONArray(manga.genres)
            )
        }
    }

    private fun jsonObjectToManga(
        jsonObject: JSONObject
    ): Manga {
        val genresArray =
            jsonObject.optJSONArray(
                "genres"
            )

        val genres = buildList {
            if (genresArray != null) {
                for (
                index in 0 until
                        genresArray.length()
                ) {
                    val genre =
                        genresArray
                            .optString(index)
                            .trim()

                    if (genre.isNotBlank()) {
                        add(genre)
                    }
                }
            }
        }

        return Manga(
            id = jsonObject
                .optString("id")
                .trim(),
            title = jsonObject
                .optString("title")
                .trim(),
            description = jsonObject
                .optString("description"),
            coverUrl = jsonObject
                .optString("coverUrl")
                .takeUnless { value ->
                    value.isBlank() ||
                            value == "null"
                },
            author = jsonObject
                .optString(
                    "author",
                    "Não informado"
                ),
            status = parseStatus(
                jsonObject.optString(
                    "status"
                )
            ),
            type = parseType(
                jsonObject.optString(
                    "type"
                )
            ),
            genres = genres
        )
    }

    private fun resolveSourceId(
        savedSourceId: String,
        mangaId: String
    ): String {
        val normalizedSourceId =
            savedSourceId.trim()

        if (
            normalizedSourceId.isNotBlank() &&
            SourceRegistry.findById(
                normalizedSourceId
            ) != null
        ) {
            return normalizedSourceId
        }

        return detectSourceId(mangaId)
    }

    private fun detectSourceId(
        mangaId: String
    ): String {
        val normalizedMangaId =
            mangaId.lowercase()

        return when {
            normalizedMangaId.contains(
                "mangalivre.blog"
            ) -> {
                "mangalivre"
            }

            normalizedMangaId.contains(
                "astratoons.com"
            ) -> {
                "astraltoons"
            }

            else -> {
                SourceRegistry.default().id
            }
        }
    }

    private fun favoriteKey(
        sourceId: String,
        mangaId: String
    ): String {
        return "$sourceId|$mangaId"
    }

    private fun parseStatus(
        value: String
    ): MangaStatus {
        return runCatching {
            MangaStatus.valueOf(value)
        }.getOrDefault(
            MangaStatus.UNKNOWN
        )
    }

    private fun parseType(
        value: String
    ): MangaType {
        return runCatching {
            MangaType.valueOf(value)
        }.getOrDefault(
            MangaType.MANHWA
        )
    }

    private fun ensureInitialized() {
        check(initialized) {
            "FavoriteStore não foi inicializado."
        }
    }
}