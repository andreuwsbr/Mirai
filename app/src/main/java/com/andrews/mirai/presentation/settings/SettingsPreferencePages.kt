package com.andrews.mirai.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.andrews.mirai.data.local.AppSettingsStore
import com.andrews.mirai.data.local.AppThemeMode
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore

@Composable
internal fun AppearanceSettingsPage(
    onBackClick: () -> Unit
) {
    val selectedTheme by
    AppSettingsStore.themeMode.collectAsState()

    SettingsSubpage(
        title = "Aparência",
        onBackClick = onBackClick
    ) {
        SettingsSectionTitle("Tema do aplicativo")

        ThemeOption(
            title = "Seguir o sistema",
            subtitle = "Usa o tema definido no Android",
            selected = selectedTheme == AppThemeMode.SYSTEM,
            onClick = {
                AppSettingsStore.setThemeMode(
                    AppThemeMode.SYSTEM
                )
            }
        )

        ThemeOption(
            title = "Claro",
            subtitle = "Mantém o aplicativo sempre claro",
            selected = selectedTheme == AppThemeMode.LIGHT,
            onClick = {
                AppSettingsStore.setThemeMode(
                    AppThemeMode.LIGHT
                )
            }
        )

        ThemeOption(
            title = "Escuro",
            subtitle = "Mantém o aplicativo sempre escuro",
            selected = selectedTheme == AppThemeMode.DARK,
            onClick = {
                AppSettingsStore.setThemeMode(
                    AppThemeMode.DARK
                )
            }
        )
    }
}

@Composable
internal fun ReaderSettingsPage(
    onBackClick: () -> Unit
) {
    val context =
        LocalContext.current.applicationContext

    val store = remember(context) {
        ReaderSettingsStore(context)
    }

    var preferences by remember {
        mutableStateOf(store.load())
    }

    SettingsSubpage(
        title = "Leitor",
        onBackClick = onBackClick
    ) {
        SettingsSectionTitle("Comportamento")

        SettingsSwitchItem(
            title = "Manter tela ligada",
            subtitle = "Impede que a tela apague durante a leitura",
            checked = preferences.keepScreenOn,
            onCheckedChange = { checked ->
                preferences = preferences.copy(
                    keepScreenOn = checked
                )

                store.save(preferences)
            }
        )

        SettingsSwitchItem(
            title = "Tela cheia",
            subtitle = "Oculta as barras do sistema durante a leitura",
            checked = preferences.fullscreen,
            onCheckedChange = { checked ->
                preferences = preferences.copy(
                    fullscreen = checked
                )

                store.save(preferences)
            }
        )

        SettingsSwitchItem(
            title = "Mostrar número da página",
            subtitle = "Exibe a página atual na barra superior",
            checked = preferences.showPageNumber,
            onCheckedChange = { checked ->
                preferences = preferences.copy(
                    showPageNumber = checked
                )

                store.save(preferences)
            }
        )
    }
}