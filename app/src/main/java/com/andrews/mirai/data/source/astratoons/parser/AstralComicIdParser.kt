package com.andrews.mirai.data.source.astratoons.parser

class AstralComicIdParser {

    fun parse(
        html: String
    ): Long? {
        val patterns = listOf(
            Regex(
                pattern = """/api/comics/(\d+)/chapters""",
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """comicId\s*[:=]\s*["']?(\d+)""",
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """comic_id\s*[:=]\s*["']?(\d+)""",
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """data-comic-id=["'](\d+)["']""",
                option = RegexOption.IGNORE_CASE
            )
        )

        patterns.forEach { pattern ->
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