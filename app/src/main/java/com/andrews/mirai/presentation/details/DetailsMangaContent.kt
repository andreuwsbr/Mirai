package com.andrews.mirai.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.andrews.mirai.domain.model.Manga
import com.andrews.mirai.presentation.components.createMiraiImageModel

@Composable
internal fun DetailsTopBar(
    manga: Manga,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Outlined
                        .ArrowBack,
                contentDescription =
                    "Voltar"
            )
        }

        IconButton(
            onClick = onFavoriteClick
        ) {
            Icon(
                imageVector =
                    if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined
                            .FavoriteBorder
                    },
                contentDescription =
                    if (isFavorite) {
                        "Remover dos favoritos"
                    } else {
                        "Adicionar aos favoritos"
                    },
                tint =
                    if (isFavorite) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}

@Composable
internal fun MangaDetailsContent(
    manga: Manga,
    detailsLoading: Boolean,
    detailsError: String?
) {
    val context =
        LocalContext.current

    val resolvedCoverModel =
        createMiraiImageModel(
            context = context,
            model = manga.coverUrl
        )

    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp
        )
    ) {
        if (detailsLoading) {
            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }

        if (detailsError != null) {
            Text(
                text =
                    "Não foi possível carregar " +
                            "todos os detalhes.\n" +
                            detailsError,
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.Start
        ) {
            AsyncImage(
                model =
                    resolvedCoverModel,
                contentDescription =
                    "Capa de ${manga.title}",
                contentScale =
                    ContentScale.Crop,
                modifier = Modifier
                    .width(140.dp)
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = manga.title,
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text =
                        "Tipo: " +
                                manga.type
                                    .displayName,
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Autor: ${manga.author}",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Status: " +
                                manga.status
                                    .displayName,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text = "Sinopse",
            style =
                MaterialTheme
                    .typography
                    .titleLarge
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                manga.description.ifBlank {
                    "A sinopse não foi encontrada."
                },
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )

        if (manga.genres.isNotEmpty()) {
            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = "Gêneros",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    manga.genres.joinToString(
                        separator = ", "
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        HorizontalDivider()

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )
    }
}