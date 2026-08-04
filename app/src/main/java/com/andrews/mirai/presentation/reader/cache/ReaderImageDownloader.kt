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
import java.util.concurrent.TimeUnit


data class DownloadedReaderImage(
    val file: File,
    val aspectRatio: Float
)

class ReaderImageDownloader(
    private val imageCache: ReaderImageCache,
    private val httpClient: OkHttpClient =
        createHttpClient()
) {

    suspend fun download(
        imageUrl: String
    ): File = withContext(
        Dispatchers.IO
    ) {
        val localFile =
            getLocalFileOrNull(
                imageUrl
            )

        if (localFile != null) {
            validateImageFile(
                file = localFile,
                imageUrl = imageUrl,
                deleteIfInvalid = false
            )


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
            aspectRatioCache[
                imageUrl
            ] ?: readAspectRatio(
                file = file,
                imageUrl = imageUrl
            ).also { calculatedRatio ->
                aspectRatioCache[
                    imageUrl
                ] = calculatedRatio
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
            cachedFile.isFile &&
            cachedFile.length() > 0L
        ) {
            val cachedFileIsValid =
                runCatching {
                    validateImageFile(
                        file = cachedFile,
                        imageUrl = imageUrl,
                        deleteIfInvalid = true
                    )
                }.isSuccess

            if (cachedFileIsValid) {
                return cachedFile
            }
        }

        imageCache.deleteImage(
            imageUrl
        )

        imageCache.deleteTemporaryImage(
            imageUrl
        )

        aspectRatioCache.remove(
            imageUrl
        )

        val temporaryFile =
            File(
                cachedFile.parentFile,
                "${cachedFile.name}$TEMPORARY_SUFFIX"
            )

        temporaryFile
            .parentFile
            ?.mkdirs()

        val request =
            createImageRequest(
                imageUrl
            )

        try {
            httpClient
                .newCall(
                    request
                )
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw IOException(
                            "Erro HTTP ${response.code} " +
                                    "ao baixar a imagem."
                        )
                    }

                    val responseBody =
                        response.body
                            ?: throw IOException(
                                "O servidor retornou " +
                                        "uma resposta vazia."
                            )

                    val contentType =
                        responseBody
                            .contentType()
                            ?.toString()
                            .orEmpty()

                    if (
                        contentType.isNotBlank() &&
                        !isAcceptedImageContentType(
                            contentType
                        )
                    ) {
                        throw IOException(
                            "O servidor retornou um arquivo " +
                                    "inválido no lugar da imagem " +
                                    "($contentType)."
                        )
                    }

                    responseBody
                        .byteStream()
                        .use { inputStream ->
                            temporaryFile
                                .outputStream()
                                .buffered()
                                .use { outputStream ->
                                    inputStream.copyTo(
                                        outputStream
                                    )
                                }
                        }
                }

            if (
                !temporaryFile.exists() ||
                !temporaryFile.isFile ||
                temporaryFile.length() <= 0L
            ) {
                throw IOException(
                    "O arquivo de imagem baixado está vazio."
                )
            }

            /*
             * Antes de mover para o cache definitivo,
             * confirma que o Android consegue realmente
             * interpretar largura e altura da imagem.
             */
            validateImageFile(
                file = temporaryFile,
                imageUrl = imageUrl,
                deleteIfInvalid = true
            )

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

            validateImageFile(
                file = cachedFile,
                imageUrl = imageUrl,
                deleteIfInvalid = true
            )

            return cachedFile
        } catch (exception: Exception) {
            temporaryFile.delete()
            cachedFile.delete()

            aspectRatioCache.remove(
                imageUrl
            )

            throw exception
        }
    }

    private fun createImageRequest(
        imageUrl: String
    ): Request {
        val requestBuilder =
            Request.Builder()
                .url(
                    imageUrl
                )
                .header(
                    "User-Agent",
                    USER_AGENT
                )
                .header(
                    "Accept",
                    IMAGE_ACCEPT_HEADER
                )
                .header(
                    "Accept-Language",
                    ACCEPT_LANGUAGE_HEADER
                )
                .header(
                    "Cache-Control",
                    "no-cache"
                )

        if (isSaikaiImage(imageUrl)) {
            requestBuilder
                .header(
                    "Origin",
                    SAIKAI_ORIGIN
                )
                .header(
                    "Referer",
                    SAIKAI_REFERER
                )
        }

        return requestBuilder
            .get()
            .build()
    }

    private fun validateImageFile(
        file: File,
        imageUrl: String,
        deleteIfInvalid: Boolean
    ) {
        try {
            if (
                !file.exists() ||
                !file.isFile ||
                file.length() <= 0L
            ) {
                throw IOException(
                    "O arquivo da página está vazio ou não existe."
                )
            }

            val dimensions =
                readImageDimensions(
                    file
                )

            if (
                dimensions.width <= 0 ||
                dimensions.height <= 0
            ) {
                throw IOException(
                    "O arquivo baixado não é uma imagem válida."
                )
            }
        } catch (exception: Exception) {
            aspectRatioCache.remove(
                imageUrl
            )

            if (deleteIfInvalid) {
                file.delete()
            }

            throw exception
        }
    }

    private fun readAspectRatio(
        file: File,
        imageUrl: String
    ): Float {
        return try {
            val dimensions =
                readImageDimensions(
                    file
                )

            dimensions.width.toFloat() /
                    dimensions.height.toFloat()
        } catch (exception: Exception) {
            aspectRatioCache.remove(
                imageUrl
            )

            if (
                Uri.parse(imageUrl).scheme !=
                FILE_SCHEME
            ) {
                file.delete()
            }

            throw IOException(
                "Não foi possível identificar " +
                        "o tamanho da imagem.",
                exception
            )
        }
    }

    private fun readImageDimensions(
        file: File
    ): ImageDimensions {
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
                "O arquivo não contém uma imagem reconhecida."
            )
        }

        return ImageDimensions(
            width = options.outWidth,
            height = options.outHeight
        )
    }

    private fun isAcceptedImageContentType(
        contentType: String
    ): Boolean {
        val normalizedType =
            contentType
                .substringBefore(';')
                .trim()
                .lowercase()

        return normalizedType.startsWith(
            prefix = "image/"
        ) ||
                normalizedType ==
                BINARY_CONTENT_TYPE
    }

    private fun isSaikaiImage(
        imageUrl: String
    ): Boolean {
        val host =
            runCatching {
                Uri.parse(
                    imageUrl
                ).host
            }.getOrNull()
                ?.lowercase()
                .orEmpty()

        return host ==
                SAIKAI_IMAGE_HOST ||
                host.endsWith(
                    suffix =
                        ".$SAIKAI_DOMAIN"
                )
    }

    private data class ImageDimensions(
        val width: Int,
        val height: Int
    )

    private companion object {

        const val FILE_SCHEME =
            "file"

        const val TEMPORARY_SUFFIX =
            ".temporary"

        const val BINARY_CONTENT_TYPE =
            "application/octet-stream"

        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/126.0 Mobile Safari/537.36"

        const val IMAGE_ACCEPT_HEADER =
            "image/avif,image/webp," +
                    "image/apng,image/svg+xml," +
                    "image/*,*/*;q=0.8"

        const val ACCEPT_LANGUAGE_HEADER =
            "pt-BR,pt;q=0.9," +
                    "en-US;q=0.8,en;q=0.7"

        const val SAIKAI_DOMAIN =
            "housesaikai.net"

        const val SAIKAI_IMAGE_HOST =
            "s3-beta.housesaikai.net"

        const val SAIKAI_ORIGIN =
            "https://housesaikai.net"

        const val SAIKAI_REFERER =
            "https://housesaikai.net/"

        val downloadLocks =
            ConcurrentHashMap<String, Mutex>()

        val aspectRatioCache =
            ConcurrentHashMap<String, Float>()

        fun createHttpClient():
                OkHttpClient {
            return OkHttpClient
                .Builder()
                .connectTimeout(
                    20,
                    TimeUnit.SECONDS
                )
                .readTimeout(
                    60,
                    TimeUnit.SECONDS
                )
                .writeTimeout(
                    60,
                    TimeUnit.SECONDS
                )
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        }
    }
}