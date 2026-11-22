package com.slouka.app.account

import com.slouka.app.BuildConfig
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.account.session.SessionManager
import com.slouka.app.network.AuthenticationDiagnostics
import com.slouka.app.network.api.AccountApi
import com.slouka.app.network.api.AuthenticationApi
import com.slouka.app.network.api.requests.LoginRequest
import com.slouka.app.network.api.requests.RegisterRequest
import com.slouka.app.network.api.requests.ResendCodeRequest
import com.slouka.app.network.api.requests.VerificationRequest
import com.slouka.app.network.api.responses.TokenResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException

@Singleton
class AccountRepository @Inject internal constructor(
    private val api: AuthenticationApi,
    private val accountApi: AccountApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<Unit> = this.request("login") {
        val tokens = this.api.login(LoginRequest(email = email.trim(), password = password))
        this.saveTokens(tokens)
    }

    suspend fun register(
        email: String,
        password: String,
        isConsentActive: Boolean
    ): Result<Unit> = this.request("register") {
        this.api.register(
            RegisterRequest(email = email.trim(), password = password, isConsentActive = isConsentActive)
        )
    }

    suspend fun verifyRegistration(email: String, code: String): Result<Unit> = this.request("confirm email") {
        val tokens = this.api.verifyEmail(VerificationRequest(email = email.trim(), code = code))
        this.saveTokens(tokens)
    }

    suspend fun resendConfirmationCode(email: String, password: String): Result<Unit> = this.request("resend code") {
        this.api.resendConfirmationCode(ResendCodeRequest(email = email.trim(), password = password))
    }

    suspend fun logout(): Result<Unit> = this.request("logout") {
        val session = this.sessionManager.getTokens()
        if (session != null) this.accountApi.logout(session)
        this.sessionManager.resetIfSameSession(session)
    }

    private fun saveTokens(tokens: TokenResponse) {
        this.sessionManager.saveTokens(
            accessToken = checkNotNull(tokens.accessToken) { "Missing access token" },
            refreshToken = checkNotNull(tokens.refreshToken) { "Missing refresh token" }
        )
    }

    private suspend fun request(operation: String, call: suspend () -> Unit): Result<Unit> {
        return try {
            call()
            if (BuildConfig.AUTH_DIAGNOSTICS) AuthenticationDiagnostics.logAccountSuccess(operation)
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: HttpException) {
            val detail = this.readErrorDetail(failure)
            if (BuildConfig.AUTH_DIAGNOSTICS) {
                AuthenticationDiagnostics.logAccountHttpError(operation, failure.code(), detail)
            }
            Result.failure(AccountRequestException(failure, detail))
        } catch (failure: Exception) {
            if (BuildConfig.AUTH_DIAGNOSTICS) AuthenticationDiagnostics.logAccountFailure(operation, failure)
            Result.failure(failure)
        }
    }

    private fun readErrorDetail(failure: HttpException): JsonElement? {
        val body = failure.response()?.errorBody() ?: return null
        return try {
            val payload = Json.parseToJsonElement(body.use { it.string() })
            (payload as? JsonObject)?.get("detail")
        } catch (_: IOException) {
            null
        } catch (_: SerializationException) {
            null
        }
    }
}
