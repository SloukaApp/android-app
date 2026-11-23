package com.slouka.app.account.session

internal class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val email: String? = null,
    val sessionId: Any = Any()
)
