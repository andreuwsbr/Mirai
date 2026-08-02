package com.andrews.mirai.data.remote

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClient {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/126.0 Mobile Safari/537.36"

    private val jsonMediaType =
        "application/json; charset=utf-8"
            .toMediaType()

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                HttpLoggingInterceptor.Level.BASIC
        }

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(
                20,
                TimeUnit.SECONDS
            )
            .readTimeout(
                30,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(
                loggingInterceptor
            )
            .build()

    fun get(
        url: String,
        headers: Map<String, String> =
            emptyMap()
    ): HttpResponse {
        val request =
            Request.Builder()
                .url(url)
                .headers(
                    createHeaders(headers)
                )
                .get()
                .build()

        return execute(request)
    }

    fun postJson(
        url: String,
        jsonBody: String,
        headers: Map<String, String> =
            emptyMap()
    ): HttpResponse {
        val request =
            Request.Builder()
                .url(url)
                .headers(
                    createHeaders(headers)
                )
                .post(
                    jsonBody.toRequestBody(
                        jsonMediaType
                    )
                )
                .build()

        return execute(request)
    }

    fun patchJson(
        url: String,
        jsonBody: String,
        headers: Map<String, String> =
            emptyMap()
    ): HttpResponse {
        val request =
            Request.Builder()
                .url(url)
                .headers(
                    createHeaders(headers)
                )
                .patch(
                    jsonBody.toRequestBody(
                        jsonMediaType
                    )
                )
                .build()

        return execute(request)
    }

    fun delete(
        url: String,
        headers: Map<String, String> =
            emptyMap()
    ): HttpResponse {
        val request =
            Request.Builder()
                .url(url)
                .headers(
                    createHeaders(headers)
                )
                .delete()
                .build()

        return execute(request)
    }

    private fun createHeaders(
        customHeaders: Map<String, String>
    ): Headers {
        val builder =
            Headers.Builder()
                .add(
                    "User-Agent",
                    USER_AGENT
                )
                .add(
                    "Accept",
                    "application/json, " +
                            "text/html, " +
                            "application/xhtml+xml, " +
                            "*/*;q=0.8"
                )
                .add(
                    "Accept-Language",
                    "pt-BR,pt;q=0.9," +
                            "en-US;q=0.8,en;q=0.7"
                )
                .add(
                    "Cache-Control",
                    "no-cache"
                )
                .add(
                    "Pragma",
                    "no-cache"
                )

        customHeaders.forEach {
                (name, value) ->

            builder.set(
                name,
                value
            )
        }

        return builder.build()
    }

    private fun execute(
        request: Request
    ): HttpResponse {
        return try {
            client
                .newCall(request)
                .execute()
                .use { response ->
                    HttpResponse(
                        code = response.code,
                        body =
                            response
                                .body
                                ?.string()
                                .orEmpty(),
                        finalUrl =
                            response
                                .request
                                .url
                                .toString()
                    )
                }
        } catch (throwable: Throwable) {
            HttpResponse(
                code = 0,
                body = "",
                finalUrl =
                    request.url.toString(),
                errorMessage =
                    throwable.message
                        ?: throwable
                            .javaClass
                            .simpleName
            )
        }
    }
}

data class HttpResponse(
    val code: Int,
    val body: String,
    val finalUrl: String,
    val errorMessage: String? = null
) {
    val isSuccessful: Boolean
        get() = code in 200..299
}