package com.andrews.mirai.data.repository

import com.andrews.mirai.data.local.AuthSessionStore
import com.andrews.mirai.data.local.FavoriteEntry
import com.andrews.mirai.data.remote.supabase.CloudChapterProgress
import com.andrews.mirai.data.remote.supabase.CloudFavorite
import com.andrews.mirai.data.remote.supabase.CloudReadingHistory
import com.andrews.mirai.data.remote.supabase.CloudSyncApi
import com.andrews.mirai.data.remote.supabase.CloudSyncResult
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem
import android.util.Log

object CloudSyncRepository {

    fun isAvailable(): Boolean {
        return AuthSessionStore.isLoggedIn()
    }

    fun uploadFavorite(
        entry: FavoriteEntry
    ): CloudSyncResult {
        val session =
            AuthSessionStore.getSession()
                ?: return notAuthenticated()

        val result =
            CloudSyncApi.uploadFavorite(
                CloudFavorite(
                    userId = session.user.id,
                    sourceId = entry.sourceId,
                    mangaId = entry.manga.id,
                    mangaTitle = entry.manga.title,
                    mangaCoverUrl = entry.manga.coverUrl
                )
            )

        when (result) {
            CloudSyncResult.Success -> {
                Log.d(
                    "MiraiCloudSync",
                    "Favorito enviado com sucesso: ${entry.manga.title}"
                )
            }

            is CloudSyncResult.Failure -> {
                Log.e(
                    "MiraiCloudSync",
                    "Falha ao enviar favorito. " +
                            "Código: ${result.statusCode}. " +
                            "Erro: ${result.message}"
                )
            }
        }

        return result
    }

    fun deleteFavorite(
        sourceId: String,
        mangaId: String
    ): CloudSyncResult {
        if (!isAvailable()) {
            return notAuthenticated()
        }

        return CloudSyncApi.deleteFavorite(
            sourceId = sourceId,
            mangaId = mangaId
        )
    }

    fun downloadFavorites():
            Result<List<CloudFavorite>> {
        return CloudSyncApi.downloadFavorites()
    }

    fun uploadReadingHistory(
        item: ReadingHistoryItem
    ): CloudSyncResult {
        val session =
            AuthSessionStore.getSession()
                ?: return notAuthenticated()

        return CloudSyncApi.uploadReadingHistory(
            CloudReadingHistory(
                userId = session.user.id,
                sourceId = item.sourceId,
                mangaId = item.mangaId,
                mangaTitle = item.mangaTitle,
                mangaCoverUrl = item.mangaCoverUrl,
                chapterId = item.chapterId,
                chapterName = item.chapterName,
                pageIndex = item.pageIndex,
                totalPages = item.totalPages,
                readAt = item.readAt
            )
        )
    }

    fun downloadReadingHistory():
            Result<List<CloudReadingHistory>> {
        return CloudSyncApi
            .downloadReadingHistory()
    }

    fun uploadChapterProgress(
        sourceId: String,
        mangaId: String,
        chapterId: String,
        chapterName: String,
        pageIndex: Int,
        totalPages: Int,
        isRead: Boolean
    ): CloudSyncResult {
        val session =
            AuthSessionStore.getSession()
                ?: return notAuthenticated()

        return CloudSyncApi.uploadChapterProgress(
            CloudChapterProgress(
                userId = session.user.id,
                sourceId = sourceId,
                mangaId = mangaId,
                chapterId = chapterId,
                chapterName = chapterName,
                pageIndex = pageIndex.coerceAtLeast(0),
                totalPages = totalPages.coerceAtLeast(0),
                isRead = isRead,
                updatedAt =
                    System.currentTimeMillis()
            )
        )
    }

    fun downloadChapterProgress():
            Result<List<CloudChapterProgress>> {
        return CloudSyncApi
            .downloadChapterProgress()
    }

    fun uploadAllFavorites(
        entries: List<FavoriteEntry>
    ): CloudSyncResult {
        if (!isAvailable()) {
            return notAuthenticated()
        }

        entries.forEach { entry ->
            val result =
                uploadFavorite(entry)

            if (
                result is CloudSyncResult.Failure
            ) {
                return result
            }
        }

        return CloudSyncResult.Success
    }

    fun uploadAllHistory(
        history: List<ReadingHistoryItem>
    ): CloudSyncResult {
        if (!isAvailable()) {
            return notAuthenticated()
        }

        history.forEach { item ->
            val result =
                uploadReadingHistory(item)

            if (
                result is CloudSyncResult.Failure
            ) {
                return result
            }
        }

        return CloudSyncResult.Success
    }

    private fun notAuthenticated():
            CloudSyncResult {
        return CloudSyncResult.Failure(
            message = "Usuário não autenticado.",
            statusCode = 401
        )
    }
}