package com.slouka.app.account.crypto

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SecureAccountStorage @Inject constructor(
    private val crypto: StorageCryptoEngine,
    private val prefs: SharedPreferences
) {
    fun getString(key: String, default: String?): String? {
        if (!this.prefs.contains(key))
            return default

        return this.crypto.decrypt(this.prefs.getString(key, null)!!)
    }

    fun putStrings(values: Map<String, String?>) {
        val encryptedValues = values.mapValues { (_, value) -> value?.let(this.crypto::encrypt) }
        this.prefs.edit {
            encryptedValues.forEach { (key, value) -> putString(key, value) }
        }
    }

    fun clear() {
        this.prefs.edit { clear() }
    }
}
