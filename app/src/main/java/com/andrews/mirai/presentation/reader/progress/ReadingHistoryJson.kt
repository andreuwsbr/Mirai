package com.andrews.mirai.presentation.reader.progress

import org.json.JSONArray
import org.json.JSONObject

internal fun decodeReadingHistory(
    savedHistory: String
): List<ReadingHistoryItem> {
    return runCatching {
        val jsonArray =
            JSONArray(savedHistory)

        buildList {
            for (
            index in 0 until
                    jsonArray.length()
            ) {
                val item =
                    jsonArray.optJSONObject(
                        index
                    ) ?: continue

                val mangaId =
                    item.optString(
                        "mangaId"
                    )

                val chapterId =
                    item.optString(
                        "chapterId"
                    )

                if (
                    mangaId.isBlank() ||
                    chapterId.isBlank()
                ) {
                    continue
                }

                val savedSourceId =
                    item.optString(
                        "sourceId"
                    )

                val resolvedSourceId =
                    savedSourceId.ifBlank {
                        detectReadingSourceId(
                            mangaId = mangaId,
                            chapterId = chapterId
                        )
                    }

                add(
                    ReadingHistoryItem(
                        sourceId =
                            resolvedSourceId,
                        mangaId = mangaId,
                        mangaTitle =
                            item.optString(
                                "mangaTitle"
                            ),
                        mangaCoverUrl =
                            item
                                .optString(
                                    "mangaCoverUrl"
                                )
                                .takeIf { value ->
                                    value.isNotBlank()
                                },
                        chapterId = chapterId,
                        chapterName =
                            item.optString(
                                "chapterName"
                            ),
                        pageIndex =
                            item.optInt(
                                "pageIndex"
                            ).coerceAtLeast(0),
                        totalPages =
                            item.optInt(
                                "totalPages"
                            ).coerceAtLeast(0),
                        readAt =
                            item.optLong(
                                "readAt"
                            )
                    )
                )
            }
        }
            .distinctBy { item ->
                readingHistoryIdentityKey(
                    sourceId = item.sourceId,
                    mangaId = item.mangaId
                )
            }
            .sortedByDescending { item ->
                item.readAt
            }
    }.getOrDefault(
        emptyList()
    )
}

internal fun encodeReadingHistory(
    history: List<ReadingHistoryItem>
): String {
    val jsonArray = JSONArray()

    history
        .distinctBy { item ->
            readingHistoryIdentityKey(
                sourceId = item.sourceId,
                mangaId = item.mangaId
            )
        }
        .take(MAX_READING_HISTORY_ITEMS)
        .forEach { item ->
            jsonArray.put(
                JSONObject().apply {
                    put(
                        "sourceId",
                        item.sourceId
                    )
                    put(
                        "mangaId",
                        item.mangaId
                    )
                    put(
                        "mangaTitle",
                        item.mangaTitle
                    )
                    put(
                        "mangaCoverUrl",
                        item.mangaCoverUrl
                            .orEmpty()
                    )
                    put(
                        "chapterId",
                        item.chapterId
                    )
                    put(
                        "chapterName",
                        item.chapterName
                    )
                    put(
                        "pageIndex",
                        item.pageIndex
                    )
                    put(
                        "totalPages",
                        item.totalPages
                    )
                    put(
                        "readAt",
                        item.readAt
                    )
                }
            )
        }

    return jsonArray.toString()
}