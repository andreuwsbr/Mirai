package com.andrews.mirai.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor(loggingInterceptor)
        .build()

    fun get(url: String): HttpResponse {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"
            )
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            return HttpResponse(
                code = response.code,
                body = response.body?.string().orEmpty(),
                finalUrl = response.request.url.toString()
            )
        }
    }
}

data class HttpResponse(
    val code: Int,
    val body: String,
    val finalUrl: String
) {
    val isSuccessful: Boolean
        get() = code in 200..299
}