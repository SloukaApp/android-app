package com.slouka.app.ui.screens.register.model

import androidx.annotation.StringRes

import com.slouka.app.account.validation.FormField

data class RegisterViewState(
    val step: RegisterViewStep = RegisterViewStep.REGISTER_FORM,

    val email: String = "",

    val code: String = "",

    val isRegistrationAccepted: Boolean = false,
    val isEmailVerified: Boolean = false,

    val password: String = "",
    val passwordConfirmation: String = "",
    val isConsentActive: Boolean = false,
    internal val showFieldErrors: Boolean = false,
    val fieldErrors: Map<FormField, Int> = emptyMap(),
    val shakenFields: Set<FormField> = emptySet(),
    val invalidAttempt: Int = 0,

    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val envelopeVersion: Int = 0,
    val envelopeReplacement: EnvelopeReplacement = EnvelopeReplacement.IDLE,
    @StringRes val codeError: Int? = null,
    val invalidCodeAttempt: Int = 0,
)
