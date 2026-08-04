package com.andrews.mirai.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.presentation.reader.progress.ReadingHistoryItem

@Composable
fun RecentHistorySection(
    items: List<ReadingHistoryItem>,
    coverModelProvider:
        (ReadingHistoryItem) -> Any?,
    onDetailsClick:
        (ReadingHistoryItem) -> Unit,
    onContinueClick:
        (ReadingHistoryItem) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        return
    }

    HomeSectionHeader(
        title = "Lidos recentemente",
        actionText = "Ver tudo",
        onActionClick =
            onViewAllClick
    )

    LazyRow(
        modifier =
            modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = 20.dp,
                end = 20.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        items(
            items = items,
            key = { item ->
                "${item.sourceId}|${item.mangaId}"
            }
        ) { item ->
            RecentHistoryCard(
                item = item,
                coverModel =
                    coverModelProvider(
                        item
                    ),
                onDetailsClick = {
                    onDetailsClick(
                        item
                    )
                },
                onContinueClick = {
                    onContinueClick(
                        item
                    )
                }
            )
        }
    }
}