package com.andrews.mirai.presentation.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.local.download.DownloadStatus
import com.andrews.mirai.domain.model.Chapter
import com.andrews.mirai.presentation.details.components.ChapterListControls

@Composable
internal fun DetailsChapterContent(
    chapterQuery: String,
    searchExpanded: Boolean,
    descendingOrder: Boolean,
    normalizedQuery: String,
    chaptersCount: Int,
    filteredChaptersCount: Int,
    chaptersLoading: Boolean,
    chaptersError: String?,
    onQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onToggleOrder: () -> Unit
) {
    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            bottom = 20.dp
        )
    ) {
        Text(
            text = "Capítulos",
            style =
                MaterialTheme
                    .typography
                    .titleLarge
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        ChapterListControls(
            query = chapterQuery,
            searchExpanded = searchExpanded,
            descendingOrder = descendingOrder,
            onQueryChange = onQueryChange,
            onSearchExpandedChange =
                onSearchExpandedChange,
            onToggleOrder = onToggleOrder
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                if (descendingOrder) {
                    "Ordem: mais recente primeiro"
                } else {
                    "Ordem: mais antigo primeiro"
                },
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        if (
            !chaptersLoading &&
            chaptersCount > 0
        ) {
            Text(
                text =
                    if (
                        normalizedQuery
                            .isNotBlank()
                    ) {
                        "$filteredChaptersCount resultado(s)"
                    } else {
                        "$chaptersCount capítulos encontrados"
                    },
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (chaptersLoading) {
            CircularProgressIndicator()
        }

        if (chaptersError != null) {
            Text(
                text = chaptersError,
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }

        if (
            !chaptersLoading &&
            chaptersError == null &&
            chaptersCount == 0
        ) {
            Text(
                text =
                    "Nenhum capítulo foi encontrado.",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        if (
            !chaptersLoading &&
            chaptersCount > 0 &&
            filteredChaptersCount == 0
        ) {
            Text(
                text =
                    "Nenhum capítulo corresponde à pesquisa.",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ChapterItem(
    chapter: Chapter,
    isViewed: Boolean,
    downloadStatus: DownloadStatus?,
    downloadProgress: Int,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(
                if (isViewed) {
                    0.70f
                } else {
                    1f
                }
            )
            .padding(
                horizontal = 20.dp,
                vertical = 5.dp
            )
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(14.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    end = 8.dp,
                    bottom = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.name,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                if (
                    chapter.uploadedAt
                        .isNotBlank()
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            chapter.uploadedAt,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                if (isViewed) {
                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text = "Visto",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                DownloadStatusText(
                    status =
                        downloadStatus,
                    progress =
                        downloadProgress
                )
            }

            ChapterDownloadButton(
                status =
                    downloadStatus,
                onClick =
                    onDownloadClick
            )
        }
    }
}

@Composable
private fun DownloadStatusText(
    status: DownloadStatus?,
    progress: Int
) {
    val text =
        when (status) {
            DownloadStatus.QUEUED ->
                "Aguardando download • toque para cancelar"

            DownloadStatus.DOWNLOADING ->
                "Baixando: ${
                    progress.coerceIn(
                        0,
                        100
                    )
                }% • toque para cancelar"

            DownloadStatus.COMPLETED ->
                "Disponível offline"

            DownloadStatus.FAILED ->
                "Falha no download"

            DownloadStatus.PAUSED ->
                "Download pausado"

            null ->
                null
        }

    if (text != null) {
        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text = text,
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            color =
                when (status) {
                    DownloadStatus.FAILED ->
                        MaterialTheme
                            .colorScheme
                            .error

                    else ->
                        MaterialTheme
                            .colorScheme
                            .primary
                }
        )
    }
}

@Composable
private fun ChapterDownloadButton(
    status: DownloadStatus?,
    onClick: () -> Unit
) {
    val enabled =
        status !=
                DownloadStatus.COMPLETED

    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        when (status) {
            DownloadStatus.QUEUED,
            DownloadStatus.DOWNLOADING -> {
                Icon(
                    imageVector =
                        Icons.Outlined.Cancel,
                    contentDescription =
                        "Cancelar download",
                    tint =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }

            DownloadStatus.COMPLETED -> {
                Icon(
                    imageVector =
                        Icons.Outlined.CheckCircle,
                    contentDescription =
                        "Capítulo disponível offline",
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            DownloadStatus.FAILED -> {
                Icon(
                    imageVector =
                        Icons.Outlined.ErrorOutline,
                    contentDescription =
                        "Tentar baixar novamente",
                    tint =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }

            DownloadStatus.PAUSED -> {
                Icon(
                    imageVector =
                        Icons.Outlined.PauseCircleOutline,
                    contentDescription =
                        "Continuar download"
                )
            }

            null -> {
                Icon(
                    imageVector =
                        Icons.Outlined.Download,
                    contentDescription =
                        "Baixar capítulo"
                )
            }
        }
    }
}