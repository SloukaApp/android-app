package com.slouka.app.network.api.responses

import com.slouka.app.network.api.entities.ResponseError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("is_successful")
    val isSuccessful: Boolean,
    @SerialName("error")
    val error: ResponseError?,

    @SerialName("access_token")
    val accessToken: String?,

    @SerialName("refresh_token")
    val refreshToken: String?,
)
