package com.slouka.app.network.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationRequest(
    @SerialName("email")
    val email: String,

    @SerialName("token")
    val code: String
)