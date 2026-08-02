package com.andrews.mirai.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private enum class AuthMode {
    LOGIN,
    REGISTER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginClick: (
        email: String,
        password: String
    ) -> Unit,
    onRegisterClick: (
        email: String,
        password: String
    ) -> Unit,
    onBackClick: () -> Unit
) {
    var authMode by remember {
        mutableStateOf(
            AuthMode.LOGIN
        )
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (authMode) {
                            AuthMode.LOGIN -> {
                                "Entrar"
                            }

                            AuthMode.REGISTER -> {
                                "Criar conta"
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored
                                    .Outlined
                                    .ArrowBack,
                            contentDescription =
                                "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = "MIRAI",
                style =
                    MaterialTheme
                        .typography
                        .displaySmall,
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
                text = when (authMode) {
                    AuthMode.LOGIN -> {
                        "Entre para sincronizar seus dados."
                    }

                    AuthMode.REGISTER -> {
                        "Crie uma conta para salvar seu progresso."
                    }
                },
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
                    Modifier.height(32.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                if (authMode == AuthMode.LOGIN) {
                    Button(
                        onClick = {},
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Entrar"
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            authMode =
                                AuthMode.LOGIN
                        },
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Entrar"
                        )
                    }
                }

                if (authMode == AuthMode.REGISTER) {
                    Button(
                        onClick = {},
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Criar conta"
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            authMode =
                                AuthMode.REGISTER
                        },
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Criar conta"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !uiState.isLoading,
                label = {
                    Text(
                        text = "E-mail"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Email,
                        contentDescription = null
                    )
                },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email,
                        imeAction =
                            ImeAction.Next
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !uiState.isLoading,
                label = {
                    Text(
                        text = "Senha"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Lock,
                        contentDescription = null
                    )
                },
                singleLine = true,
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password,
                        imeAction =
                            ImeAction.Done
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "A senha precisa ter pelo menos 6 caracteres.",
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            val message =
                uiState.message

            if (message != null) {
                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text = message,
                    modifier =
                        Modifier.fillMaxWidth(),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        if (uiState.isError) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )

            Button(
                onClick = {
                    when (authMode) {
                        AuthMode.LOGIN -> {
                            onLoginClick(
                                email,
                                password
                            )
                        }

                        AuthMode.REGISTER -> {
                            onRegisterClick(
                                email,
                                password
                            )
                        }
                    }
                },
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when (authMode) {
                            AuthMode.LOGIN -> {
                                "Entrar"
                            }

                            AuthMode.REGISTER -> {
                                "Criar conta"
                            }
                        }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text =
                    "Você pode continuar usando o Mirai sem criar uma conta.",
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