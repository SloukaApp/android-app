package com.slouka.app.network

import com.slouka.app.account.session.SessionManager
import com.slouka.app.network.api.AuthenticationApi
import com.slouka.app.network.api.requests.RefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authenticationApi: AuthenticationApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = sessionManager.getRefreshToken() ?: return null
        val refreshResponse = authenticationApi.refreshToken(RefreshRequest(refreshToken)).execute()
        if (!refreshResponse.isSuccessful) {
            sessionManager.reset()
            return null
        }

        val body = refreshResponse.body()!!
        if (!body.isSuccessful) {
            sessionManager.reset()
            return null
        }

        sessionManager.saveTokens(
            body.accessToken!!,
            body.refreshToken!!
        )

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.accessToken}")
            .build()
    }
}
