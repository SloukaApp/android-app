plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.devtools.ksp)
}

val authDiagnostics = providers.gradleProperty("sloukaAuthDiagnostics")
    .map { it.toBooleanStrict() }
    .orElse(false)

android {
    namespace = "com.slouka.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.slouka.app"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("boolean", "AUTH_DIAGNOSTICS", "false")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            buildConfigField("boolean", "AUTH_DIAGNOSTICS", authDiagnostics.get().toString())
            proguardFiles("proguard-rules.pro")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.hilt.android)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.serialization)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
