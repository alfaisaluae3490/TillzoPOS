# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ─── Room / Database ─────────────────────────────────────────────────────────
# Keep all Room entity classes (needed for schema reflection)
-keep class com.tillzo.pos.data.local.entity.** { *; }
# Keep Room DAO implementations
-keep class * extends androidx.room.RoomDatabase { *; }

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
# Keep Hilt-generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ─── Gson / JSON models ──────────────────────────────────────────────────────
# Keep all sync model data classes (serialized to/from JSON)
-keep class com.tillzo.pos.domain.sync.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── OkHttp / Retrofit ───────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# ─── ZXing (QR codes) ────────────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ─── RootBeer ────────────────────────────────────────────────────────────────
-keep class com.scottyab.rootbeer.** { *; }

# ─── BuildConfig ─────────────────────────────────────────────────────────────
-keep class com.tillzo.pos.BuildConfig { *; }

# ─── Timber ──────────────────────────────────────────────────────────────────
-keep class timber.log.** { *; }

# ─── Play Billing ────────────────────────────────────────────────────────────
-keep class com.android.billingclient.api.** { *; }
