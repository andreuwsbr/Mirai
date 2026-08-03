package com.andrews.mirai.data.source.saikai

object SaikaiHeaders {

    fun apiHeaders():
            Map<String, String> {
        return mapOf(
            "Accept" to
                    "application/json, " +
                    "text/plain, */*",
            "Origin" to
                    SaikaiUrls.BASE_URL,
            "Referer" to
                    "${SaikaiUrls.BASE_URL}/"
        )
    }

    fun imageHeaders():
            Map<String, String> {
        return mapOf(
            "Accept" to
                    "image/avif,image/webp," +
                    "image/apng,image/svg+xml," +
                    "image/*,*/*;q=0.8",
            "Origin" to
                    SaikaiUrls.BASE_URL,
            "Referer" to
                    "${SaikaiUrls.BASE_URL}/"
        )
    }
}