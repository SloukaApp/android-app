package com.slouka.app.account.exceptions

import com.slouka.app.network.api.entities.ResponseError

class LoginException(error: ResponseError) : Exception(error.message) {}
