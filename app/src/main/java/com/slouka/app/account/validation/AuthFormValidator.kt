package com.slouka.app.account.validation

import android.util.Patterns
import javax.inject.Inject

class AuthFormValidator @Inject constructor() {
    fun validateLogin(email: String, password: String): Map<FormField, String> = buildMap {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            put(FormField.EMAIL, "Enter a valid email address.")
        }
        if (password.isBlank()) put(FormField.PASSWORD, "Enter a password.")
    }

    fun validateRegistration(
        email: String,
        password: String,
        passwordConfirmation: String,
        isConsentActive: Boolean
    ): Map<FormField, String> = buildMap {
        putAll(this@AuthFormValidator.validateLogin(email, password))
        if (password.isNotBlank() && password.codePointCount(0, password.length) < 8) {
            put(FormField.PASSWORD, "Use at least 8 characters.")
        }
        if (password != passwordConfirmation) {
            put(FormField.CONFIRMATION, "Passwords must match.")
        } else if (passwordConfirmation.isBlank()) {
            put(FormField.CONFIRMATION, "Confirm your password.")
        }
        if (!isConsentActive) put(FormField.CONSENT, "Confirm your consent to continue.")
    }
}
