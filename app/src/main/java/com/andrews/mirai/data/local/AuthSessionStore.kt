package com.andrews.mirai.data.local

import android.content.Context
import android.content.SharedPreferences
import com.andrews.mirai.data.remote.supabase.AuthSession
import com.andrews.mirai.data.remote.supabase.AuthUser

object AuthSessionStore {

    private const val PREFERENCES_NAME =
        "mirai_auth_session"

    private const val ACCESS_TOKEN_KEY =
        "access_token"

    private const val REFRESH_TOKEN_KEY =
        "refresh_token"

    private const val EXPIRES_IN_KEY =
        "expires_in"

    private const val SESSION_SAVED_AT_KEY =
        "session_saved_at"

    private const val USER_ID_KEY =
        "user_id"

    private const val USER_EMAIL_KEY =
        "user_email"

    private const val EXPIRATION_MARGIN_SECONDS =
        120L

    private var preferences: SharedPreferences? =
        null

    fun initialize(
        context: Context
    ) {
        preferences =
            context.applicationContext
                .getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )
    }

    fun saveSession(
        session: AuthSession
    ) {
        requireInitialized()
            .edit()
            .putString(
                ACCESS_TOKEN_KEY,
                session.accessToken
            )
            .putString(
                REFRESH_TOKEN_KEY,
                session.refreshToken
            )
            .putLong(
                EXPIRES_IN_KEY,
                session.expiresInSeconds
            )
            .putLong(
                SESSION_SAVED_AT_KEY,
                currentEpochSeconds()
            )
            .putString(
                USER_ID_KEY,
                session.user.id
            )
            .putString(
                USER_EMAIL_KEY,
                session.user.email
            )
            .apply()
    }

    fun getSession(): AuthSession? {
        val storedPreferences =
            requireInitialized()

        val accessToken =
            storedPreferences
                .getString(
                    ACCESS_TOKEN_KEY,
                    null
                )
                ?.takeIf(String::isNotBlank)
                ?: return null

        val refreshToken =
            storedPreferences
                .getString(
                    REFRESH_TOKEN_KEY,
                    null
                )
                ?.takeIf(String::isNotBlank)
                ?: return null

        val userId =
            storedPreferences
                .getString(
                    USER_ID_KEY,
                    null
                )
                ?.takeIf(String::isNotBlank)
                ?: return null

        val userEmail =
            storedPreferences
                .getString(
                    USER_EMAIL_KEY,
                    null
                )
                .orEmpty()

        val expiresInSeconds =
            storedPreferences.getLong(
                EXPIRES_IN_KEY,
                0L
            )

        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInSeconds =
                expiresInSeconds,
            user =
                AuthUser(
                    id = userId,
                    email = userEmail
                )
        )
    }

    fun isSessionExpiringSoon(): Boolean {
        val storedPreferences =
            requireInitialized()

        val expiresInSeconds =
            storedPreferences.getLong(
                EXPIRES_IN_KEY,
                0L
            )

        val savedAt =
            storedPreferences.getLong(
                SESSION_SAVED_AT_KEY,
                0L
            )

        /*
         * Sessões antigas não possuem o instante em que
         * foram salvas. Nesse caso, forçamos uma renovação.
         */
        if (
            expiresInSeconds <= 0L ||
            savedAt <= 0L
        ) {
            return true
        }

        val expirationTime =
            savedAt + expiresInSeconds

        return currentEpochSeconds() >=
                expirationTime -
                EXPIRATION_MARGIN_SECONDS
    }

    fun clearSession() {
        requireInitialized()
            .edit()
            .clear()
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getSession() != null
    }

    private fun currentEpochSeconds(): Long {
        return System.currentTimeMillis() /
                1_000L
    }

    private fun requireInitialized():
            SharedPreferences {
        return checkNotNull(preferences) {
            "AuthSessionStore não foi inicializado."
        }
    }
}