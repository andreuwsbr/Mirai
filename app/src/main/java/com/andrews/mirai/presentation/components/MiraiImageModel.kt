package com.andrews.mirai.presentation.components

import android.content.Context
import android.net.Uri
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest

fun createMiraiImageModel(
    context: Context,
    model: Any?
): Any? {
    if (model !is String) {
        return model
    }

    if (!isSaikaiImageUrl(model)) {
        return model
    }

    val headers =
        NetworkHeaders
            .Builder()
            .set(
                "Origin",
                SAIKAI_ORIGIN
            )
            .set(
                "Referer",
                SAIKAI_REFERER
            )
            .set(
                "Accept",
                IMAGE_ACCEPT_HEADER
            )
            .set(
                "Accept-Language",
                ACCEPT_LANGUAGE_HEADER
            )
            .build()

    return ImageRequest
        .Builder(context)
        .data(model)
        .httpHeaders(headers)
        .build()
}

private fun isSaikaiImageUrl(
    imageUrl: String
): Boolean {
    val host =
        runCatching {
            Uri
                .parse(imageUrl)
                .host
                ?.lowercase()
        }.getOrNull()
            .orEmpty()

    return host ==
            SAIKAI_IMAGE_HOST ||
            host.endsWith(
                suffix =
                    ".$SAIKAI_DOMAIN"
            )
}

private const val SAIKAI_DOMAIN =
    "housesaikai.net"

private const val SAIKAI_IMAGE_HOST =
    "s3-beta.housesaikai.net"

private const val SAIKAI_ORIGIN =
    "https://housesaikai.net"

private const val SAIKAI_REFERER =
    "https://housesaikai.net/"

private const val IMAGE_ACCEPT_HEADER =
    "image/avif,image/webp," +
            "image/apng,image/svg+xml," +
            "image/*,*/*;q=0.8"

private const val ACCEPT_LANGUAGE_HEADER =
    "pt-BR,pt;q=0.9," +
            "en-US;q=0.8,en;q=0.7"