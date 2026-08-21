plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // google-services plugin NOT needed — play-services-auth works with Web Client ID directly
    // Firebase is NOT part of this project (blueprint has no Firebase requirement)
}

android {
    namespace = "com.tillzo.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tillzo.pos"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        ndk {
            version = "27.0.12077973"
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // APPS_SCRIPT_URL is set at runtime from SharedPrefs after auto-provisioning
        buildConfigField("String", "PLAY_STORE_URL", "\"https://play.google.com/store/apps/details?id=com.tillzo.pos\"")
        
        manifestPlaceholders += mapOf("appAuthRedirectScheme" to "com.tillzo.pos")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "NativeApiIssues"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            excludes.add("**/libtoolChecker.so")
            excludes.add("**/libtoolChecker*")
            excludes.add("lib/**/libtoolChecker.so")
            excludes.add("lib/arm64-v8a/libtoolChecker.so")
            excludes.add("lib/armeabi-v7a/libtoolChecker.so")
            excludes.add("lib/x86/libtoolChecker.so")
            excludes.add("lib/x86_64/libtoolChecker.so")
            excludes.add("arm64-v8a/libtoolChecker.so")
            excludes.add("armeabi-v7a/libtoolChecker.so")
            excludes.add("x86/libtoolChecker.so")
            excludes.add("x86_64/libtoolChecker.so")
        }
        resources {
            excludes += setOf("**/libtoolChecker.so")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lifecycle / ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.workmanager.ktx)

    // Logging (Timber)
    implementation(libs.timber)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // DataStore
    implementation(libs.datastore.preferences)

    // AppCompat (needed for XML theme resolution)
    implementation(libs.appcompat)

    // Security / Root Detection
    // M8.2: RootBeer added for Root Detection per blueprint requirements.
    implementation(libs.rootbeer)

    // M8.4: Google Play Billing
    implementation(libs.billing.ktx)

    // Security Crypto — EncryptedSharedPreferences for OAuth token storage (M2.10)
    implementation(libs.security.crypto)

    // SQLCipher — encrypted Room database (FIX 2026-08-07: Issue 1 — plaintext SQLite)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)

    // Google Sign-In / Credentials
    implementation(libs.google.signin)
    implementation(libs.credentials)
    implementation(libs.credentials.play.auth)
    implementation(libs.googleid)
    implementation(libs.appauth)

    // QR Code
    implementation(libs.zxing.core)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // ML Kit Vision
    implementation(libs.mlkit.barcode)
    implementation(libs.mlkit.text)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(libs.room.testing)
}
