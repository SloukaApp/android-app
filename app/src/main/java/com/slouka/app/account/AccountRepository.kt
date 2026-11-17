package com.slouka.app.account

import com.slouka.app.account.exceptions.LoginException
import com.slouka.app.account.exceptions.RegisterException
import com.slouka.app.account.session.SessionManager
import com.slouka.app.network.api.AuthenticationApi
import com.slouka.app.network.api.requests.LoginRequest
import com.slouka.app.network.api.requests.RegisterRequest
import com.slouka.app.network.api.requests.ResendCodeRequest
import com.slouka.app.network.api.requests.VerificationRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val api: AuthenticationApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val tokens = this.api.login(LoginRequest(email, password))
            if (!tokens.isSuccessful)
                throw LoginException(tokens.error!!)

            this.sessionManager.saveTokens(
                tokens.accessToken!!,
                tokens.refreshToken!!
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, name: String, password: String): Result<Unit> {
        return try {
            val response = this.api.register(RegisterRequest(email, name, password))
            if (!response.isSuccessful)
                throw RegisterException(response.error!!)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyRegistration(code: String): Result<Unit> {
        return try {
            val tokens = this.api.verifyEmail(VerificationRequest(code))
            if (!tokens.isSuccessful)
                throw RegisterException(tokens.error!!)

            this.sessionManager.saveTokens(
                tokens.accessToken!!,
                tokens.refreshToken!!
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendConfirmationCode(email: String, password: String): Result<Unit> {
        return try {
            val response = this.api.resendConfirmationCode(ResendCodeRequest(email, password))
            if (!response.isSuccessful)
                throw RegisterException(response.error!!)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        this.sessionManager.reset()
        return Result.success(Unit)
    }
}
