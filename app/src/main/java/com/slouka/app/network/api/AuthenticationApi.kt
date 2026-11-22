package com.slouka.app.network.api

import com.slouka.app.network.api.requests.LoginRequest
import com.slouka.app.network.api.requests.RefreshRequest
import com.slouka.app.network.api.requests.RegisterRequest
import com.slouka.app.network.api.requests.ResendCodeRequest
import com.slouka.app.network.api.requests.VerificationRequest
import com.slouka.app.network.api.responses.RegisterResponse
import com.slouka.app.network.api.responses.ResendCodeResponse
import com.slouka.app.network.api.responses.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

@Suppress("unused")
interface AuthenticationApi {
    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): RegisterResponse

    @POST("auth/confirmations/resend")
    suspend fun resendConfirmationCode(@Body req: ResendCodeRequest): ResendCodeResponse

    @POST("auth/confirm-email")
    suspend fun verifyEmail(@Body req: VerificationRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @POST("auth/refresh")
    fun refreshToken(@Body req: RefreshRequest): Call<TokenResponse>
}
