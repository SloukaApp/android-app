package com.slouka.app.ui.screens.sessions.model

sealed interface SessionsViewEvent {
    data class TerminateSession(val id: String) : SessionsViewEvent
}
