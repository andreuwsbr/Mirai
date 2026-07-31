package com.andrews.mirai.presentation.reader.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ReaderImageDownloader(
    private val imageCache: ReaderImageCache,
    private val httpClient: OkHttpClient = OkHttpClient()
) {

    suspend fun download(
        imageUrl: String
    ): File = withContext(Dispatchers.IO) {

        val downloadLock = downloadLocks.getOrPut(imageUrl) {
            Mutex()
        }

        downloadLock.withLock {
            downloadImageIfNecessary(imageUrl)
        }
    }

    private fun downloadImageIfNecessary(
        imageUrl: String
    ): File {
        val cachedFile = imageCache.getImageFile(imageUrl)

        if (cachedFile.exists() && cachedFile.length() > 0L) {
            return cachedFile
        }

        val temporaryFile = File(
            cachedFile.parentFile,
            "${cachedFile.name}.temporary"
        )

        temporaryFile.delete()

        val request = Request.Builder()
            .url(imageUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Mobile Safari/537.36"
            )
            .build()

        try {
            httpClient
                .newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {
                        throw IOException(
                            "Erro HTTP ${response.code} ao baixar a imagem."
                        )
                    }

                    val responseBody = response.body
                        ?: throw IOException(
                            "O servidor retornou uma imagem vazia."
                        )

                    responseBody.byteStream().use { inputStream ->
                        temporaryFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

            if (temporaryFile.length() <= 0L) {
                throw IOException(
                    "O arquivo baixado está vazio."
                )
            }

            if (cachedFile.exists()) {
                cachedFile.delete()
            }

            val renamedSuccessfully =
                temporaryFile.renameTo(cachedFile)

            if (!renamedSuccessfully) {
                temporaryFile.copyTo(
                    target = cachedFile,
                    overwrite = true
                )

                temporaryFile.delete()
            }

            if (!cachedFile.exists() || cachedFile.length() <= 0L) {
                throw IOException(
                    "Não foi possível salvar a imagem no cache."
                )
            }

            return cachedFile
        } catch (exception: Exception) {
            temporaryFile.delete()

            if (cachedFile.length() <= 0L) {
                cachedFile.delete()
            }

            throw exception
        }
    }

    private companion object {
        val downloadLocks =
            ConcurrentHashMap<String, Mutex>()
    }
}