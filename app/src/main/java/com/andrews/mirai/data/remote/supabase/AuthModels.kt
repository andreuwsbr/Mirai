package com.andrews.mirai.data.remote.supabase

data class AuthUser(
    val id: String,
    val email: String
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val user: AuthUser
)

sealed interface AuthResult {

    data class Success(
        val session: AuthSession?
    ) : AuthResult

    data class Failure(
        val message: String,
        val statusCode: Int? = null
    ) : AuthResult
}