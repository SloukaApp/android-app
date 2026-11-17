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

    fun putString(key: String, value: String) {
        this.prefs.edit {
            putString(key, this@SecureAccountStorage.crypto.encrypt(value))
        }
    }

    fun clear() {
        this.prefs.edit { clear() }
    }
}