package com.slouka.app.ui.screens.register.model

sealed interface RegisterViewEffect {
    data class ShowError(val message: String) : RegisterViewEffect
    data object NavigateToHome : RegisterViewEffect
}
