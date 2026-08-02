package com.andrews.mirai.data.source.astratoons

internal object AstralToonsSelectors {

    const val CATALOG_CARD =
        "a[href*='/comics/']"

    const val CATALOG_TITLE =
        "h2"

    const val CATALOG_COVER =
        "img"

    const val CATALOG_DESCRIPTION =
        "p"

    const val CATALOG_METADATA =
        "span"

    const val CHAPTER_LINK =
        "a[href*='/capitulo/']"

    const val CHAPTER_LOCK_ATTRIBUTE =
        "x-data"

    const val CHAPTER_LOCKED_VALUE =
        "islocked: true"

    val CHAPTER_NUMBER_FROM_URL =
        Regex(
            pattern =
                """/capitulo/(\d+(?:\.\d+)?)""",
            option =
                RegexOption.IGNORE_CASE
        )

    val CHAPTER_NUMBER_FROM_TEXT =
        Regex(
            pattern =
                """cap[ií]tulo\s+(\d+(?:\.\d+)?)""",
            option =
                RegexOption.IGNORE_CASE
        )

    val CHAPTER_NAME =
        Regex(
            pattern =
                """cap[ií]tulo\s+\d+(?:\.\d+)?""",
            option =
                RegexOption.IGNORE_CASE
        )

    val CHAPTER_DATE_PATTERNS = listOf(
        Regex(
            pattern =
                """há\s+\d+\s+(?:minuto|minutos|hora|horas|dia|dias|semana|semanas|mês|meses|ano|anos)""",
            option =
                RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = """ontem""",
            option =
                RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = """hoje""",
            option =
                RegexOption.IGNORE_CASE
        )
    )

    const val DETAILS_TITLE =
        "h1"

    const val DETAILS_METADATA =
        "span"

    const val PAGE_STORAGE_PATH =
        "/storage/chapters/"

    const val PAGE_IMAGE =
        "img[src*='/storage/chapters/'], " +
                "img[data-src*='/storage/chapters/'], " +
                "img[data-lazy-src*='/storage/chapters/']"

    val PAGE_IMAGE_ATTRIBUTES = listOf(
        "src",
        "data-src",
        "data-lazy-src"
    )

    val PAGE_URL =
        Regex(
            pattern =
                """https?://[^"'\\\s<>]+/storage/chapters/[^"'\\\s<>]+?\.(?:jpg|jpeg|png|webp|avif)(?:\?[^"'\\\s<>]*)?""",
            option =
                RegexOption.IGNORE_CASE
        )

    val PAGE_NUMBER =
        Regex("""\d+""")

    val COMIC_ID_PATTERNS = listOf(
        Regex(
            pattern =
                """/api/comics/(\d+)/chapters""",
            option =
                RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern =
                """comicId\s*[:=]\s*["']?(\d+)""",
            option =
                RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern =
                """comic_id\s*[:=]\s*["']?(\d+)""",
            option =
                RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern =
                """data-comic-id=["'](\d+)["']""",
            option =
                RegexOption.IGNORE_CASE
        )
    )

    const val API_HTML_FIELD =
        "html"

    const val API_HAS_MORE_FIELD =
        "hasMore"
}