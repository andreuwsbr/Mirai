package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors
import org.json.JSONObject

class AstralChapterApiParser {

    fun parse(
        json: String
    ): AstralChapterApiPage? {
        return runCatching {
            val root =
                JSONObject(json)

            AstralChapterApiPage(
                html = root.optString(
                    AstralToonsSelectors
                        .API_HTML_FIELD,
                    ""
                ),
                hasMore = root.optBoolean(
                    AstralToonsSelectors
                        .API_HAS_MORE_FIELD,
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