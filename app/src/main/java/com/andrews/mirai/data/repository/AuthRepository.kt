package com.andrews.mirai.data.repository

import com.andrews.mirai.data.local.AuthSessionStore
import com.andrews.mirai.data.remote.supabase.AuthApi
import com.andrews.mirai.data.remote.supabase.AuthResult
import com.andrews.mirai.data.remote.supabase.AuthSession

object AuthRepository {

    fun register(
        email: String,
        password: String
    ): AuthResult {
        val result =
            AuthApi.register(
                email = email,
                password = password
            )

        saveSessionIfAvailable(result)

        return result
    }

    fun login(
        email: String,
        password: String
    ): AuthResult {
        val result =
            AuthApi.login(
                email = email,
                password = password
            )

        saveSessionIfAvailable(result)

        return result
    }

    fun getCurrentSession(): AuthSession? {
        return AuthSessionStore.getSession()
    }

    fun isLoggedIn(): Boolean {
        return AuthSessionStore.isLoggedIn()
    }

    fun logout() {
        AuthSessionStore.clearSession()
    }

    private fun saveSessionIfAvailable(
        result: AuthResult
    ) {
        if (result !is AuthResult.Success) {
            return
        }

        val session =
            result.session
                ?: return

        AuthSessionStore.saveSession(
            session
        )
    }
}