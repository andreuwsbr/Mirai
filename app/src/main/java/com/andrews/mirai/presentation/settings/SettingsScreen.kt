package com.andrews.mirai.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andrews.mirai.data.local.AppSettingsStore
import com.andrews.mirai.data.local.AppThemeMode
import com.andrews.mirai.presentation.components.MiraiHeader
import com.andrews.mirai.presentation.reader.progress.ReadingProgressStore
import com.andrews.mirai.presentation.reader.settings.ReaderSettingsStore
import java.io.File
import java.util.Locale

private const val DISCORD_URL =
    "https://discord.gg/vyp4pdHbK8"

private enum class SettingsPage {
    MAIN,
    APPEARANCE,
    READER,
    STORAGE,
    ABOUT
}

@Composable
fun SettingsScreen() {
    var currentPage by remember {
        mutableStateOf(SettingsPage.MAIN)
    }

    BackHandler(
        enabled = currentPage != SettingsPage.MAIN
    ) {
        currentPage = SettingsPage.MAIN
    }

    when (currentPage) {
        SettingsPage.MAIN -> {
            MainSettingsPage(
                onAppearanceClick = {
                    currentPage = SettingsPage.APPEARANCE
                },
                onReaderClick = {
                    currentPage = SettingsPage.READER
                },
                onStorageClick = {
                    currentPage = SettingsPage.STORAGE
                },
                onAboutClick = {
                    currentPage = SettingsPage.ABOUT
                }
            )
        }

        SettingsPage.APPEARANCE -> {
            AppearanceSettingsPage(
                onBackClick = {
                    currentPage = SettingsPage.MAIN
                }
            )
        }

        SettingsPage.READER -> {
            ReaderSettingsPage(
                onBackClick = {
                    currentPage = SettingsPage.MAIN
                }
            )
        }

        SettingsPage.STORAGE -> {
            StorageSettingsPage(
                onBackClick = {
                    currentPage = SettingsPage.MAIN
                }
            )
        }

        SettingsPage.ABOUT -> {
            AboutSettingsPage(
                onBackClick = {
                    currentPage = SettingsPage.MAIN
                }
            )
        }
    }
}

@Composable
private fun MainSettingsPage(
    onAppearanceClick: () -> Unit,
    onReaderClick: () -> Unit,
    onStorageClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Ajustes",
            subtitle = "Personalize sua experiência"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsSectionTitle("Aplicativo")

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null
                        )
                    },
                    title = "Aparência",
                    subtitle = "Tema claro, escuro ou do sistema",
                    onClick = onAppearanceClick
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = null
                        )
                    },
                    title = "Leitor",
                    subtitle = "Tela ligada, tela cheia e páginas",
                    onClick = onReaderClick
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Storage,
                            contentDescription = null
                        )
                    },
                    title = "Armazenamento",
                    subtitle = "Cache e histórico de leitura",
                    onClick = onStorageClick
                )
            }

            item {
                SettingsSectionTitle("Comunidade")

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = null
                        )
                    },
                    title = "Discord oficial",
                    subtitle = "Comunidade, suporte, sugestões e bugs",
                    onClick = {
                        openDiscord(context)
                    }
                )
            }

            item {
                SettingsSectionTitle("Mirai")

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    title = "Sobre",
                    subtitle = "Versão 0.1.0",
                    onClick = onAboutClick
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsPage(
    onBackClick: () -> Unit
) {
    val selectedTheme =
        AppSettingsStore.themeMode.value

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
private fun ReaderSettingsPage(
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

@Composable
private fun StorageSettingsPage(
    onBackClick: () -> Unit
) {
    val context =
        LocalContext.current.applicationContext

    val progressStore = remember(context) {
        ReadingProgressStore(context)
    }

    var cacheSize by remember {
        mutableStateOf(
            calculateCacheSize(context)
        )
    }

    var showCacheDialog by remember {
        mutableStateOf(false)
    }

    var showHistoryDialog by remember {
        mutableStateOf(false)
    }

    SettingsSubpage(
        title = "Armazenamento",
        onBackClick = onBackClick
    ) {
        SettingsSectionTitle("Cache")

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title = "Limpar cache",
            subtitle = "Espaço utilizado: $cacheSize",
            onClick = {
                showCacheDialog = true
            }
        )

        SettingsSectionTitle("Histórico")

        SettingsActionItem(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null
                )
            },
            title = "Limpar histórico de leitura",
            subtitle = "Remove a lista de capítulos acessados",
            onClick = {
                showHistoryDialog = true
            }
        )
    }

    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = {
                showCacheDialog = false
            },
            title = {
                Text("Limpar cache?")
            },
            text = {
                Text(
                    "As imagens temporárias serão removidas. " +
                            "Seus favoritos e seu histórico não serão apagados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearApplicationCache(context)

                        cacheSize =
                            calculateCacheSize(context)

                        showCacheDialog = false

                        Toast.makeText(
                            context,
                            "Cache limpo com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCacheDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showHistoryDialog = false
            },
            title = {
                Text("Limpar histórico?")
            },
            text = {
                Text(
                    "A lista de leituras recentes será removida. " +
                            "Os favoritos não serão afetados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressStore.clearHistory()

                        showHistoryDialog = false

                        Toast.makeText(
                            context,
                            "Histórico removido.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showHistoryDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AboutSettingsPage(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    SettingsSubpage(
        title = "Sobre",
        onBackClick = onBackClick
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "未来",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Mirai",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Futuro",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Versão 0.1.0",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Leitor de mangás desenvolvido para oferecer " +
                        "uma experiência simples, rápida e moderna.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Desenvolvido por",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Andreuws",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(
                modifier = Modifier.height(28.dp)
            )

            SettingsActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Forum,
                        contentDescription = null
                    )
                },
                title = "Discord oficial",
                subtitle = "Comunidade, suporte e sugestões",
                onClick = {
                    openDiscord(context)
                }
            )
        }
    }
}

@Composable
private fun SettingsSubpage(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 8.dp
        )
    )
}

@Composable
private fun SettingsNavigationItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp)
    )
}

@Composable
private fun SettingsActionItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openDiscord(
    context: Context
) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(DISCORD_URL)
    )

    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            "Não foi possível abrir o Discord.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun calculateCacheSize(
    context: Context
): String {
    return formatBytes(
        directorySize(context.cacheDir)
    )
}

private fun directorySize(
    file: File?
): Long {
    if (file == null || !file.exists()) {
        return 0L
    }

    if (file.isFile) {
        return file.length()
    }

    return file.listFiles()
        ?.sumOf { child ->
            directorySize(child)
        }
        ?: 0L
}

private fun clearApplicationCache(
    context: Context
) {
    context.cacheDir
        .listFiles()
        ?.forEach { file ->
            runCatching {
                file.deleteRecursively()
            }
        }
}

private fun formatBytes(
    bytes: Long
): String {
    if (bytes <= 0L) {
        return "0 B"
    }

    val units = listOf(
        "B",
        "KB",
        "MB",
        "GB"
    )

    var value = bytes.toDouble()
    var unitIndex = 0

    while (
        value >= 1024.0 &&
        unitIndex < units.lastIndex
    ) {
        value /= 1024.0
        unitIndex++
    }

    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        value,
        units[unitIndex]
    )
}