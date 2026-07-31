package com.andrews.mirai.presentation.reader.cache

import android.content.Context
import java.io.File
import java.security.MessageDigest

class ReaderImageCache(
    private val context: Context
) {

    private val cacheDir: File =
        File(context.cacheDir, "reader_pages").apply {
            mkdirs()
        }

    fun getImageFile(url: String): File {
        return File(cacheDir, md5(url))
    }

    fun hasImage(url: String): Boolean {
        return getImageFile(url).exists()
    }

    private fun md5(text: String): String {
        val bytes = MessageDigest
            .getInstance("MD5")
            .digest(text.toByteArray())

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }
}