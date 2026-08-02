package com.andrews.mirai.data.remote.supabase

object SupabaseHeaders {

    fun publicHeaders(): Map<String, String> {
        return mapOf(
            "apikey" to
                    SupabaseConfig.publishableKey,
            "Content-Type" to
                    "application/json"
        )
    }

    fun authenticatedHeaders(
        accessToken: String
    ): Map<String, String> {
        return mapOf(
            "apikey" to
                    SupabaseConfig.publishableKey,
            "Authorization" to
                    "Bearer $accessToken",
            "Content-Type" to
                    "application/json"
        )
    }
}