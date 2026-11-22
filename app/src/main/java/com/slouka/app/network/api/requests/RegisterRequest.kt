package com.slouka.app.network.api.requests

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    @SerialName("consent")
    val isConsentActive: Boolean,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    @SerialName("consent_version")
    val consentVersion: String = "1",
)
