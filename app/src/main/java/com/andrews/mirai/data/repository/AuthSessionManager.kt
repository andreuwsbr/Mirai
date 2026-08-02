package com.andrews.mirai.data.repository

import android.util.Log
import com.andrews.mirai.data.local.AuthSessionStore
import com.andrews.mirai.data.remote.supabase.AuthApi
import com.andrews.mirai.data.remote.supabase.AuthResult
import com.andrews.mirai.data.remote.supabase.AuthSession

object AuthSessionManager {

    private const val LOG_TAG =
        "MiraiAuthSession"

    private val refreshLock =
        Any()

    fun getValidSession():
            SessionResult {
        val currentSession =
            AuthSessionStore.getSession()
                ?: return SessionResult
                    .NotAuthenticated

        if (
            !AuthSessionStore
                .isSessionExpiringSoon()
        ) {
            return SessionResult.Success(
                currentSession
            )
        }

        /*
         * Impede que favoritos, histórico e progresso
         * tentem renovar o mesmo token ao mesmo tempo.
         */
        synchronized(refreshLock) {
            val latestSession =
                AuthSessionStore.getSession()
                    ?: return SessionResult
                        .NotAuthenticated

            /*
             * Outra chamada pode ter renovado a sessão
             * enquanto esta aguardava o bloqueio.
             */
            if (
                !AuthSessionStore
                    .isSessionExpiringSoon()
            ) {
                return SessionResult.Success(
                    latestSession
                )
            }

            return refreshSession(
                latestSession
            )
        }
    }

    private fun refreshSession(
        currentSession: AuthSession
    ): SessionResult {
        Log.d(
            LOG_TAG,
            "Renovando sessão do Supabase."
        )

        return when (
            val result =
                AuthApi.refreshSession(
                    currentSession.refreshToken
                )
        ) {
            is AuthResult.Success -> {
                val refreshedSession =
                    result.session

                if (refreshedSession == null) {
                    SessionResult.Failure(
                        message =
                            "O Supabase não retornou a sessão renovada."
                    )
                } else {
                    /*
                     * Salva imediatamente o novo access token
                     * e o novo refresh token rotacionado.
                     */
                    AuthSessionStore.saveSession(
                        refreshedSession
                    )

                    Log.d(
                        LOG_TAG,
                        "Sessão renovada com sucesso."
                    )

                    SessionResult.Success(
                        refreshedSession
                    )
                }
            }

            is AuthResult.Failure -> {
                val invalidSession =
                    result.statusCode == 400 ||
                            result.statusCode == 401

                if (invalidSession) {
                    AuthSessionStore.clearSession()

                    Log.e(
                        LOG_TAG,
                        "Refresh token inválido. " +
                                "Sessão local removida."
                    )

                    SessionResult
                        .NotAuthenticated
                } else {
                    Log.e(
                        LOG_TAG,
                        "Falha temporária ao renovar sessão: " +
                                result.message
                    )

                    SessionResult.Failure(
                        message = result.message
                    )
                }
            }
        }
    }
}

sealed interface SessionResult {

    data class Success(
        val session: AuthSession
    ) : SessionResult

    data class Failure(
        val message: String
    ) : SessionResult

    data object NotAuthenticated :
        SessionResult
}