package com.slouka.app.account.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageCryptoEngine @Inject constructor() {
    private val alias: String = "account_storage_key"
    private val provider: String = "AndroidKeyStore"

    init {
        val keyStore = KeyStore.getInstance(this.provider).apply { load(null) }
        if (!keyStore.containsAlias(this.alias)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        this@StorageCryptoEngine.alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                generateKey()
            }
        }
    }

    fun decrypt(data: String): String {
        val keyStore = KeyStore.getInstance(this.provider).apply { load(null) }
        val secretKey = keyStore.getKey(this.alias, null) as SecretKey

        val combined = Base64.decode(data, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        }

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    fun encrypt(data: String): String {
        val keyStore = KeyStore.getInstance(this.provider).apply { load(null) }
        val secretKey = keyStore.getKey(this.alias, null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }

        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }
}
