package com.andrews.mirai.data.remote.supabase

import com.andrews.mirai.BuildConfig

object SupabaseConfig {

    val projectUrl: String
        get() = BuildConfig.SUPABASE_URL

    val publishableKey: String
        get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    val authUrl: String
        get() = "$projectUrl/auth/v1"

    val restUrl: String
        get() = "$projectUrl/rest/v1"
}