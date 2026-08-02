package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors

class AstralComicIdParser {

    fun parse(
        html: String
    ): Long? {
        AstralToonsSelectors
            .COMIC_ID_PATTERNS
            .forEach { pattern ->
                val value = pattern
                    .find(html)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.toLongOrNull()

                if (value != null) {
                    return value
                }
            }

        return null
    }
}