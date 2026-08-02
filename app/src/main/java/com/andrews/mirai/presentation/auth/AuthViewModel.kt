package com.andrews.mirai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrews.mirai.data.remote.supabase.AuthResult
import com.andrews.mirai.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AuthUiState(
                currentUser =
                    AuthRepository
                        .getCurrentSession()
                        ?.user
            )
        )

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        if (!validateCredentials(email, password)) {
            return
        }

        executeAuthOperation {
            AuthRepository.login(
                email = email.trim(),
                password = password
            )
        }
    }

    fun register(
        email: String,
        password: String
    ) {
        if (!validateCredentials(email, password)) {
            return
        }

        executeAuthOperation {
            AuthRepository.register(
                email = email.trim(),
                password = password
            )
        }
    }

    fun logout() {
        AuthRepository.logout()

        _uiState.value =
            AuthUiState(
                message =
                    "Você saiu da sua conta."
            )
    }

    fun clearMessage() {
        _uiState.value =
            _uiState.value.copy(
                message = null,
                isError = false,
                requiresEmailConfirmation = false
            )
    }

    private fun validateCredentials(
        email: String,
        password: String
    ): Boolean {
        if (email.isBlank()) {
            showError(
                "Digite seu e-mail."
            )

            return false
        }

        if (!email.contains("@")) {
            showError(
                "Digite um e-mail válido."
            )

            return false
        }

        if (password.length < 6) {
            showError(
                "A senha precisa ter pelo menos 6 caracteres."
            )

            return false
        }

        return true
    }

    private fun executeAuthOperation(
        operation: () -> AuthResult
    ) {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    message = null,
                    isError = false,
                    requiresEmailConfirmation = false
                )

            val result =
                withContext(
                    Dispatchers.IO
                ) {
                    operation()
                }

            handleResult(result)
        }
    }

    private fun handleResult(
        result: AuthResult
    ) {
        when (result) {
            is AuthResult.Success -> {
                val session =
                    result.session

                if (session == null) {
                    _uiState.value =
                        AuthUiState(
                            message =
                                "Conta criada. Verifique seu e-mail para confirmar o cadastro.",
                            requiresEmailConfirmation = true
                        )

                    return
                }

                _uiState.value =
                    AuthUiState(
                        currentUser =
                            session.user,
                        message =
                            "Autenticação realizada com sucesso."
                    )
            }

            is AuthResult.Failure -> {
                _uiState.value =
                    AuthUiState(
                        message =
                            translateError(
                                result.message
                            ),
                        isError = true
                    )
            }
        }
    }

    private fun showError(
        message: String
    ) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                message = message,
                isError = true
            )
    }

    private fun translateError(
        message: String
    ): String {
        val normalizedMessage =
            message.lowercase()

        return when {
            "invalid login credentials" in
                    normalizedMessage -> {
                "E-mail ou senha incorretos."
            }

            "email not confirmed" in
                    normalizedMessage -> {
                "Confirme seu e-mail antes de entrar."
            }

            "user already registered" in
                    normalizedMessage -> {
                "Já existe uma conta com este e-mail."
            }

            "password should be at least" in
                    normalizedMessage -> {
                "A senha precisa ter pelo menos 6 caracteres."
            }

            else -> message
        }
    }
}