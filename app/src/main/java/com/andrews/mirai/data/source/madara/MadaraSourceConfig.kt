package com.andrews.mirai.data.source.madara

data class MadaraSourceConfig(

    val id: String,

    val name: String,

    val baseUrl: String,

    val language: String = "pt-BR",

    val mangaPaths: List<String> = listOf(
        "/manga/"
    ),

    val popularPath: String = "/",

    val searchPath: String = "/?s=%s",

    val supportsLatest: Boolean = true,

    val supportsSearch: Boolean = true,

    val supportsPopular: Boolean = true
)