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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
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
import com.andrews.mirai.presentation.downloads.DownloadsScreen

private const val DISCORD_URL =
    "https://discord.gg/vyp4pdHbK8"

private enum class SettingsPage {
    MAIN,
    ACCOUNT,
    APPEARANCE,
    READER,
    STORAGE,
    DOWNLOADS,
    ABOUT
}

@Composable
fun SettingsScreen(
    currentUserEmail: String?,
    onAuthenticationClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var currentPage by remember {
        mutableStateOf(
            SettingsPage.MAIN
        )
    }

    BackHandler(
        enabled =
            currentPage != SettingsPage.MAIN
    ) {
        currentPage =
            when (currentPage) {
                SettingsPage.DOWNLOADS ->
                    SettingsPage.STORAGE

                else ->
                    SettingsPage.MAIN
            }
    }

    when (currentPage) {
        SettingsPage.MAIN -> {
            MainSettingsPage(
                currentUserEmail =
                    currentUserEmail,
                onAccountClick = {
                    if (currentUserEmail == null) {
                        onAuthenticationClick()
                    } else {
                        currentPage =
                            SettingsPage.ACCOUNT
                    }
                },
                onAppearanceClick = {
                    currentPage =
                        SettingsPage.APPEARANCE
                },
                onReaderClick = {
                    currentPage =
                        SettingsPage.READER
                },
                onStorageClick = {
                    currentPage =
                        SettingsPage.STORAGE
                },
                onAboutClick = {
                    currentPage =
                        SettingsPage.ABOUT
                }
            )
        }

        SettingsPage.ACCOUNT -> {
            AccountSettingsPage(
                email =
                    currentUserEmail.orEmpty(),
                onLogoutClick = {
                    onLogoutClick()

                    currentPage =
                        SettingsPage.MAIN
                },
                onBackClick = {
                    currentPage =
                        SettingsPage.MAIN
                }
            )
        }

        SettingsPage.APPEARANCE -> {
            AppearanceSettingsPage(
                onBackClick = {
                    currentPage =
                        SettingsPage.MAIN
                }
            )
        }

        SettingsPage.READER -> {
            ReaderSettingsPage(
                onBackClick = {
                    currentPage =
                        SettingsPage.MAIN
                }
            )
        }

        SettingsPage.STORAGE -> {
            StorageSettingsPage(
                onBackClick = {
                    currentPage =
                        SettingsPage.MAIN
                },
                onManageDownloadsClick = {
                    currentPage =
                        SettingsPage.DOWNLOADS
                }
            )
        }

        SettingsPage.DOWNLOADS -> {
            DownloadsScreen(
                onBackClick = {
                    currentPage =
                        SettingsPage.STORAGE
                }
            )
        }

        SettingsPage.ABOUT -> {
            AboutSettingsPage(
                onBackClick = {
                    currentPage =
                        SettingsPage.MAIN
                }
            )
        }
    }
}

@Composable
private fun MainSettingsPage(
    currentUserEmail: String?,
    onAccountClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onReaderClick: () -> Unit,
    onStorageClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val context =
        LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiraiHeader(
            title = "Ajustes",
            subtitle =
                "Personalize sua experiência"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsSectionTitle(
                    title = "Conta"
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined
                                    .AccountCircle,
                            contentDescription = null
                        )
                    },
                    title =
                        if (currentUserEmail == null) {
                            "Entrar ou criar conta"
                        } else {
                            "Minha conta"
                        },
                    subtitle =
                        currentUserEmail
                            ?: "Sincronize favoritos, histórico e progresso",
                    onClick =
                        onAccountClick
                )
            }

            item {
                SettingsSectionTitle(
                    title = "Aplicativo"
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.DarkMode,
                            contentDescription = null
                        )
                    },
                    title = "Aparência",
                    subtitle =
                        "Tema claro, escuro ou do sistema",
                    onClick =
                        onAppearanceClick
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.AutoStories,
                            contentDescription = null
                        )
                    },
                    title = "Leitor",
                    subtitle =
                        "Tela ligada, tela cheia e páginas",
                    onClick =
                        onReaderClick
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.Storage,
                            contentDescription = null
                        )
                    },
                    title = "Armazenamento",
                    subtitle =
                        "Cache, downloads e histórico",
                    onClick =
                        onStorageClick
                )
            }

            item {
                SettingsSectionTitle(
                    title = "Comunidade"
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.Forum,
                            contentDescription = null
                        )
                    },
                    title = "Discord oficial",
                    subtitle =
                        "Comunidade, suporte, sugestões e bugs",
                    onClick = {
                        openDiscord(
                            context
                        )
                    }
                )
            }

            item {
                SettingsSectionTitle(
                    title = "Mirai"
                )

                SettingsNavigationItem(
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    title = "Sobre",
                    subtitle =
                        "Versão ${BuildConfig.VERSION_NAME}",
                    onClick =
                        onAboutClick
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountSettingsPage(
    email: String,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    SettingsSubpage(
        title = "Minha conta",
        onBackClick = onBackClick
    ) {
        Column(
            modifier =
                Modifier.padding(24.dp)
        ) {
            Text(
                text = "Conta conectada",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = email,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            SettingsActionItem(
                icon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Logout,
                        contentDescription = null
                    )
                },
                title = "Sair da conta",
                subtitle =
                    "Os dados locais continuarão no dispositivo",
                onClick =
                    onLogoutClick
            )
        }
    }
}

@Composable
private fun AboutSettingsPage(
    onBackClick: () -> Unit
) {
    val context =
        LocalContext.current

    SettingsSubpage(
        title = "Sobre",
        onBackClick = onBackClick
    ) {
        Column(
            modifier =
                Modifier.padding(24.dp)
        ) {
            Text(
                text = "未来",
                style =
                    MaterialTheme
                        .typography
                        .displayMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = "Mirai",
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium
            )

            Text(
                text = "Futuro",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text =
                    "Versão ${BuildConfig.VERSION_NAME}",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Leitor de mangás desenvolvido para oferecer " +
                            "uma experiência simples, rápida e moderna.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text = "Desenvolvido por",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = "Andreuws",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )

            SettingsActionItem(
                icon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Forum,
                        contentDescription = null
                    )
                },
                title = "Discord oficial",
                subtitle =
                    "Comunidade, suporte e sugestões",
                onClick = {
                    openDiscord(
                        context
                    )
                }
            )
        }
    }
}

private fun openDiscord(
    context: Context
) {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(DISCORD_URL)
        )

    runCatching {
        context.startActivity(
            intent
        )
    }.onFailure {
        Toast.makeText(
            context,
            "Não foi possível abrir o Discord.",
            Toast.LENGTH_SHORT
        ).show()
    }
}