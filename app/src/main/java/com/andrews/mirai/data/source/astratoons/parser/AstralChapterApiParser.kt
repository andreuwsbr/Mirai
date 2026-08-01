package com.andrews.mirai.data.source.astratoons.parser

import org.json.JSONObject

class AstralChapterApiParser {

    fun parse(
        json: String
    ): AstralChapterApiPage? {
        return runCatching {
            val root = JSONObject(json)

            AstralChapterApiPage(
                html = root.optString(
                    "html",
                    ""
                ),
                hasMore = root.optBoolean(
                    "hasMore",
                    false
                )
            )
        }.getOrNull()
    }
}

data class AstralChapterApiPage(
    val html: String,
    val hasMore: Boolean
)