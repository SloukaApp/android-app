package com.slouka.app.ui.screens.register.model


data class RegisterViewState(
    val step: RegisterViewStep = RegisterViewStep.REGISTER_FORM,

    val email: String = "",
    val name: String = "",

    val code: String = "",

    val isRegistrationAccepted: Boolean = false,
    val isEmailVerified: Boolean = false,

    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val envelopeVersion: Int = 0,
    val envelopeReplacement: EnvelopeReplacement = EnvelopeReplacement.IDLE,
    val codeError: String? = null,
    val invalidCodeAttempt: Int = 0,
)
