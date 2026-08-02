package com.andrews.mirai.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andrews.mirai.BuildConfig
import com.andrews.mirai.presentation.components.MiraiHeader

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
                    subtitle =
                        "Versão ${BuildConfig.VERSION_NAME}",
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
                text = "Versão ${BuildConfig.VERSION_NAME}",
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