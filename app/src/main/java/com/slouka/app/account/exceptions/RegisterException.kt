package com.slouka.app.account.exceptions

import com.slouka.app.network.api.entities.ResponseError

class RegisterException(error: ResponseError) : Exception(error.message) {}