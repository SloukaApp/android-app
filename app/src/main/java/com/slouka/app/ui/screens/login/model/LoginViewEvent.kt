package com.slouka.app.ui.screens.login.model

sealed interface LoginViewEvent {
    data class EmailChanged(val email: String) : LoginViewEvent
    data class LoginClicked(val password: String) : LoginViewEvent
}
