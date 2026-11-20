package com.slouka.app.ui.screens.login.model

sealed interface LoginViewEvent {
    data class EmailChanged(val email: String) : LoginViewEvent
    data class PasswordChanged(val password: String) : LoginViewEvent
    data object LoginClicked : LoginViewEvent
}
