package com.slouka.app.ui.screens.login.model

import com.slouka.app.account.validation.FormField

data class LoginViewState(
    val email: String = "",

    val password: String = "",
    val fieldErrors: Map<FormField, Int> = emptyMap(),
    val shakenFields: Set<FormField> = emptySet(),
    val invalidAttempt: Int = 0,

    val isLoading: Boolean = false,

    val isLoginSuccessful: Boolean = false
)
