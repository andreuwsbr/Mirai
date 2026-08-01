package com.andrews.mirai.presentation.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.source.MangaSource

@Composable
fun SourceSelector(

    sources: List<MangaSource>,

    selectedSource: MangaSource,

    onSourceSelected: (MangaSource) -> Unit

) {

    var expanded by remember {

        mutableStateOf(false)

    }

    Box {

        SourceSelectorItem(
            text = selectedSource.name,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                }
        )

        DropdownMenu(

            expanded = expanded,

            onDismissRequest = {

                expanded = false

            }

        ) {

            sources.forEach { source ->

                DropdownMenuItem(

                    text = {

                        Text(

                            text = source.name,

                            fontWeight =
                                if (source.id == selectedSource.id)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal

                        )

                    },

                    trailingIcon = {

                        if (source.id == selectedSource.id) {

                            Icon(

                                imageVector = Icons.Rounded.ArrowDropDown,

                                contentDescription = null

                            )

                        }

                    },

                    onClick = {

                        expanded = false

                        onSourceSelected(source)

                    }

                )

            }

        }

    }

}