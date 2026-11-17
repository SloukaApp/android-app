package com.slouka.app.ui.screens.register.model

enum class RegisterViewStep(val index: Int) {
    REGISTER_FORM(1),
    VERIFICATION(2);

    operator fun minus(other: RegisterViewStep): Int {
        return this.index - other.index
    }
}