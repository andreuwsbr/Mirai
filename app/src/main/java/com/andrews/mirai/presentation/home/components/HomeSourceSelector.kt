package com.andrews.mirai.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.source.MangaSource

@Composable
fun HomeSourceSelector(
    sources: List<MangaSource>,
    selectedSourceId: String,
    onSourceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val selectedSource =
        sources.firstOrNull { source ->
            source.id == selectedSourceId
        } ?: sources.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    menuExpanded = true
                },
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceContainer,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 13.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Source,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Text(
                    text =
                        selectedSource?.name
                            ?: "Selecionar fonte",
                    modifier =
                        Modifier.weight(1f),
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall
                )

                Icon(
                    imageVector =
                        Icons.Outlined.ExpandMore,
                    contentDescription =
                        "Selecionar fonte"
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = {
                menuExpanded = false
            }
        ) {
            sources.forEach { source ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = source.name
                        )
                    },
                    onClick = {
                        menuExpanded = false

                        if (
                            source.id !=
                            selectedSourceId
                        ) {
                            onSourceSelected(
                                source.id
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.Source,
                            contentDescription = null,
                            tint =
                                if (
                                    source.id ==
                                    selectedSourceId
                                ) {
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
                )
            }
        }
    }
}