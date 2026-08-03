package com.andrews.mirai.presentation.reader.logic

import com.andrews.mirai.domain.model.ReaderPage
import com.andrews.mirai.presentation.reader.cache.ReaderImageDownloader
import kotlinx.coroutines.CancellationException

class ReaderPreloader(
    private val imageDownloader:
    ReaderImageDownloader
) {

    suspend fun preloadCurrentPages(
        pages: List<ReaderPage>,
        currentPageIndex: Int,
        preloadDistance: Int
    ) {
        if (pages.isEmpty()) {
            return
        }

        val safeCurrentPage =
            currentPageIndex.coerceIn(
                minimumValue = 0,
                maximumValue = pages.lastIndex
            )

        val endIndex =
            (
                    safeCurrentPage +
                            preloadDistance
                    ).coerceAtMost(
                    pages.lastIndex
                )

        for (
        pageIndex in
        safeCurrentPage..endIndex
        ) {
            preloadPage(
                pages[pageIndex]
            )
        }
    }

    suspend fun preloadNextChapter(
        pages: List<ReaderPage>,
        maximumPages: Int
    ) {
        pages
            .take(
                maximumPages.coerceAtLeast(0)
            )
            .forEach { page ->
                preloadPage(page)
            }
    }

    private suspend fun preloadPage(
        page: ReaderPage
    ) {
        try {
            imageDownloader.downloadWithInfo(
                page.imageUrl
            )
        } catch (
            exception: CancellationException
        ) {
            throw exception
        } catch (_: Throwable) {
            /*
             * Uma falha no pré-carregamento não pode
             * interromper a leitura do capítulo.
             */
        }
    }
}