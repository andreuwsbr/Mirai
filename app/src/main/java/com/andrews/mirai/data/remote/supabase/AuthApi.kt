package com.andrews.mirai.data.remote.supabase

import com.andrews.mirai.data.remote.HttpClient
import com.andrews.mirai.data.remote.HttpResponse
import org.json.JSONObject

object AuthApi {

    fun register(
        email: String,
        password: String
    ): AuthResult {
        val body =
            JSONObject()
                .put(
                    "email",
                    email.trim()
                )
                .put(
                    "password",
                    password
                )
                .toString()

        val response =
            HttpClient.postJson(
                url =
                    "${SupabaseConfig.authUrl}/signup",
                jsonBody = body,
                headers =
                    SupabaseHeaders.publicHeaders()
            )

        return parseAuthResponse(
            response
        )
    }

    fun login(
        email: String,
        password: String
    ): AuthResult {
        val body =
            JSONObject()
                .put(
                    "email",
                    email.trim()
                )
                .put(
                    "password",
                    password
                )
                .toString()

        val response =
            HttpClient.postJson(
                url =
                    "${SupabaseConfig.authUrl}" +
                            "/token?grant_type=password",
                jsonBody = body,
                headers =
                    SupabaseHeaders.publicHeaders()
            )

        return parseAuthResponse(
            response
        )
    }

    fun refreshSession(
        refreshToken: String
    ): AuthResult {
        val body =
            JSONObject()
                .put(
                    "refresh_token",
                    refreshToken
                )
                .toString()

        val response =
            HttpClient.postJson(
                url =
                    "${SupabaseConfig.authUrl}" +
                            "/token?grant_type=refresh_token",
                jsonBody = body,
                headers =
                    SupabaseHeaders.publicHeaders()
            )

        return parseAuthResponse(
            response
        )
    }

    private fun parseAuthResponse(
        response: HttpResponse
    ): AuthResult {
        if (!response.isSuccessful) {
            return AuthResult.Failure(
                message =
                    extractErrorMessage(
                        response
                    ),
                statusCode =
                    response.code
                        .takeIf { code ->
                            code > 0
                        }
            )
        }

        return try {
            val json =
                JSONObject(
                    response.body
                )

            val userJson =
                json.optJSONObject(
                    "user"
                )

            val accessToken =
                json.optString(
                    "access_token"
                )

            val refreshToken =
                json.optString(
                    "refresh_token"
                )

            val expiresIn =
                json.optLong(
                    "expires_in",
                    0L
                )

            if (
                accessToken.isBlank() ||
                refreshToken.isBlank() ||
                userJson == null
            ) {
                return AuthResult.Success(
                    session = null
                )
            }

            val userId =
                userJson.optString(
                    "id"
                )

            if (userId.isBlank()) {
                return AuthResult.Failure(
                    message =
                        "O Supabase não retornou o usuário da sessão."
                )
            }

            val user =
                AuthUser(
                    id = userId,
                    email =
                        userJson.optString(
                            "email"
                        )
                )

            AuthResult.Success(
                session =
                    AuthSession(
                        accessToken =
                            accessToken,
                        refreshToken =
                            refreshToken,
                        expiresInSeconds =
                            expiresIn,
                        user = user
                    )
            )
        } catch (
            throwable: Throwable
        ) {
            AuthResult.Failure(
                message =
                    "O Supabase enviou uma resposta inválida."
            )
        }
    }

    private fun extractErrorMessage(
        response: HttpResponse
    ): String {
        if (response.code == 0) {
            return response.errorMessage
                ?: "Não foi possível conectar ao servidor."
        }

        return try {
            val json =
                JSONObject(
                    response.body
                )

            json.optString("msg")
                .ifBlank {
                    json.optString(
                        "message"
                    )
                }
                .ifBlank {
                    json.optString(
                        "error_description"
                    )
                }
                .ifBlank {
                    json.optString(
                        "error"
                    )
                }
                .ifBlank {
                    json.optString(
                        "error_code"
                    )
                }
                .ifBlank {
                    "Não foi possível concluir a operação."
                }
        } catch (
            throwable: Throwable
        ) {
            "Não foi possível concluir a operação."
        }
    }
}