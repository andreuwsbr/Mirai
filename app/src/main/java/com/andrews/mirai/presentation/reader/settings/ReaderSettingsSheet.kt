package com.andrews.mirai.presentation.reader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    preferences: ReaderPreferences,
    onPreferencesChange: (ReaderPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Modo de leitura",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderMode.entries.forEach { mode ->
                    FilterChip(
                        selected = preferences.mode == mode,
                        onClick = {
                            onPreferencesChange(
                                preferences.copy(
                                    mode = mode
                                )
                            )
                        },
                        label = {
                            Text(mode.label)
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Cor de fundo",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderBackground.entries.forEach { background ->
                    FilterChip(
                        selected =
                            preferences.background == background,
                        onClick = {
                            onPreferencesChange(
                                preferences.copy(
                                    background = background
                                )
                            )
                        },
                        label = {
                            Text(background.label)
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            SettingSwitch(
                label = "Mostrar o número da página",
                checked = preferences.showPageNumber,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            showPageNumber = checked
                        )
                    )
                }
            )

            SettingSwitch(
                label = "Tela cheia",
                checked = preferences.fullscreen,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            fullscreen = checked
                        )
                    )
                }
            )

            SettingSwitch(
                label = "Manter a tela ligada",
                checked = preferences.keepScreenOn,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            keepScreenOn = checked
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}