package com.slouka.app.ui.screens.sessions.model

sealed interface SessionsViewEffect {
    data class ShowError(val message: String) : SessionsViewEffect
}
