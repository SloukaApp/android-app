package com.slouka.app.network.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResendCodeRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
) {
}