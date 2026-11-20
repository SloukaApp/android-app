package com.slouka.app.ui.screens.sessions.model

data class Session(
    val id: String,
    val device: String,
    val details: String,
    val lastActive: String,
    val isCurrent: Boolean = false,
    val deviceType: SessionDeviceType = SessionDeviceType.PHONE
)
