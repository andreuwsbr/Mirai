package com.andrews.mirai.data.local.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun saveManga(
        manga: DownloadedMangaEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun saveChapter(
        chapter: DownloadedChapterEntity
    )

    @Update
    suspend fun updateChapter(
        chapter: DownloadedChapterEntity
    )

    @Query(
        """
        SELECT *
        FROM downloaded_mangas
        ORDER BY updatedAt DESC
        """
    )
    fun observeMangas():
            Flow<List<DownloadedMangaEntity>>

    @Query(
        """
        SELECT *
        FROM downloaded_mangas
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        LIMIT 1
        """
    )
    suspend fun getManga(
        sourceId: String,
        mangaId: String
    ): DownloadedMangaEntity?

    @Query(
        """
        SELECT *
        FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        ORDER BY chapterNumber DESC
        """
    )
    fun observeChapters(
        sourceId: String,
        mangaId: String
    ): Flow<List<DownloadedChapterEntity>>

    @Query(
        """
        SELECT *
        FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND chapterId = :chapterId
        LIMIT 1
        """
    )
    suspend fun getChapter(
        sourceId: String,
        mangaId: String,
        chapterId: String
    ): DownloadedChapterEntity?

    @Query(
        """
        SELECT *
        FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND status = 'COMPLETED'
        ORDER BY chapterNumber ASC
        """
    )
    suspend fun getCompletedChapters(
        sourceId: String,
        mangaId: String
    ): List<DownloadedChapterEntity>

    @Query(
        """
        UPDATE downloaded_chapters
        SET downloadedPages = :downloadedPages,
            totalPages = :totalPages,
            progressPercent = :progressPercent,
            sizeBytes = :sizeBytes,
            updatedAt = :updatedAt
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND chapterId = :chapterId
        """
    )
    suspend fun updateProgress(
        sourceId: String,
        mangaId: String,
        chapterId: String,
        downloadedPages: Int,
        totalPages: Int,
        progressPercent: Int,
        sizeBytes: Long,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE downloaded_chapters
        SET status = :status,
            errorMessage = :errorMessage,
            updatedAt = :updatedAt
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND chapterId = :chapterId
        """
    )
    suspend fun updateStatus(
        sourceId: String,
        mangaId: String,
        chapterId: String,
        status: DownloadStatus,
        errorMessage: String?,
        updatedAt: Long
    )

    @Query(
        """
        SELECT COALESCE(
            SUM(sizeBytes),
            0
        )
        FROM downloaded_chapters
        WHERE status = 'COMPLETED'
        """
    )
    fun observeTotalSizeBytes(): Flow<Long>

    @Query(
        """
        SELECT COUNT(*)
        FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND status = 'COMPLETED'
        """
    )
    suspend fun countCompletedChapters(
        sourceId: String,
        mangaId: String
    ): Int

    @Query(
        """
        DELETE FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        AND chapterId = :chapterId
        """
    )
    suspend fun deleteChapter(
        sourceId: String,
        mangaId: String,
        chapterId: String
    )

    @Query(
        """
        DELETE FROM downloaded_chapters
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        """
    )
    suspend fun deleteChaptersFromManga(
        sourceId: String,
        mangaId: String
    )

    @Query(
        """
        DELETE FROM downloaded_mangas
        WHERE sourceId = :sourceId
        AND mangaId = :mangaId
        """
    )
    suspend fun deleteManga(
        sourceId: String,
        mangaId: String
    )

    @Query(
        "DELETE FROM downloaded_chapters"
    )
    suspend fun deleteAllChapters()

    @Query(
        "DELETE FROM downloaded_mangas"
    )
    suspend fun deleteAllMangas()
}