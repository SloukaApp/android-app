package com.slouka.app.ui.screens.login.model

sealed interface LoginViewEffect {
    data class ShowError(val message: String) : LoginViewEffect
    data object NavigateToHome : LoginViewEffect
}
