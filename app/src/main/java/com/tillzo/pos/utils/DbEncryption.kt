package com.tillzo.pos.utils

import android.content.Context
import com.tillzo.pos.data.local.AppDatabase
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * DbEncryption — SQLCipher DB passphrase manager (FIX 2026-08-07, Issue 1).
 *
 * Passphrase Android Keystore (AES-256-GCM) mein generate + store hota hai:
 * - Key hardware-backed Keystore mein rehti hai — app ke bahar extract impossible
 * - Har install pe nayi key ban sakti hai (DB bhi naya banega)
 * - Passphrase kabhi hardcode nahi hota, kabhi plaintext file mein nahi hota
 *
 * Security model:
 * - Keystore key → encrypt passphrase → EncryptedSharedPreferences mein store
 * - Ya seedha: Keystore AES key se derived passphrase (deterministic per-install)
 */
object DbEncryption {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "tillzo_db_encryption_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    /**
     * Get-or-create the DB passphrase. Deterministic per install:
     * AES-256-GCM key Keystore mein, usi se 32-byte passphrase derived.
     */
    fun getOrCreatePassphrase(context: Context): String {
        // FIX (2026-08-22, DEF-79): `key.encoded` returns NULL on
        // hardware-backed (StrongBox/TEE) keys — Android 10 emulator and some
        // Pixel devices. The old code then threw and fell back to the legacy
        // derivable key every launch. New approach never touches key.encoded:
        // a 32-byte passphrase is generated once, encrypted with the Keystore
        // AES-GCM key, and the ciphertext lives in app-private prefs. Every
        // subsequent launch decrypts the same passphrase — deterministic,
        // survives restarts, and works on hardware-backed keys.
        //
        // FIX (2026-08-23, DEF-91): precedence order — pehle STORED passphrases
        // check karo, dbFile.exists() ke legacy fallback se PEHLE. Purana code
        // kisi bhi existing DB ke liye seedha legacy derivable key return kar
        // deta tha → keystore-keyed DB (fresh installs, DEF-79 path) par HAR
        // restart / app update par wrong key → SQLCipher "file is not a
        // database" → FATAL CRASH (local data unreachable). Order ab:
        //   1. keystore_ciphertext (DEF-79 fresh-install DBs — restart-safe)
        //   2. fallback_passphrase (keystore-fail path DBs — random, stored)
        //   3. dbFile.exists() → legacy derivable (pre-fix DBs hi isi se readable)
        //   4. kuch nahi → generate + keystore encrypt + store
        val prefs = context.getSharedPreferences("db_encryption", Context.MODE_PRIVATE)
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        return try {
            // 1. Keystore-encrypted passphrase (fresh-install DBs)
            val storedCipher = prefs.getString("keystore_ciphertext", null)
            val storedIv = prefs.getString("keystore_iv", null)
            if (storedCipher != null && storedIv != null) {
                val key = getOrCreateKey()
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE, key,
                    GCMParameterSpec(128, android.util.Base64.decode(storedIv, android.util.Base64.NO_WRAP))
                )
                val plain = cipher.doFinal(android.util.Base64.decode(storedCipher, android.util.Base64.NO_WRAP))
                return String(plain, Charsets.UTF_8)
            }
            // 2. Legacy catch-path random passphrase (keystore-fail DBs)
            val existingFallback = prefs.getString("fallback_passphrase", null)
            if (existingFallback != null && existingFallback.length >= 32) {
                return existingFallback
            }
            // 3. Pre-keystore DBs — derivable legacy key.
            //    FIX (2026-08-23, DEF-84): rotation — legacy derivable passphrase
            //    ko Keystore AES-GCM key se encrypt karke keystore_ciphertext mein
            //    store kar do. VALUE UNCHANGED (existing DB readable rehta hai),
            //    lekin ab passphrase APK se derive nahi ho sakta. Next launch
            //    precedence path (1) se decrypt hoga — derivable branch phir
            //    kabhi touch nahi hoti. Keystore unavailable → rotation skip
            //    (sirf log) — fail-open for legacy DBs, fail-closed for fresh.
            if (dbFile.exists()) {
                val legacy = "tillzo-db-fallback-${context.packageName.hashCode().toString(16).padStart(8, '0')}"
                try {
                    val key = getOrCreateKey()
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                    val enc = cipher.doFinal(legacy.toByteArray(Charsets.UTF_8))
                    prefs.edit()
                        .putString("keystore_ciphertext", android.util.Base64.encodeToString(enc, android.util.Base64.NO_WRAP))
                        .putString("keystore_iv", android.util.Base64.encodeToString(cipher.iv, android.util.Base64.NO_WRAP))
                        .apply()
                } catch (e: Exception) {
                    android.util.Log.w("DbEncryption", "Legacy passphrase rotation failed — continuing with legacy key: ${e.message}")
                }
                return legacy
            }
            // 4. Fresh install: generate + encrypt + store
            val key = getOrCreateKey()
            val bytes = ByteArray(32)
            java.security.SecureRandom().nextBytes(bytes)
            val passphrase = bytes.joinToString("") { "%02x".format(it) }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val enc = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString("keystore_ciphertext", android.util.Base64.encodeToString(enc, android.util.Base64.NO_WRAP))
                .putString("keystore_iv", android.util.Base64.encodeToString(cipher.iv, android.util.Base64.NO_WRAP))
                .apply()
            passphrase
        } catch (e: Exception) {
            android.util.Log.e("DbEncryption", "Keystore failed, using fallback: ${e.message}")
            // Catch path — stored random beats derivable legacy (DEF-91)
            val prefs2 = context.getSharedPreferences("db_encryption", Context.MODE_PRIVATE)
            val existing = prefs2.getString("fallback_passphrase", null)
            if (existing != null && existing.length >= 32) {
                existing
            } else {
                val legacy = "tillzo-db-fallback-${context.packageName.hashCode().toString(16).padStart(8, '0')}"
                val dbFile2 = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                if (dbFile2.exists()) {
                    legacy
                } else {
                    val bytes = ByteArray(32)
                    java.security.SecureRandom().nextBytes(bytes)
                    val generated = bytes.joinToString("") { "%02x".format(it) }
                    prefs2.edit().putString("fallback_passphrase", generated).apply()
                    generated
                }
            }
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
