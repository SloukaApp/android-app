package com.slouka.app.ui.screens.register.model

sealed interface RegisterViewEvent {
    data class EmailChanged(val email: String) : RegisterViewEvent
    data class NameChanged(val name: String) : RegisterViewEvent
    data class PasswordChanged(val password: String) : RegisterViewEvent
    data class PasswordConfirmationChanged(val password: String) : RegisterViewEvent
    data object RegisterClicked : RegisterViewEvent

    data class CodeChanged(val code: String) : RegisterViewEvent
    data object BackToRegistrationForm : RegisterViewEvent
    data object ClubSignatureShown : RegisterViewEvent
    data object VerificationStampShown : RegisterViewEvent
    data object ResendCodeClicked : RegisterViewEvent
    data object OldEnvelopeDismissed : RegisterViewEvent
    data object NewEnvelopeArrived : RegisterViewEvent
    data object VerifyClicked : RegisterViewEvent
}
