package com.slouka.app.network

import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Response

internal class AuthenticationDiagnostics : EventListener() {
    companion object {
        private const val LOG_TAG = "SloukaAuth"
        private val mValidationFields = setOf("email", "password", "token", "consent", "consent_version")

        fun logAccountSuccess(operation: String) {
            Log.i(LOG_TAG, "Account operation $operation completed")
        }

        fun logAccountHttpError(operation: String, statusCode: Int, detail: JsonElement?) {
            val invalidFields = (detail as? JsonArray).orEmpty()
                .flatMap { ((it as? JsonObject)?.get("loc") as? JsonArray).orEmpty() }
                .mapNotNull { (it as? JsonPrimitive)?.content }
                .filter { it in mValidationFields }
                .distinct()
            Log.w(LOG_TAG, "Account operation $operation: HTTP $statusCode, invalid fields $invalidFields")
        }

        fun logAccountFailure(operation: String, failure: Exception) {
            Log.w(LOG_TAG, "Account operation $operation failed: ${describeFailure(failure)}")
        }

        private fun describeFailure(failure: Throwable): String {
            return generateSequence(failure) { it.cause }.take(4).joinToString(" -> ") {
                if (it is SerializationException) "SerializationException" else it.javaClass.simpleName
            }
        }
    }

    private var mStartedAt = 0L
    private var mPhase = "start"

    override fun callStart(call: Call) {
        this.mStartedAt = SystemClock.elapsedRealtime()
        Log.i(LOG_TAG, "Sending ${this.requestLabel(call)}")
    }

    override fun dnsStart(call: Call, domainName: String) {
        this.mPhase = "DNS"
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        this.mPhase = "connect"
    }

    override fun secureConnectStart(call: Call) {
        this.mPhase = "TLS"
    }

    override fun requestHeadersStart(call: Call) {
        this.mPhase = "request"
    }

    override fun responseHeadersStart(call: Call) {
        this.mPhase = "response headers"
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        Log.i(LOG_TAG, "${this.requestLabel(call)}: HTTP ${response.code} after ${this.elapsedMillis()} ms")
    }

    override fun responseBodyStart(call: Call) {
        this.mPhase = "response body"
    }

    override fun callFailed(call: Call, ioe: IOException) {
        Log.w(
            LOG_TAG,
            "${this.requestLabel(call)} failed during ${this.mPhase} after ${this.elapsedMillis()} ms: ${describeFailure(ioe)}"
        )
    }

    private fun requestLabel(call: Call): String {
        val request = call.request()
        return "${request.method} ${request.url.encodedPath}"
    }

    private fun elapsedMillis(): Long = SystemClock.elapsedRealtime() - this.mStartedAt
}
