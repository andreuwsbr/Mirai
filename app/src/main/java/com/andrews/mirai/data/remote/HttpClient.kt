package com.andrews.mirai.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClient {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/126.0 Mobile Safari/537.36"

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Requisição para páginas HTML.
     * Usada pelo Manga Livre e outras fontes com Jsoup.
     */
    fun get(url: String): HttpResponse {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header(
                "Accept",
                "text/html,application/xhtml+xml," +
                        "application/xml;q=0.9,*/*;q=0.8"
            )
            .header(
                "Accept-Language",
                "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"
            )
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .get()
            .build()

        return execute(request)
    }

    /**
     * Requisição para APIs JSON.
     * Usada pelo Império da Britannia.
     */
    fun getJson(url: String): HttpResponse {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header(
                "Accept-Language",
                "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"
            )
            .header(
                "Origin",
                "https://imperiodabritannia.net"
            )
            .header(
                "Referer",
                "https://imperiodabritannia.net/"
            )
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("Accept-Encoding", "identity")
            .get()
            .build()

        return execute(request)
    }

    private fun execute(
        request: Request
    ): HttpResponse {
        return try {
            client.newCall(request)
                .execute()
                .use { response ->
                    HttpResponse(
                        code = response.code,
                        body = response.body
                            ?.string()
                            .orEmpty(),
                        finalUrl = response.request
                            .url
                            .toString()
                    )
                }
        } catch (throwable: Throwable) {
            HttpResponse(
                code = 0,
                body = "",
                finalUrl = request.url.toString(),
                errorMessage = throwable.message
                    ?: throwable.javaClass.simpleName
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