package com.andrews.mirai.data.remote.supabase

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.remote.HttpResponse
import com.andrews.mirai.data.repository.AuthSessionManager
import com.andrews.mirai.data.repository.SessionResult
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

object CloudSyncApi {

    private const val FAVORITES_TABLE =
        "mirai_favorites"

    private const val HISTORY_TABLE =
        "mirai_reading_history"

    private const val PROGRESS_TABLE =
        "mirai_chapter_progress"

    fun uploadFavorite(
        favorite: CloudFavorite
    ): CloudSyncResult {
        val json =
            JSONObject()
                .put(
                    "user_id",
                    favorite.userId
                )
                .put(
                    "source_id",
                    favorite.sourceId
                )
                .put(
                    "manga_id",
                    favorite.mangaId
                )
                .put(
                    "manga_title",
                    favorite.mangaTitle
                )
                .putNullable(
                    name = "manga_cover_url",
                    value = favorite.mangaCoverUrl
                )

        return upsert(
            table = FAVORITES_TABLE,
            conflictColumns =
                "user_id,source_id,manga_id",
            json = json
        )
    }

    fun deleteFavorite(
        sourceId: String,
        mangaId: String
    ): CloudSyncResult {
        return withValidSession { session ->
            val url =
                "${SupabaseConfig.restUrl}/$FAVORITES_TABLE" +
                        "?user_id=eq.${encode(session.user.id)}" +
                        "&source_id=eq.${encode(sourceId)}" +
                        "&manga_id=eq.${encode(mangaId)}"

            val response =
                HttpClient.delete(
                    url = url,
                    headers =
                        SupabaseHeaders
                            .authenticatedHeaders(
                                session.accessToken
                            )
                )

            response.toCloudSyncResult()
        }
    }

    fun downloadFavorites():
            Result<List<CloudFavorite>> {
        val session =
            getValidSessionForDownload()
                .getOrElse { throwable ->
                    return Result.failure(
                        throwable
                    )
                }

        val url =
            "${SupabaseConfig.restUrl}/$FAVORITES_TABLE" +
                    "?select=*" +
                    "&user_id=eq.${encode(session.user.id)}" +
                    "&order=created_at.asc"

        val response =
            HttpClient.get(
                url = url,
                headers =
                    SupabaseHeaders
                        .authenticatedHeaders(
                            session.accessToken
                        )
            )

        if (!response.isSuccessful) {
            return Result.failure(
                CloudSyncException(
                    response.extractErrorMessage()
                )
            )
        }

        return runCatching {
            val array =
                JSONArray(response.body)

            buildList {
                for (
                index in 0 until array.length()
                ) {
                    val json =
                        array.getJSONObject(index)

                    add(
                        CloudFavorite(
                            userId =
                                json.getString(
                                    "user_id"
                                ),
                            sourceId =
                                json.getString(
                                    "source_id"
                                ),
                            mangaId =
                                json.getString(
                                    "manga_id"
                                ),
                            mangaTitle =
                                json.getString(
                                    "manga_title"
                                ),
                            mangaCoverUrl =
                                json.optNullableString(
                                    "manga_cover_url"
                                )
                        )
                    )
                }
            }
        }
    }

    fun uploadReadingHistory(
        history: CloudReadingHistory
    ): CloudSyncResult {
        val json =
            JSONObject()
                .put(
                    "user_id",
                    history.userId
                )
                .put(
                    "source_id",
                    history.sourceId
                )
                .put(
                    "manga_id",
                    history.mangaId
                )
                .put(
                    "manga_title",
                    history.mangaTitle
                )
                .putNullable(
                    name = "manga_cover_url",
                    value = history.mangaCoverUrl
                )
                .put(
                    "chapter_id",
                    history.chapterId
                )
                .put(
                    "chapter_name",
                    history.chapterName
                )
                .put(
                    "page_index",
                    history.pageIndex
                )
                .put(
                    "total_pages",
                    history.totalPages
                )
                .put(
                    "read_at",
                    Instant
                        .ofEpochMilli(
                            history.readAt
                        )
                        .toString()
                )

        return upsert(
            table = HISTORY_TABLE,
            conflictColumns =
                "user_id,source_id,manga_id",
            json = json
        )
    }

    fun downloadReadingHistory():
            Result<List<CloudReadingHistory>> {
        val session =
            getValidSessionForDownload()
                .getOrElse { throwable ->
                    return Result.failure(
                        throwable
                    )
                }

        val url =
            "${SupabaseConfig.restUrl}/$HISTORY_TABLE" +
                    "?select=*" +
                    "&user_id=eq.${encode(session.user.id)}" +
                    "&order=read_at.desc"

        val response =
            HttpClient.get(
                url = url,
                headers =
                    SupabaseHeaders
                        .authenticatedHeaders(
                            session.accessToken
                        )
            )

        if (!response.isSuccessful) {
            return Result.failure(
                CloudSyncException(
                    response.extractErrorMessage()
                )
            )
        }

        return runCatching {
            val array =
                JSONArray(response.body)

            buildList {
                for (
                index in 0 until array.length()
                ) {
                    val json =
                        array.getJSONObject(index)

                    add(
                        CloudReadingHistory(
                            userId =
                                json.getString(
                                    "user_id"
                                ),
                            sourceId =
                                json.getString(
                                    "source_id"
                                ),
                            mangaId =
                                json.getString(
                                    "manga_id"
                                ),
                            mangaTitle =
                                json.getString(
                                    "manga_title"
                                ),
                            mangaCoverUrl =
                                json.optNullableString(
                                    "manga_cover_url"
                                ),
                            chapterId =
                                json.getString(
                                    "chapter_id"
                                ),
                            chapterName =
                                json.getString(
                                    "chapter_name"
                                ),
                            pageIndex =
                                json.optInt(
                                    "page_index",
                                    0
                                ),
                            totalPages =
                                json.optInt(
                                    "total_pages",
                                    0
                                ),
                            readAt =
                                parseInstant(
                                    json.optString(
                                        "read_at"
                                    )
                                )
                        )
                    )
                }
            }
        }
    }

    fun uploadChapterProgress(
        progress: CloudChapterProgress
    ): CloudSyncResult {
        val json =
            JSONObject()
                .put(
                    "user_id",
                    progress.userId
                )
                .put(
                    "source_id",
                    progress.sourceId
                )
                .put(
                    "manga_id",
                    progress.mangaId
                )
                .put(
                    "chapter_id",
                    progress.chapterId
                )
                .put(
                    "chapter_name",
                    progress.chapterName
                )
                .put(
                    "page_index",
                    progress.pageIndex
                )
                .put(
                    "total_pages",
                    progress.totalPages
                )
                .put(
                    "is_read",
                    progress.isRead
                )
                .put(
                    "updated_at",
                    Instant
                        .ofEpochMilli(
                            progress.updatedAt
                        )
                        .toString()
                )

        return upsert(
            table = PROGRESS_TABLE,
            conflictColumns =
                "user_id,source_id,manga_id,chapter_id",
            json = json
        )
    }

    fun downloadChapterProgress():
            Result<List<CloudChapterProgress>> {
        val session =
            getValidSessionForDownload()
                .getOrElse { throwable ->
                    return Result.failure(
                        throwable
                    )
                }

        val url =
            "${SupabaseConfig.restUrl}/$PROGRESS_TABLE" +
                    "?select=*" +
                    "&user_id=eq.${encode(session.user.id)}" +
                    "&order=updated_at.desc"

        val response =
            HttpClient.get(
                url = url,
                headers =
                    SupabaseHeaders
                        .authenticatedHeaders(
                            session.accessToken
                        )
            )

        if (!response.isSuccessful) {
            return Result.failure(
                CloudSyncException(
                    response.extractErrorMessage()
                )
            )
        }

        return runCatching {
            val array =
                JSONArray(response.body)

            buildList {
                for (
                index in 0 until array.length()
                ) {
                    val json =
                        array.getJSONObject(index)

                    add(
                        CloudChapterProgress(
                            userId =
                                json.getString(
                                    "user_id"
                                ),
                            sourceId =
                                json.getString(
                                    "source_id"
                                ),
                            mangaId =
                                json.getString(
                                    "manga_id"
                                ),
                            chapterId =
                                json.getString(
                                    "chapter_id"
                                ),
                            chapterName =
                                json.getString(
                                    "chapter_name"
                                ),
                            pageIndex =
                                json.optInt(
                                    "page_index",
                                    0
                                ),
                            totalPages =
                                json.optInt(
                                    "total_pages",
                                    0
                                ),
                            isRead =
                                json.optBoolean(
                                    "is_read",
                                    false
                                ),
                            updatedAt =
                                parseInstant(
                                    json.optString(
                                        "updated_at"
                                    )
                                )
                        )
                    )
                }
            }
        }
    }

    private fun upsert(
        table: String,
        conflictColumns: String,
        json: JSONObject
    ): CloudSyncResult {
        return withValidSession { session ->
            val url =
                "${SupabaseConfig.restUrl}/$table" +
                        "?on_conflict=" +
                        encode(conflictColumns)

            val response =
                HttpClient.postJson(
                    url = url,
                    jsonBody = json.toString(),
                    headers =
                        SupabaseHeaders
                            .authenticatedHeaders(
                                session.accessToken
                            ) + mapOf(
                            "Prefer" to
                                    "resolution=merge-duplicates," +
                                    "return=minimal"
                        )
                )

            response.toCloudSyncResult()
        }
    }

    private fun withValidSession(
        operation:
            (com.andrews.mirai.data.remote.supabase.AuthSession) ->
        CloudSyncResult
    ): CloudSyncResult {
        return when (
            val result =
                AuthSessionManager
                    .getValidSession()
        ) {
            is SessionResult.Success -> {
                operation(
                    result.session
                )
            }

            is SessionResult.Failure -> {
                CloudSyncResult.Failure(
                    message = result.message
                )
            }

            SessionResult.NotAuthenticated -> {
                CloudSyncResult.Failure(
                    message =
                        "Usuário não autenticado.",
                    statusCode = 401
                )
            }
        }
    }

    private fun getValidSessionForDownload():
            Result<AuthSession> {
        return when (
            val result =
                AuthSessionManager
                    .getValidSession()
        ) {
            is SessionResult.Success -> {
                Result.success(
                    result.session
                )
            }

            is SessionResult.Failure -> {
                Result.failure(
                    CloudSyncException(
                        result.message
                    )
                )
            }

            SessionResult.NotAuthenticated -> {
                Result.failure(
                    CloudSyncException(
                        "Usuário não autenticado."
                    )
                )
            }
        }
    }

    private fun HttpResponse.toCloudSyncResult():
            CloudSyncResult {
        return if (isSuccessful) {
            CloudSyncResult.Success
        } else {
            CloudSyncResult.Failure(
                message =
                    extractErrorMessage(),
                statusCode =
                    code.takeIf {
                        it > 0
                    }
            )
        }
    }

    private fun HttpResponse.extractErrorMessage():
            String {
        if (code == 0) {
            return errorMessage
                ?: "Não foi possível conectar ao servidor."
        }

        return runCatching {
            val json =
                JSONObject(body)

            json.optString("message")
                .ifBlank {
                    json.optString(
                        "error_description"
                    )
                }
                .ifBlank {
                    json.optString(
                        "details"
                    )
                }
                .ifBlank {
                    json.optString(
                        "hint"
                    )
                }
                .ifBlank {
                    json.optString(
                        "code"
                    )
                }
        }.getOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Não foi possível sincronizar os dados."
    }

    private fun JSONObject.putNullable(
        name: String,
        value: String?
    ): JSONObject {
        return put(
            name,
            value ?: JSONObject.NULL
        )
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {
        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }

        return optString(name)
            .takeIf {
                it.isNotBlank()
            }
    }

    private fun parseInstant(
        value: String
    ): Long {
        return runCatching {
            Instant
                .parse(value)
                .toEpochMilli()
        }.getOrDefault(0L)
    }

    private fun encode(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
                .toString()
        )
    }
}

class CloudSyncException(
    message: String
) : Exception(message)