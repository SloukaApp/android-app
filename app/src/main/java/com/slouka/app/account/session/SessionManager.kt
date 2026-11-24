package com.slouka.app.account.session

import com.slouka.app.account.crypto.SecureAccountStorage
import dagger.Lazy
import java.io.IOException
import java.security.GeneralSecurityException
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
        private const val SESSION_STORAGE_KEY_EMAIL = "SESSION_EMAIL"
    }

    private var mTokens: SessionTokens? = null
    private var mTokensLoaded = false
    private val mSessionResets = MutableStateFlow(SessionReset())
    private val mAccountEmail = MutableStateFlow<String?>(null)

    internal val sessionResets = this.mSessionResets.asStateFlow()
    val accountEmail = this.mAccountEmail.asStateFlow()

    @Synchronized
    internal fun getTokens(): SessionTokens? {
        if (!this.mTokensLoaded) {
            val accountStorage = this.storage.get()
            val accessToken = accountStorage.getString(SESSION_STORAGE_KEY_ACCESS_TOKEN, null)
            val refreshToken = accountStorage.getString(SESSION_STORAGE_KEY_REFRESH_TOKEN, null)
            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                val email = this.readEmail(accountStorage)
                this.mTokens = SessionTokens(accessToken, refreshToken, email)
                this.mAccountEmail.value = email
            }
            this.mTokensLoaded = true
        }
        return this.mTokens
    }

    fun hasStoredSession(): Boolean = this.getTokens() != null

    private fun readEmail(accountStorage: SecureAccountStorage): String? = try {
        accountStorage.getString(SESSION_STORAGE_KEY_EMAIL, null)
    } catch (_: GeneralSecurityException) {
        null
    } catch (_: IOException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: ClassCastException) {
        null
    }

    /**
     * Saves tokens into local storage.
     *
     * @param accessToken Access token. that will be inserted into context.
     * @param refreshToken Refresh token. that will be inserted into context.
     * @param email Email associated with the authenticated session.
     */
    @Synchronized
    fun saveTokens(accessToken: String, refreshToken: String, email: String? = null) {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        this.storeTokens(SessionTokens(accessToken, refreshToken, email))
    }

    @Synchronized
    internal fun replaceTokens(
        expected: SessionTokens,
        accessToken: String,
        refreshToken: String
    ): SessionTokens? {
        if (this.mTokens !== expected) return null

        val tokens = SessionTokens(accessToken, refreshToken, expected.email, expected.sessionId)
        this.storeTokens(tokens)
        return tokens
    }

    @Synchronized
    internal fun resetIfCurrent(expected: SessionTokens) {
        if (this.mTokens === expected) this.reset()
    }

    @Synchronized
    internal fun resetAfterLogout(expected: SessionTokens?) {
        if (this.mTokens?.sessionId === expected?.sessionId) this.reset(isLogout = true)
    }

    private fun storeTokens(tokens: SessionTokens) {
        this.storage.get().putStrings(
            mapOf(
                SESSION_STORAGE_KEY_ACCESS_TOKEN to tokens.accessToken,
                SESSION_STORAGE_KEY_REFRESH_TOKEN to tokens.refreshToken,
                SESSION_STORAGE_KEY_EMAIL to tokens.email
            )
        )
        this.mTokens = tokens
        this.mTokensLoaded = true
        this.mAccountEmail.value = tokens.email
    }

    /**
     * Resets tokens in local storage and in context.
     */
    @Synchronized
    fun reset() = this.reset(isLogout = false)

    private fun reset(isLogout: Boolean) {
        this.storage.get().clear()
        this.mTokens = null
        this.mTokensLoaded = true
        this.mAccountEmail.value = null
        this.mSessionResets.value = SessionReset(this.mSessionResets.value.sequence + 1, isLogout)
    }
}
