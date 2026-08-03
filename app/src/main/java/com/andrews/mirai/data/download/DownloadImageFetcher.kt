package com.andrews.mirai.data.download

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownloadImageFetcher(
    private val httpClient: OkHttpClient =
        createHttpClient()
) {

    suspend fun downloadToFile(
        imageUrl: String,
        destinationFile: File,
        referer: String? = null
    ): Long = withContext(
        Dispatchers.IO
    ) {
        destinationFile
            .parentFile
            ?.mkdirs()

        if (
            destinationFile.exists() &&
            destinationFile.length() > 0L
        ) {
            return@withContext destinationFile.length()
        }

        val temporaryFile =
            File(
                destinationFile.parentFile,
                "${destinationFile.name}.temporary"
            )

        temporaryFile.delete()

        val requestBuilder =
            Request.Builder()
                .url(imageUrl)
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
        } else if (
            !referer.isNullOrBlank()
        ) {
            requestBuilder.header(
                "Referer",
                referer
            )
        }

        try {
            httpClient
                .newCall(
                    requestBuilder
                        .get()
                        .build()
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
                                        "uma imagem vazia."
                            )

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
                temporaryFile.length() <= 0L
            ) {
                throw IOException(
                    "O arquivo baixado está vazio."
                )
            }

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val renamed =
                temporaryFile.renameTo(
                    destinationFile
                )

            if (!renamed) {
                temporaryFile.copyTo(
                    target = destinationFile,
                    overwrite = true
                )

                temporaryFile.delete()
            }

            if (
                !destinationFile.exists() ||
                destinationFile.length() <= 0L
            ) {
                throw IOException(
                    "Não foi possível salvar " +
                            "a imagem baixada."
                )
            }

            destinationFile.length()
        } catch (exception: Exception) {
            temporaryFile.delete()

            if (
                destinationFile.exists() &&
                destinationFile.length() <= 0L
            ) {
                destinationFile.delete()
            }

            throw exception
        }
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
                    ".$SAIKAI_DOMAIN"
                )
    }

    private companion object {

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

        fun createHttpClient():
                OkHttpClient {
            return OkHttpClient
                .Builder()
                .connectTimeout(
                    20,
                    TimeUnit.SECONDS
                )
                .readTimeout(
                    45,
                    TimeUnit.SECONDS
                )
                .writeTimeout(
                    45,
                    TimeUnit.SECONDS
                )
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }
}