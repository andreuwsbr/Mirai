package com.andrews.mirai.data.local

import android.content.Context
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.domain.model.MangaStatus
import com.andrews.mirai.domain.model.MangaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object FavoriteStore {

    private const val PREFERENCES_NAME = "mirai_favorites"
    private const val FAVORITES_KEY = "favorite_mangas"

    private var initialized = false

    private lateinit var applicationContext: Context

    private val _favorites =
        MutableStateFlow<List<Manga>>(emptyList())

    val favorites: StateFlow<List<Manga>> =
        _favorites.asStateFlow()

    fun initialize(context: Context) {
        if (initialized) {
            return
        }

        applicationContext =
            context.applicationContext

        initialized = true

        _favorites.value = loadFavorites()
    }

    fun isFavorite(mangaId: String): Boolean {
        return _favorites.value.any { manga ->
            manga.id == mangaId
        }
    }

    fun toggleFavorite(manga: Manga): Boolean {
        ensureInitialized()

        val currentFavorites =
            _favorites.value.toMutableList()

        val existingIndex =
            currentFavorites.indexOfFirst { favorite ->
                favorite.id == manga.id
            }

        val isNowFavorite: Boolean

        if (existingIndex >= 0) {
            currentFavorites.removeAt(existingIndex)
            isNowFavorite = false
        } else {
            currentFavorites.add(
                index = 0,
                element = manga
            )

            isNowFavorite = true
        }

        _favorites.value = currentFavorites

        saveFavorites(currentFavorites)

        return isNowFavorite
    }

    fun removeFavorite(mangaId: String) {
        ensureInitialized()

        val updatedFavorites =
            _favorites.value.filterNot { manga ->
                manga.id == mangaId
            }

        _favorites.value = updatedFavorites

        saveFavorites(updatedFavorites)
    }

    fun clearFavorites() {
        ensureInitialized()

        _favorites.value = emptyList()

        saveFavorites(emptyList())
    }

    private fun loadFavorites(): List<Manga> {
        val preferences =
            applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val json = preferences.getString(
            FAVORITES_KEY,
            null
        ) ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(json)

            buildList {
                for (index in 0 until jsonArray.length()) {
                    val mangaObject =
                        jsonArray.optJSONObject(index)
                            ?: continue

                    add(
                        jsonObjectToManga(
                            mangaObject
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveFavorites(
        favorites: List<Manga>
    ) {
        val preferences =
            applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        val jsonArray = JSONArray()

        favorites.forEach { manga ->
            jsonArray.put(
                mangaToJsonObject(manga)
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

    private fun mangaToJsonObject(
        manga: Manga
    ): JSONObject {
        return JSONObject().apply {
            put("id", manga.id)
            put("title", manga.title)
            put("description", manga.description)
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
            jsonObject.optJSONArray("genres")

        val genres = buildList {
            if (genresArray != null) {
                for (
                index in 0 until genresArray.length()
                ) {
                    val genre =
                        genresArray.optString(index)
                            .trim()

                    if (genre.isNotBlank()) {
                        add(genre)
                    }
                }
            }
        }

        return Manga(
            id = jsonObject
                .optString("id"),
            title = jsonObject
                .optString("title"),
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
                jsonObject.optString("status")
            ),
            type = parseType(
                jsonObject.optString("type")
            ),
            genres = genres
        )
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