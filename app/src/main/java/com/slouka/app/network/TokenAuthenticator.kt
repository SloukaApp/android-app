package com.slouka.app.network

import com.slouka.app.account.session.SessionManager
import com.slouka.app.account.session.SessionTokens
import com.slouka.app.network.api.AuthenticationApi
import com.slouka.app.network.api.requests.RefreshRequest
import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authenticationApi: AuthenticationApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val sentTokens = request.tag(SessionTokens::class.java) ?: return null
        if (request.header("Authorization") != "Bearer ${sentTokens.accessToken}") return null

        val alreadyRetried = generateSequence(response.priorResponse) { it.priorResponse }
            .any { it.code == HTTP_UNAUTHORIZED }
        if (alreadyRetried) return null
        if (request.body?.isOneShot() == true || request.body?.isDuplex() == true) return null

        synchronized(this) {
            val currentTokens = this.sessionManager.getTokens() ?: return null
            if (currentTokens.sessionId !== sentTokens.sessionId) return null

            val tokens = if (currentTokens !== sentTokens) {
                currentTokens
            } else {
                this.refresh(currentTokens) ?: return null
            }

            return request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .tag(SessionTokens::class.java, tokens)
                .build()
        }
    }

    private fun refresh(currentTokens: SessionTokens): SessionTokens? {
        val refreshResponse = try {
            this.authenticationApi.refreshToken(RefreshRequest(refreshToken = currentTokens.refreshToken)).execute()
        } catch (_: IOException) {
            return null
        } catch (_: SerializationException) {
            return null
        }

        if (!refreshResponse.isSuccessful) {
            refreshResponse.errorBody()?.close()
            if (refreshResponse.code() == HTTP_UNAUTHORIZED) {
                this.sessionManager.resetIfCurrent(currentTokens)
            }
            return null
        }

        val body = refreshResponse.body() ?: return null
        val accessToken = body.accessToken
        val refreshToken = body.refreshToken
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) return null

        return this.sessionManager.replaceTokens(
            expected = currentTokens,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
