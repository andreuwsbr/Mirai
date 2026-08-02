import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties =
    Properties().apply {
        val propertiesFile =
            rootProject.file(
                "local.properties"
            )

        if (propertiesFile.exists()) {
            propertiesFile
                .inputStream()
                .use { inputStream ->
                    load(inputStream)
                }
        }
    }

fun buildConfigString(
    value: String
): String {
    val escapedValue =
        value
            .replace(
                oldValue = "\\",
                newValue = "\\\\"
            )
            .replace(
                oldValue = "\"",
                newValue = "\\\""
            )

    return "\"$escapedValue\""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.andrews.mirai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.andrews.mirai"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"

        buildConfigField(
            type = "String",
            name = "SUPABASE_URL",
            value = buildConfigString(
                localProperties
                    .getProperty(
                        "SUPABASE_URL",
                        ""
                    )
                    .trim()
            )
        )

        buildConfigField(
            type = "String",
            name = "SUPABASE_PUBLISHABLE_KEY",
            value = buildConfigString(
                localProperties
                    .getProperty(
                        "SUPABASE_PUBLISHABLE_KEY",
                        ""
                    )
                    .trim()
            )
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(
            JvmTarget.JVM_17
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )
    implementation(
        libs.androidx.activity.compose
    )
    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )
    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )
    implementation(
        libs.androidx.compose.ui.graphics
    )
    implementation(
        libs.androidx.compose.ui.tooling.preview
    )
    implementation(
        libs.androidx.compose.material3
    )
    implementation(
        libs.androidx.compose.material.icons.extended
    )
    implementation(
        libs.androidx.navigation.compose
    )

    implementation(
        "androidx.core:core-splashscreen:1.0.1"
    )

    implementation(libs.okhttp)
    implementation(
        libs.okhttp.logging
    )
    implementation(libs.jsoup)

    implementation(libs.coil.compose)
    implementation(
        libs.coil.network.okhttp
    )
    implementation(
        libs.telephoto.zoomable.image.coil3
    )

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.junit
    )
    androidTestImplementation(
        libs.androidx.espresso.core
    )
    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )
    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}