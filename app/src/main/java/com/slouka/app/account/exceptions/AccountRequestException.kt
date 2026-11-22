package com.slouka.app.account.exceptions

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

internal class AccountRequestException(
    cause: HttpException,
    val detail: JsonElement?
) : Exception(cause.message, cause) {
    val statusCode = cause.code()
    val detailMessage: String?
        get() = (this.detail as? JsonPrimitive)?.takeIf { it.isString }?.content
}
