package com.andrews.mirai.presentation.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.andrews.mirai.domain.model.Chapter

@Composable
fun ReaderChapterTransitionContent(
    completedChapter: Chapter,
    nextChapter: Chapter,
    backgroundColor: Color
) {
    val contentColor =
        if (
            backgroundColor.luminance() >
            0.5f
        ) {
            Color.Black
        } else {
            Color.White
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 300.dp
                )
                .background(
                    backgroundColor
                )
                .padding(
                    horizontal = 40.dp,
                    vertical = 48.dp
                ),
        verticalArrangement =
            Arrangement.Center
    ) {
        Icon(
            imageVector =
                Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint =
                contentColor
        )

        Text(
            text =
                "Capítulo concluído",
            modifier =
                Modifier.padding(
                    top = 12.dp
                ),
            color =
                contentColor,
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        Text(
            text =
                completedChapter.name,
            modifier =
                Modifier.padding(
                    top = 6.dp
                ),
            color =
                contentColor,
            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Icon(
            imageVector =
                Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            modifier =
                Modifier.padding(
                    top = 30.dp
                ),
            tint =
                contentColor
        )

        Text(
            text =
                "Próximo capítulo",
            modifier =
                Modifier.padding(
                    top = 10.dp
                ),
            color =
                contentColor.copy(
                    alpha = 0.75f
                ),
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        Text(
            text =
                nextChapter.name,
            modifier =
                Modifier.padding(
                    top = 6.dp
                ),
            color =
                contentColor,
            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )
    }
}