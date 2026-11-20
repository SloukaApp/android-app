package com.slouka.app.ui.screens.sessions.model

data class SessionsViewState(
    val sessions: List<Session> = emptyList(),
    val terminatingIds: Set<String> = emptySet()
)
