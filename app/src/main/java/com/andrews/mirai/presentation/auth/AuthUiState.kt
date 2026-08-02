package com.andrews.mirai.presentation.auth

import com.andrews.mirai.data.remote.supabase.AuthUser

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val message: String? = null,
    val isError: Boolean = false,
    val requiresEmailConfirmation: Boolean = false
)