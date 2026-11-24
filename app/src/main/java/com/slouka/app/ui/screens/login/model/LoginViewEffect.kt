package com.slouka.app.ui.screens.login.model

import com.slouka.app.ui.UiText

sealed interface LoginViewEffect {
    data class ShowError(val message: UiText) : LoginViewEffect
    data object NavigateToHome : LoginViewEffect
}
