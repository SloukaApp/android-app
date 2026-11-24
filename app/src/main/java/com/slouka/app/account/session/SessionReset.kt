package com.slouka.app.account.session

internal data class SessionReset(val sequence: Long = 0, val isLogout: Boolean = false)
