package com.andrews.mirai.data.source.mangalivre

internal object MangaLivreSelectors {

    const val CHAPTER_LINK =
        "a.chapter-link"

    const val CHAPTER_NUMBER =
        ".chapter-number"

    const val CHAPTER_ITEM =
        ".chapter-item"

    const val CHAPTER_DATE =
        ".chapter-date"

    val DETAILS_TITLE = listOf(
        ".manga-title",
        "h1.manga-title",
        ".post-title h1",
        ".manga-title h1",
        ".entry-title",
        ".post-title",
        "h1"
    )

    val DETAILS_DESCRIPTION = listOf(
        ".synopsis-content",
        ".manga-synopsis .synopsis-content",
        ".description-summary .summary__content",
        ".summary__content",
        ".manga-excerpt",
        ".description",
        ".sinopse",
        ".summary",
        ".entry-content p"
    )

    val DETAILS_COVER = listOf(
        ".manga-cover img",
        ".summary_image img",
        ".manga-thumb img",
        ".manga-cover-image",
        ".post-content_item img",
        ".tab-summary img",
        "article img"
    )

    const val DETAILS_META_ITEM =
        ".manga-meta-item"

    const val DETAILS_META_LABEL =
        ".meta-label"

    const val DETAILS_META_VALUE =
        ".meta-value"

    val DETAILS_AUTHOR_FALLBACK = listOf(
        ".author-content",
        ".post-content_item:has(" +
                ".summary-heading:matchesOwn((?i)autor)" +
                ") .summary-content",
        ".post-content_item:has(" +
                "h5:matchesOwn((?i)autor)" +
                ") .summary-content",
        ".manga-author",
        "[class*=author]"
    )

    val DETAILS_GENRES = listOf(
        ".manga-tag",
        ".manga-tags .manga-tag",
        ".genres-content a",
        ".genres a",
        ".manga-genres a",
        "a[href*=genero]",
        "a[href*=genre]"
    )

    val DETAILS_IMAGE_ATTRIBUTES = listOf(
        "data-src",
        "data-lazy-src",
        "data-original",
        "src"
    )

    const val PAGE_CONTAINER =
        ".chapter-image-container"

    const val PAGE_PRIMARY_IMAGE =
        "img.chapter-image"

    const val PAGE_FALLBACK_IMAGE =
        "img"

    const val PAGE_NUMBER_ATTRIBUTE =
        "data-page"

    const val PAGE_ID_PREFIX =
        "page-"

    val PAGE_ORIGINAL_IMAGE_ATTRIBUTES =
        listOf(
            "data-full",
            "data-full-url",
            "data-original",
            "data-original-src",
            "data-high-res-src",
            "data-hires"
        )

    val PAGE_SRCSET_ATTRIBUTES =
        listOf(
            "data-srcset",
            "data-lazy-srcset",
            "srcset"
        )

    val PAGE_FALLBACK_IMAGE_ATTRIBUTES =
        listOf(
            "data-src",
            "data-lazy-src",
            "data-url",
            "src"
        )
}