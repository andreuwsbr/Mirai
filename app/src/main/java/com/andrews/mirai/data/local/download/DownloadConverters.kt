package com.andrews.mirai.data.local.download

import androidx.room.TypeConverter

class DownloadConverters {

    @TypeConverter
    fun fromDownloadStatus(
        status: DownloadStatus
    ): String {
        return status.name
    }

    @TypeConverter
    fun toDownloadStatus(
        value: String
    ): DownloadStatus {
        return runCatching {
            DownloadStatus.valueOf(value)
        }.getOrDefault(
            DownloadStatus.FAILED
        )
    }
}