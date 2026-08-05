package com.andrews.mirai.presentation.reader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrews.mirai.presentation.reader.display.ReaderOrientationMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    preferences: ReaderPreferences,
    onPreferencesChange: (ReaderPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest =
            onDismiss
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp
                    )
                    .padding(
                        bottom = 40.dp
                    )
        ) {
            SettingsTitle(
                text = "Modo de leitura"
            )

            ReaderModeChips(
                selectedMode =
                    preferences.mode,
                onModeSelected = { mode ->
                    onPreferencesChange(
                        preferences.copy(
                            mode = mode
                        )
                    )
                }
            )

            SettingsDivider()

            SettingsTitle(
                text = "Navegação"
            )

            ReaderTapModeChips(
                selectedMode =
                    preferences.tapMode,
                onModeSelected = { tapMode ->
                    onPreferencesChange(
                        preferences.copy(
                            tapMode = tapMode
                        )
                    )
                }
            )

            if (
                preferences.tapMode ==
                ReaderTapMode.TAP_AND_SWIPE
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Text(
                    text =
                        "Tamanho das zonas de toque",
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                FlowRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    ReaderTapZoneSize
                        .entries
                        .forEach {
                                zoneSize ->

                            FilterChip(
                                selected =
                                    preferences
                                        .tapZoneSize ==
                                            zoneSize,
                                onClick = {
                                    onPreferencesChange(
                                        preferences.copy(
                                            tapZoneSize =
                                                zoneSize
                                        )
                                    )
                                },
                                label = {
                                    Text(
                                        text =
                                            zoneSize.label
                                    )
                                }
                            )
                        }
                }
            }

            SettingsDivider()

            SettingsTitle(
                text = "Tela"
            )

            Text(
                text = "Orientação",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                ReaderOrientationMode
                    .entries
                    .forEach {
                            orientationMode ->

                        FilterChip(
                            selected =
                                preferences
                                    .orientationMode ==
                                        orientationMode,
                            onClick = {
                                onPreferencesChange(
                                    preferences.copy(
                                        orientationMode =
                                            orientationMode
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text =
                                        orientationMode.label
                                )
                            }
                        )
                    }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            ReaderBrightnessSetting(
                brightnessPercent =
                    preferences
                        .brightnessPercent,
                onBrightnessChange = {
                        brightnessPercent ->

                    onPreferencesChange(
                        preferences.copy(
                            brightnessPercent =
                                brightnessPercent
                        )
                    )
                }
            )

            SettingSwitch(
                label =
                    "Tela cheia",
                checked =
                    preferences.fullscreen,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            fullscreen = checked
                        )
                    )
                }
            )

            SettingSwitch(
                label =
                    "Manter a tela ligada",
                checked =
                    preferences.keepScreenOn,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            keepScreenOn =
                                checked
                        )
                    )
                }
            )

            SettingSwitch(
                label =
                    "Mostrar o número da página",
                checked =
                    preferences.showPageNumber,
                onCheckedChange = { checked ->
                    onPreferencesChange(
                        preferences.copy(
                            showPageNumber =
                                checked
                        )
                    )
                }
            )

            SettingsDivider()

            SettingsTitle(
                text = "Aparência"
            )

            Text(
                text = "Cor de fundo",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                ReaderBackground
                    .entries
                    .forEach {
                            background ->

                        FilterChip(
                            selected =
                                preferences
                                    .background ==
                                        background,
                            onClick = {
                                onPreferencesChange(
                                    preferences.copy(
                                        background =
                                            background
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text =
                                        background.label
                                )
                            }
                        )
                    }
            }

            if (
                preferences.mode ==
                ReaderMode.LONG_STRIP_GAPS
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                LongStripGapSetting(
                    gapDp =
                        preferences
                            .longStripGapDp,
                    onGapChange = { gapDp ->
                        onPreferencesChange(
                            preferences.copy(
                                longStripGapDp =
                                    gapDp
                            )
                        )
                    }
                )
            }

            SettingsDivider()

            SettingsTitle(
                text = "Desempenho"
            )

            Text(
                text = "Pré-carregamento",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                ReaderPreloadMode
                    .entries
                    .forEach {
                            preloadMode ->

                        FilterChip(
                            selected =
                                preferences
                                    .preloadMode ==
                                        preloadMode,
                            onClick = {
                                onPreferencesChange(
                                    preferences.copy(
                                        preloadMode =
                                            preloadMode
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text =
                                        preloadMode.label
                                )
                            }
                        )
                    }
            }
        }
    }
}

@Composable
private fun ReaderModeChips(
    selectedMode: ReaderMode,
    onModeSelected: (ReaderMode) -> Unit
) {
    Spacer(
        modifier =
            Modifier.height(
                12.dp
            )
    )

    FlowRow(
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        ReaderMode
            .entries
            .forEach { mode ->
                FilterChip(
                    selected =
                        selectedMode == mode,
                    onClick = {
                        onModeSelected(
                            mode
                        )
                    },
                    label = {
                        Text(
                            text = mode.label
                        )
                    }
                )
            }
    }
}

@Composable
private fun ReaderTapModeChips(
    selectedMode: ReaderTapMode,
    onModeSelected: (ReaderTapMode) -> Unit
) {
    Spacer(
        modifier =
            Modifier.height(
                8.dp
            )
    )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        ReaderTapMode
            .entries
            .forEach { tapMode ->
                Column {
                    FilterChip(
                        selected =
                            selectedMode ==
                                    tapMode,
                        onClick = {
                            onModeSelected(
                                tapMode
                            )
                        },
                        label = {
                            Text(
                                text =
                                    tapMode.label
                            )
                        }
                    )

                    Text(
                        text =
                            tapMode.description,
                        modifier =
                            Modifier.padding(
                                start = 4.dp,
                                top = 4.dp
                            ),
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
            }
    }
}

@Composable
private fun ReaderBrightnessSetting(
    brightnessPercent: Int,
    onBrightnessChange: (Int) -> Unit
) {
    val brightnessLabel =
        if (brightnessPercent == 0) {
            "Brilho do sistema"
        } else {
            "Brilho do leitor: $brightnessPercent%"
        }

    Text(
        text =
            brightnessLabel,
        style =
            MaterialTheme
                .typography
                .titleSmall
    )

    Slider(
        value =
            brightnessPercent
                .toFloat(),
        onValueChange = { value ->
            onBrightnessChange(
                value
                    .roundToInt()
                    .coerceIn(
                        0,
                        100
                    )
            )
        },
        valueRange =
            0f..100f,
        steps =
            19,
        modifier =
            Modifier.fillMaxWidth()
    )

    Text(
        text =
            "Em 0%, o Mirai usa o brilho configurado no aparelho.",
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

@Composable
private fun LongStripGapSetting(
    gapDp: Int,
    onGapChange: (Int) -> Unit
) {
    Text(
        text =
            "Espaçamento entre páginas: ${gapDp}dp",
        style =
            MaterialTheme
                .typography
                .titleSmall
    )

    Slider(
        value =
            gapDp.toFloat(),
        onValueChange = { value ->
            onGapChange(
                value
                    .roundToInt()
                    .coerceIn(
                        0,
                        40
                    )
            )
        },
        valueRange =
            0f..40f,
        steps =
            39,
        modifier =
            Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsTitle(
    text: String
) {
    Text(
        text = text,
        style =
            MaterialTheme
                .typography
                .titleLarge
    )
}

@Composable
private fun SettingsDivider() {
    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )

    HorizontalDivider()

    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier =
                Modifier.weight(
                    1f
                )
        )

        Switch(
            checked = checked,
            onCheckedChange =
                onCheckedChange
        )
    }
}