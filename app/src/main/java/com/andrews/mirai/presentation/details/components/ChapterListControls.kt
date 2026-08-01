package com.andrews.mirai.presentation.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChapterListControls(
    query: String,
    searchExpanded: Boolean,
    descendingOrder: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onToggleOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (searchExpanded) {
                    onQueryChange("")
                    onSearchExpandedChange(false)
                } else {
                    onSearchExpandedChange(true)
                }
            }
        ) {
            Icon(
                imageVector = if (searchExpanded) {
                    Icons.Outlined.Close
                } else {
                    Icons.Outlined.Search
                },
                contentDescription = if (searchExpanded) {
                    "Fechar pesquisa"
                } else {
                    "Pesquisar capítulo"
                }
            )
        }

        AnimatedVisibility(
            visible = searchExpanded,
            enter = fadeIn() +
                    expandHorizontally(
                        expandFrom = Alignment.Start
                    ),
            exit = fadeOut() +
                    shrinkHorizontally(
                        shrinkTowards = Alignment.Start
                    ),
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Número ou nome")
                }
            )
        }

        if (!searchExpanded) {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.weight(1f)
            )
        }

        IconButton(
            onClick = onToggleOrder
        ) {
            Icon(
                imageVector = Icons.Outlined.SwapVert,
                contentDescription = if (descendingOrder) {
                    "Mostrar capítulos antigos primeiro"
                } else {
                    "Mostrar capítulos recentes primeiro"
                }
            )
        }
    }
}