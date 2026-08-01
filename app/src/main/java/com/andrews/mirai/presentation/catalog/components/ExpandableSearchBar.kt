package com.andrews.mirai.presentation.catalog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {

        AnimatedVisibility(expanded) {

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text("Pesquisar...")
                }
            )
        }

        IconButton(
            onClick = {

                if (expanded && value.isNotBlank()) {

                    onValueChange("")

                } else {

                    expanded = !expanded

                }
            }
        ) {

            Icon(
                imageVector =
                    if (expanded)
                        Icons.Default.Close
                    else
                        Icons.Default.Search,
                contentDescription = null
            )
        }
    }
}