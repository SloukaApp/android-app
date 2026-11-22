package com.slouka.app.account.session

import com.slouka.app.account.crypto.SecureAccountStorage
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manipulates with session in local storage.
 *
 * @param storage Object, that describes storage with {@link SharedPreferences} type
 */
@Singleton
class SessionManager @Inject constructor(
    private val storage: Lazy<SecureAccountStorage>
) {
    companion object {
        private const val SESSION_STORAGE_KEY_ACCESS_TOKEN  = "SESSION_ACCESS_TOKEN"
        private const val SESSION_STORAGE_KEY_REFRESH_TOKEN = "SESSION_REFRESH_TOKEN"
    }

    private var mTokens: SessionTokens? = null
    private var mTokensLoaded = false
    private val mSessionResets = MutableStateFlow(0L)

    val sessionResets = this.mSessionResets.asStateFlow()

    @Synchronized
    internal fun getTokens(): SessionTokens? {
        if (!this.mTokensLoaded) {
            val accountStorage = this.storage.get()
            val accessToken = accountStorage.getString(SESSION_STORAGE_KEY_ACCESS_TOKEN, null)
            val refreshToken = accountStorage.getString(SESSION_STORAGE_KEY_REFRESH_TOKEN, null)
            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                this.mTokens = SessionTokens(accessToken, refreshToken)
            }
            this.mTokensLoaded = true
        }
        return this.mTokens
    }

    fun hasStoredSession(): Boolean = this.getTokens() != null

    /**
     * Saves tokens into local storage.
     *
     * @param accessToken Access token. that will be inserted into context.
     * @param refreshToken Refresh token. that will be inserted into context.
     */
    @Synchronized
    fun saveTokens(accessToken: String, refreshToken: String) {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        this.storeTokens(SessionTokens(accessToken, refreshToken))
    }

    @Synchronized
    internal fun replaceTokens(
        expected: SessionTokens,
        accessToken: String,
        refreshToken: String
    ): SessionTokens? {
        if (this.mTokens !== expected) return null

        val tokens = SessionTokens(accessToken, refreshToken, expected.sessionId)
        this.storeTokens(tokens)
        return tokens
    }

    @Synchronized
    internal fun resetIfCurrent(expected: SessionTokens) {
        if (this.mTokens === expected) this.reset()
    }

    @Synchronized
    internal fun resetIfSameSession(expected: SessionTokens?) {
        if (this.mTokens?.sessionId === expected?.sessionId) this.reset()
    }

    private fun storeTokens(tokens: SessionTokens) {
        this.storage.get().putStrings(
            mapOf(
                SESSION_STORAGE_KEY_ACCESS_TOKEN to tokens.accessToken,
                SESSION_STORAGE_KEY_REFRESH_TOKEN to tokens.refreshToken
            )
        )
        this.mTokens = tokens
        this.mTokensLoaded = true
    }

    /**
     * Resets tokens in local storage and in context.
     */
    @Synchronized
    fun reset() {
        this.storage.get().clear()
        this.mTokens = null
        this.mTokensLoaded = true
        this.mSessionResets.value += 1
    }
}
