package com.slouka.app.account.session

import com.slouka.app.account.crypto.SecureAccountStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manipulates with session in local storage.
 *
 * @param storage Object, that describes storage with {@link SharedPreferences} type
 */
@Singleton
class SessionManager @Inject constructor(
    private val storage: SecureAccountStorage
) {
    companion object {
        private const val SESSION_STORAGE_KEY_ACCESS_TOKEN  = "SESSION_ACCESS_TOKEN"
        private const val SESSION_STORAGE_KEY_REFRESH_TOKEN = "SESSION_REFRESH_TOKEN"
    }

    private var mAccessToken: String? = null
    private var mRefreshToken: String? = null

    /**
     * Returns access token.
     *
     * @return Access token, if it exists, otherwise {@code null}
     */
    fun getAccessToken(): String? =
        this.storage.getString(SESSION_STORAGE_KEY_ACCESS_TOKEN, null)
            .also { token -> this.mAccessToken = token }

    /**
     * Returns refresh token.
     *
     * @return Refresh token, if it exists, otherwise {@code null}
     */
    fun getRefreshToken(): String? =
        this.storage.getString(SESSION_STORAGE_KEY_REFRESH_TOKEN, null)
            .also { token -> this.mRefreshToken = token }

    /**
     * Saves tokens into local storage.
     *
     * @param accessToken Access token. that will be inserted into context.
     * @param refreshToken Refresh token. that will be inserted into context.
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        this.mAccessToken = accessToken
        this.mRefreshToken = refreshToken

        this.storage.putString(SESSION_STORAGE_KEY_ACCESS_TOKEN, accessToken)
        this.storage.putString(SESSION_STORAGE_KEY_REFRESH_TOKEN, refreshToken)
    }

    /**
     * Resets tokens in local storage and in context.
     */
    fun reset() {
        this.mAccessToken = null
        this.mRefreshToken = null

        this.storage.clear()
    }
}