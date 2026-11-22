package com.slouka.app.network.api

import com.slouka.app.account.session.SessionTokens
import retrofit2.http.POST
import retrofit2.http.Tag

internal interface AccountApi {
    @POST("auth/logout")
    suspend fun logout(@Tag session: SessionTokens)
}
