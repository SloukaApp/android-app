package com.slouka.app.network.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    @SerialName("detail")
    override val detail: String = "Confirmation email sent"
) : DetailedResponse<String>
