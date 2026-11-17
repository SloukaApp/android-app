package com.slouka.app.network.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    @SerialName("code")
    val code: Int,

    @SerialName("message")
    val message: String
)
