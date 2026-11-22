package com.slouka.app.account.session

internal class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val sessionId: Any = Any()
)
