package com.andrews.mirai.presentation.reader.cache

import android.content.Context
import java.io.File
import java.security.MessageDigest

class ReaderImageCache(
    context: Context
) {

    private val cacheDirectory: File =
        File(
            context.applicationContext.cacheDir,
            CACHE_DIRECTORY_NAME
        ).apply {
            mkdirs()
        }

    fun getImageFile(
        url: String
    ): File {
        return File(
            cacheDirectory,
            createCacheKey(url)
        )
    }

    fun hasImage(
        url: String
    ): Boolean {
        val file =
            getImageFile(url)

        return file.exists() &&
                file.isFile &&
                file.length() > 0L
    }

    fun deleteImage(
        url: String
    ): Boolean {
        val file =
            getImageFile(url)

        if (!file.exists()) {
            return true
        }

        return file.delete()
    }

    fun deleteTemporaryImage(
        url: String
    ): Boolean {
        val temporaryFile =
            File(
                cacheDirectory,
                "${
                    createCacheKey(url)
                }$TEMPORARY_SUFFIX"
            )

        if (!temporaryFile.exists()) {
            return true
        }

        return temporaryFile.delete()
    }

    private fun createCacheKey(
        text: String
    ): String {
        val bytes =
            MessageDigest
                .getInstance(
                    HASH_ALGORITHM
                )
                .digest(
                    text.toByteArray()
                )

        return bytes.joinToString(
            separator = ""
        ) { byte ->
            "%02x".format(byte)
        }
    }

    private companion object {

        const val CACHE_DIRECTORY_NAME =
            "reader_pages"

        const val TEMPORARY_SUFFIX =
            ".temporary"

        const val HASH_ALGORITHM =
            "MD5"
    }
}