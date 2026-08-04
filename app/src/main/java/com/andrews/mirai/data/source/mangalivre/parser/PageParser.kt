package com.andrews.mirai.data.source.mangalivre.parser

import com.andrews.mirai.data.source.mangalivre.MangaLivreSelectors
import com.andrews.mirai.domain.model.ReaderPage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI

class PageParser(
    private val baseUrl: String
) {

    fun parse(
        html: String
    ): List<ReaderPage> {
        val document =
            Jsoup.parse(
                html,
                baseUrl
            )

        val pagesByUrl =
            linkedMapOf<String, ReaderPage>()

        document
            .select(
                MangaLivreSelectors
                    .PAGE_CONTAINER
            )
            .forEachIndexed {
                    position,
                    container ->

                val page =
                    parsePage(
                        container =
                            container,
                        fallbackIndex =
                            position
                    ) ?: return@forEachIndexed

                /*
                 * LinkedHashMap preserva a ordem
                 * em que as páginas foram encontradas.
                 */
                pagesByUrl.putIfAbsent(
                    page.imageUrl,
                    page
                )
            }

        /*
         * A ordem final segue o número da página
         * informado pelo próprio container.
         */
        return pagesByUrl
            .values
            .sortedBy { page ->
                page.index
            }
    }

    private fun parsePage(
        container: Element,
        fallbackIndex: Int
    ): ReaderPage? {
        val image =
            container.selectFirst(
                MangaLivreSelectors
                    .PAGE_PRIMARY_IMAGE
            ) ?: container.selectFirst(
                MangaLivreSelectors
                    .PAGE_FALLBACK_IMAGE
            ) ?: return null

        val imageUrl =
            extractBestImageUrl(
                image
            ) ?: return null

        val pageNumber =
            container
                .attr(
                    MangaLivreSelectors
                        .PAGE_NUMBER_ATTRIBUTE
                )
                .toIntOrNull()
                ?: container
                    .id()
                    .removePrefix(
                        MangaLivreSelectors
                            .PAGE_ID_PREFIX
                    )
                    .toIntOrNull()
                ?: fallbackIndex + 1

        return ReaderPage(
            index =
                pageNumber - 1,
            imageUrl =
                imageUrl
        )
    }

    private fun extractBestImageUrl(
        image: Element
    ): String? {
        /*
         * 1. Primeiro usamos somente atributos
         * que indicam explicitamente a imagem
         * original ou de alta resolução.
         */
        MangaLivreSelectors
            .PAGE_ORIGINAL_IMAGE_ATTRIBUTES
            .forEach { attribute ->
                val rawUrl =
                    image
                        .attr(attribute)
                        .trim()

                if (isUsableImageUrl(rawUrl)) {
                    return resolveUrl(
                        image = image,
                        rawUrl = rawUrl
                    )
                }
            }

        /*
         * 2. Depois analisamos o srcset e
         * selecionamos a maior opção disponível.
         */
        MangaLivreSelectors
            .PAGE_SRCSET_ATTRIBUTES
            .forEach { attribute ->
                val srcSet =
                    image
                        .attr(attribute)
                        .trim()

                val largestUrl =
                    extractLargestUrlFromSrcSet(
                        srcSet
                    )

                if (
                    !largestUrl.isNullOrBlank() &&
                    isUsableImageUrl(
                        largestUrl
                    )
                ) {
                    return resolveUrl(
                        image = image,
                        rawUrl = largestUrl
                    )
                }
            }

        /*
         * 3. data-src e src ficam apenas como
         * último recurso, pois podem apontar
         * para uma miniatura ou versão reduzida.
         */
        MangaLivreSelectors
            .PAGE_FALLBACK_IMAGE_ATTRIBUTES
            .forEach { attribute ->
                val rawUrl =
                    image
                        .attr(attribute)
                        .trim()

                if (isUsableImageUrl(rawUrl)) {
                    return resolveUrl(
                        image = image,
                        rawUrl = rawUrl
                    )
                }
            }

        return null
    }

    private fun extractLargestUrlFromSrcSet(
        srcSet: String
    ): String? {
        if (srcSet.isBlank()) {
            return null
        }

        return srcSet
            .split(",")
            .mapNotNull { candidate ->
                val parts =
                    candidate
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                val url =
                    parts
                        .firstOrNull()
                        ?.trim()
                        ?.takeIf { value ->
                            isUsableImageUrl(
                                value
                            )
                        }
                        ?: return@mapNotNull null

                val descriptor =
                    parts
                        .getOrNull(1)
                        .orEmpty()
                        .trim()
                        .lowercase()

                val size =
                    when {
                        descriptor.endsWith("w") -> {
                            descriptor
                                .removeSuffix("w")
                                .toDoubleOrNull()
                                ?: 1.0
                        }

                        descriptor.endsWith("x") -> {
                            descriptor
                                .removeSuffix("x")
                                .toDoubleOrNull()
                                ?.times(
                                    DENSITY_WEIGHT
                                )
                                ?: 1.0
                        }

                        else -> {
                            1.0
                        }
                    }

                ImageCandidate(
                    url = url,
                    size = size
                )
            }
            .maxByOrNull { candidate ->
                candidate.size
            }
            ?.url
    }

    private fun isUsableImageUrl(
        value: String
    ): Boolean {
        val normalizedValue =
            value
                .trim()
                .removePrefix("\"")
                .removeSuffix("\"")

        if (
            normalizedValue.isBlank() ||
            normalizedValue == "#" ||
            normalizedValue.startsWith(
                "data:image/",
                ignoreCase = true
            )
        ) {
            return false
        }

        return true
    }

    private fun resolveUrl(
        image: Element,
        rawUrl: String
    ): String {
        val cleanUrl =
            rawUrl
                .trim()
                .removePrefix("\"")
                .removeSuffix("\"")

        return runCatching {
            val pageBaseUrl =
                image
                    .baseUri()
                    .ifBlank {
                        baseUrl
                    }

            URI(pageBaseUrl)
                .resolve(cleanUrl)
                .toString()
        }.getOrDefault(
            cleanUrl
        )
    }

    private data class ImageCandidate(
        val url: String,
        val size: Double
    )

    private companion object {

        /*
         * Dá prioridade correta para descritores
         * de densidade, como 2x e 3x.
         */
        const val DENSITY_WEIGHT =
            10_000.0
    }
}