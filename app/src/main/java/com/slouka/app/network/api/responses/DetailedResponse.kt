package com.slouka.app.network.api.responses

interface DetailedResponse<out T> {
    val detail: T
}
