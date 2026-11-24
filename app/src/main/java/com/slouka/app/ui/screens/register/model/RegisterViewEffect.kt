package com.slouka.app.ui.screens.register.model

import com.slouka.app.ui.UiText

sealed interface RegisterViewEffect {
    data class ShowError(val message: UiText) : RegisterViewEffect
    data object NavigateToHome : RegisterViewEffect
}
