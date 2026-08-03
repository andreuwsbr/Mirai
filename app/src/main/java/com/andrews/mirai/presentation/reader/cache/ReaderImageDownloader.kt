package com.andrews.mirai.presentation.reader.cache

import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

data class DownloadedReaderImage(
    val file: File,
    val aspectRatio: Float
)

class ReaderImageDownloader(
    private val imageCache: ReaderImageCache,
    private val httpClient: OkHttpClient = OkHttpClient()
) {

    suspend fun download(
        imageUrl: String
    ): File = withContext(Dispatchers.IO) {
        val localFile =
            getLocalFileOrNull(
                imageUrl
            )

        if (localFile != null) {
            return@withContext localFile
        }

        val downloadLock =
            downloadLocks.getOrPut(
                imageUrl
            ) {
                Mutex()
            }

        downloadLock.withLock {
            downloadImageIfNecessary(
                imageUrl
            )
        }
    }

    suspend fun downloadWithInfo(
        imageUrl: String
    ): DownloadedReaderImage {
        val file =
            download(
                imageUrl
            )

        val aspectRatio =
            aspectRatioCache.getOrPut(
                imageUrl
            ) {
                readAspectRatio(
                    file
                )
            }

        return DownloadedReaderImage(
            file = file,
            aspectRatio = aspectRatio
        )
    }

    fun getCachedAspectRatio(
        imageUrl: String
    ): Float? {
        return aspectRatioCache[
            imageUrl
        ]
    }

    private fun getLocalFileOrNull(
        imageUrl: String
    ): File? {
        val uri =
            Uri.parse(
                imageUrl
            )

        if (uri.scheme != FILE_SCHEME) {
            return null
        }

        val localPath =
            uri.path
                ?: throw IOException(
                    "O caminho da página offline é inválido."
                )

        val localFile =
            File(
                localPath
            )

        if (
            !localFile.exists() ||
            !localFile.isFile ||
            localFile.length() <= 0L
        ) {
            throw IOException(
                "A página offline não foi encontrada."
            )
        }

        return localFile
    }

    private fun downloadImageIfNecessary(
        imageUrl: String
    ): File {
        val cachedFile =
            imageCache.getImageFile(
                imageUrl
            )

        if (
            cachedFile.exists() &&
            cachedFile.length() > 0L
        ) {
            return cachedFile
        }

        val temporaryFile =
            File(
                cachedFile.parentFile,
                "${cachedFile.name}.temporary"
            )

        temporaryFile.delete()

        val request =
            Request.Builder()
                .url(
                    imageUrl
                )
                .header(
                    "User-Agent",
                    USER_AGENT
                )
                .build()

        try {
            httpClient
                .newCall(
                    request
                )
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw IOException(
                            "Erro HTTP ${response.code} ao baixar a imagem."
                        )
                    }

                    val responseBody =
                        response.body
                            ?: throw IOException(
                                "O servidor retornou uma imagem vazia."
                            )

                    responseBody
                        .byteStream()
                        .use { inputStream ->
                            temporaryFile
                                .outputStream()
                                .use { outputStream ->
                                    inputStream.copyTo(
                                        outputStream
                                    )
                                }
                        }
                }

            if (
                !temporaryFile.exists() ||
                temporaryFile.length() <= 0L
            ) {
                throw IOException(
                    "O arquivo baixado está vazio."
                )
            }

            if (cachedFile.exists()) {
                cachedFile.delete()
            }

            val renamedSuccessfully =
                temporaryFile.renameTo(
                    cachedFile
                )

            if (!renamedSuccessfully) {
                temporaryFile.copyTo(
                    target = cachedFile,
                    overwrite = true
                )

                temporaryFile.delete()
            }

            if (
                !cachedFile.exists() ||
                cachedFile.length() <= 0L
            ) {
                throw IOException(
                    "Não foi possível salvar a imagem no cache."
                )
            }

            return cachedFile
        } catch (exception: Exception) {
            temporaryFile.delete()

            if (
                cachedFile.exists() &&
                cachedFile.length() <= 0L
            ) {
                cachedFile.delete()
            }

            throw exception
        }
    }

    private fun readAspectRatio(
        file: File
    ): Float {
        val options =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

        BitmapFactory.decodeFile(
            file.absolutePath,
            options
        )

        if (
            options.outWidth <= 0 ||
            options.outHeight <= 0
        ) {
            throw IOException(
                "Não foi possível identificar o tamanho da imagem."
            )
        }

        return options.outWidth.toFloat() /
                options.outHeight.toFloat()
    }

    private companion object {

        const val FILE_SCHEME =
            "file"

        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Mobile Safari/537.36"

        val downloadLocks =
            ConcurrentHashMap<String, Mutex>()

        val aspectRatioCache =
            ConcurrentHashMap<String, Float>()
    }
}