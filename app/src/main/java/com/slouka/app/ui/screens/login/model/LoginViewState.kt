package com.slouka.app.ui.screens.login.model


data class LoginViewState(
    val email: String = "",

    val isLoading: Boolean = false,
    val error: String? = null,

    val isLoginSuccessful: Boolean = false
)
