package com.slouka.app.ui.screens.register.model

import com.slouka.app.account.validation.FormField

data class RegisterViewState(
    val step: RegisterViewStep = RegisterViewStep.REGISTER_FORM,

    val email: String = "",
    val name: String = "",

    val code: String = "",

    val isRegistrationAccepted: Boolean = false,
    val isEmailVerified: Boolean = false,

    val password: String = "",
    val passwordConfirmation: String = "",
    internal val showFieldErrors: Boolean = false,
    val fieldErrors: Map<FormField, String> = emptyMap(),
    val shakenFields: Set<FormField> = emptySet(),
    val invalidAttempt: Int = 0,

    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val envelopeVersion: Int = 0,
    val envelopeReplacement: EnvelopeReplacement = EnvelopeReplacement.IDLE,
    val codeError: String? = null,
    val invalidCodeAttempt: Int = 0,
)
