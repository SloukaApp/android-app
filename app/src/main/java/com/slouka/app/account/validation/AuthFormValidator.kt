package com.slouka.app.account.validation

import android.util.Patterns
import com.slouka.app.R
import javax.inject.Inject

class AuthFormValidator @Inject constructor() {
    fun validateLogin(email: String, password: String): Map<FormField, Int> = buildMap {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            put(FormField.EMAIL, R.string.auth_error_invalid_email)
        }
        if (password.isBlank()) put(FormField.PASSWORD, R.string.auth_error_password_required)
    }

    fun validateRegistration(
        email: String,
        password: String,
        passwordConfirmation: String,
        isConsentActive: Boolean
    ): Map<FormField, Int> = buildMap {
        putAll(this@AuthFormValidator.validateLogin(email, password))
        if (password.isNotBlank() && password.codePointCount(0, password.length) < 8) {
            put(FormField.PASSWORD, R.string.register_error_password_length)
        }
        if (password != passwordConfirmation) {
            put(FormField.CONFIRMATION, R.string.register_error_password_mismatch)
        } else if (passwordConfirmation.isBlank()) {
            put(FormField.CONFIRMATION, R.string.register_error_confirmation_required)
        }
        if (!isConsentActive) put(FormField.CONSENT, R.string.register_error_consent_required)
    }
}
