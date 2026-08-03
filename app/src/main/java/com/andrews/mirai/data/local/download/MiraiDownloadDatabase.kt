package com.andrews.mirai.data.local.download

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DownloadedMangaEntity::class,
        DownloadedChapterEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DownloadConverters::class
)
abstract class MiraiDownloadDatabase :
    RoomDatabase() {

    abstract fun downloadDao():
            DownloadDao

    companion object {

        private const val DATABASE_NAME =
            "mirai_downloads.db"

        @Volatile
        private var instance:
                MiraiDownloadDatabase? = null

        fun getInstance(
            context: Context
        ): MiraiDownloadDatabase {
            return instance
                ?: synchronized(this) {
                    instance
                        ?: createDatabase(
                            context.applicationContext
                        ).also { database ->
                            instance = database
                        }
                }
        }

        private fun createDatabase(
            context: Context
        ): MiraiDownloadDatabase {
            return Room.databaseBuilder(
                context,
                MiraiDownloadDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}