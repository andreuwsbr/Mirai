package com.andrews.mirai.data.source.astratoons.parser

import com.andrews.mirai.data.source.astratoons.AstralToonsSelectors
import com.andrews.mirai.domain.model.ReaderPage
import org.jsoup.Jsoup
import java.net.URI

class AstralPageParser(
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

        /*
         * LinkedHashSet mantém a ordem em que as imagens
         * aparecem no site e também remove duplicadas.
         */
        val imageUrls =
            linkedSetOf<String>()

        /*
         * Primeiro coletamos as imagens diretamente dos
         * elementos HTML, preservando a ordem visual do capítulo.
         */
        document
            .select(
                AstralToonsSelectors.PAGE_IMAGE
            )
            .forEach { image ->
                val rawUrl =
                    AstralToonsSelectors
                        .PAGE_IMAGE_ATTRIBUTES
                        .asSequence()
                        .map { attribute ->
                            image
                                .attr(attribute)
                                .trim()
                        }
                        .firstOrNull { value ->
                            value.contains(
                                other =
                                    AstralToonsSelectors
                                        .PAGE_STORAGE_PATH,
                                ignoreCase = true
                            )
                        }

                resolveUrl(rawUrl)
                    ?.let(imageUrls::add)
            }

        /*
         * Algumas páginas podem estar dentro de scripts.
         * Essas URLs são adicionadas apenas quando ainda
         * não foram encontradas no HTML.
         */
        extractUrlsFromHtml(html)
            .mapNotNull(::resolveUrl)
            .forEach(imageUrls::add)

        /*
         * Não ordenamos pelo nome do arquivo.
         * A ordem correta é a ordem entregue pelo site.
         */
        return imageUrls
            .mapIndexed { index, imageUrl ->
                ReaderPage(
                    index = index,
                    imageUrl = imageUrl
                )
            }
    }

    private fun extractUrlsFromHtml(
        html: String
    ): List<String> {
        return AstralToonsSelectors
            .PAGE_URL
            .findAll(html)
            .map { match ->
                match.value
                    .replace("\\/", "/")
                    .replace(
                        "\\u0026",
                        "&"
                    )
            }
            .distinct()
            .toList()
    }

    private fun resolveUrl(
        value: String?
    ): String? {
        val normalizedValue =
            value
                ?.trim()
                ?.replace("\\/", "/")
                ?.replace(
                    "\\u0026",
                    "&"
                )
                .orEmpty()

        if (
            normalizedValue.isBlank() ||
            normalizedValue.startsWith(
                prefix = "data:",
                ignoreCase = true
            )
        ) {
            return null
        }

        return runCatching {
            URI(baseUrl)
                .resolve(normalizedValue)
                .toString()
        }.getOrNull()
    }
}