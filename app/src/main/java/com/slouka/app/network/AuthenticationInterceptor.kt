package com.slouka.app.network

import com.slouka.app.account.session.SessionManager
import com.slouka.app.account.session.SessionTokens
import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null) return chain.proceed(request)

        val requestedSession = request.tag(SessionTokens::class.java)
        val tokens = this.sessionManager.getTokens()
        if (requestedSession != null && requestedSession.sessionId !== tokens?.sessionId) {
            throw IOException("Session changed before sending the request")
        }
        if (tokens == null) return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .tag(SessionTokens::class.java, tokens)
                .build()
        )
    }
}
