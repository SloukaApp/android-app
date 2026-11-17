package com.slouka.app.ui.screens.login.model

sealed interface LoginViewEffect {
    data object NavigateToHome : LoginViewEffect
}