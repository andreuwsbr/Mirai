package com.andrews.mirai.presentation.components

import com.andrews.mirai.data.local.download.DownloadedMangaEntity
import java.io.File

fun resolveMangaCoverModel(
    sourceId: String,
    mangaId: String,
    remoteCoverUrl: String?,
    downloadedMangas:
    List<DownloadedMangaEntity>
): Any? {
    val downloadedManga =
        downloadedMangas.firstOrNull { manga ->
            manga.sourceId == sourceId &&
                    manga.mangaId == mangaId
        }

    val localPath =
        downloadedManga
            ?.coverLocalPath
            ?.takeIf { path ->
                path.isNotBlank()
            }

    if (localPath != null) {
        val localFile =
            File(localPath)

        if (
            localFile.exists() &&
            localFile.isFile &&
            localFile.length() > 0L
        ) {
            return localFile
        }
    }

    return remoteCoverUrl
        ?.takeIf { url ->
            url.isNotBlank()
        }
}