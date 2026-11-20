package com.slouka.app.ui.screens.register.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.validation.AuthFormValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val validator: AuthFormValidator
) : ViewModel() {
    private var verificationNavigationSent = false
    private val mViewEffect = Channel<RegisterViewEffect>(Channel.BUFFERED)
    private val mViewState = MutableStateFlow(RegisterViewState())

    val effect = this.mViewEffect.receiveAsFlow()
    val state = this.mViewState.asStateFlow()

    fun onEvent(event: RegisterViewEvent) {
        when (event) {
            is RegisterViewEvent.EmailChanged -> this.editForm { it.copy(email = event.email) }
            is RegisterViewEvent.NameChanged -> this.editForm { it.copy(name = event.name) }
            is RegisterViewEvent.PasswordChanged -> this.editForm { it.copy(password = event.password) }
            is RegisterViewEvent.PasswordConfirmationChanged -> this.editForm {
                it.copy(passwordConfirmation = event.password)
            }
            is RegisterViewEvent.RegisterClicked -> this.register()

            is RegisterViewEvent.CodeChanged -> {
                this.mViewState.update {
                    if (it.step != RegisterViewStep.VERIFICATION || it.isLoading || it.isResending ||
                        it.isEmailVerified || it.envelopeReplacement != EnvelopeReplacement.IDLE
                    ) it else it.copy(code = event.code, codeError = null)
                }
            }
            is RegisterViewEvent.VerifyClicked -> this.verify()
            is RegisterViewEvent.ResendCodeClicked -> {
                // TODO: Pass the password from the registration request.
                this.resendConfirmationCode(password = "")
            }
            is RegisterViewEvent.OldEnvelopeDismissed -> {
                this.mViewState.update {
                    if (it.envelopeReplacement == EnvelopeReplacement.EXITING) {
                        it.copy(
                            code = "",
                            codeError = null,
                            envelopeVersion = it.envelopeVersion + 1,
                            envelopeReplacement = EnvelopeReplacement.ENTERING
                        )
                    } else it
                }
            }
            is RegisterViewEvent.NewEnvelopeArrived -> {
                this.mViewState.update {
                    if (it.envelopeReplacement == EnvelopeReplacement.ENTERING) {
                        it.copy(envelopeReplacement = EnvelopeReplacement.IDLE)
                    } else it
                }
            }
            is RegisterViewEvent.ClubSignatureShown -> {
                this.mViewState.update {
                    if (it.step == RegisterViewStep.REGISTER_FORM && it.isRegistrationAccepted) {
                        it.copy(step = RegisterViewStep.VERIFICATION, password = "", passwordConfirmation = "")
                    } else {
                        it
                    }
                }
            }
            is RegisterViewEvent.VerificationStampShown -> {
                if (this.state.value.isEmailVerified && !verificationNavigationSent) {
                    verificationNavigationSent = true
                    this.viewModelScope.launch {
                        this@RegisterViewModel.mViewEffect.send(RegisterViewEffect.NavigateToHome)
                    }
                }
            }
            is RegisterViewEvent.BackToRegistrationForm -> {
                this.mViewState.update {
                    RegisterViewState(email = it.email, name = it.name, invalidAttempt = it.invalidAttempt)
                }
            }
        }
    }

    private fun editForm(edit: (RegisterViewState) -> RegisterViewState) {
        this.mViewState.update { current ->
            if (current.step != RegisterViewStep.REGISTER_FORM || current.isLoading ||
                current.isRegistrationAccepted
            ) return@update current

            val edited = edit(current)
            if (!edited.showFieldErrors) edited
            else edited.copy(fieldErrors = validateForm(edited))
        }
    }

    private fun validateForm(state: RegisterViewState) = this.validator.validateRegistration(
        email = state.email,
        name = state.name,
        password = state.password,
        passwordConfirmation = state.passwordConfirmation
    )

    private fun register() {
        val current = this.state.value
        if (current.step != RegisterViewStep.REGISTER_FORM || current.isLoading ||
            current.isRegistrationAccepted
        ) return

        val errors = validateForm(current)
        if (errors.isNotEmpty()) {
            this.mViewState.update {
                it.copy(
                    showFieldErrors = true,
                    fieldErrors = errors,
                    shakenFields = errors.keys,
                    invalidAttempt = it.invalidAttempt + 1
                )
            }
            return
        }

        this.mViewState.update { it.copy(isLoading = true, fieldErrors = emptyMap()) }
        this.viewModelScope.launch {
            val result = this@RegisterViewModel.accountRepository.register(
                email = current.email,
                name = current.name,
                password = current.password
            )

            result.onSuccess {
                this@RegisterViewModel.mViewState.update {
                    it.copy(isRegistrationAccepted = true, isLoading = false)
                }
            }.onFailure {
                // TODO: Remove from production-ready
                this@RegisterViewModel.mViewState.update {
                    it.copy(isRegistrationAccepted = true, isLoading = false)
                }
//                this@RegisterViewModel.mViewState.update {
//                    it.copy(isLoading = false)
//                }
//                this@RegisterViewModel.mViewEffect.send(RegisterViewEffect.ShowError("Could not register. Please try again."))
            }
        }
    }

    fun verify() {
        val current = this.state.value
        if (current.step != RegisterViewStep.VERIFICATION || current.isLoading || current.isResending ||
            current.isEmailVerified || current.envelopeReplacement != EnvelopeReplacement.IDLE ||
            current.code.length != 6
        ) return

        this.mViewState.update { it.copy(isLoading = true, codeError = null) }
        this.viewModelScope.launch {
            val result = this@RegisterViewModel.accountRepository.verifyRegistration(code = current.code)

            result.onSuccess {
                this@RegisterViewModel.mViewState.update {
                    it.copy(isLoading = false, isEmailVerified = true)
                }
            }.onFailure { failure ->
                // TODO: Remove this from production-ready
                this@RegisterViewModel.mViewState.update {
                    it.copy(isLoading = false, isEmailVerified = true)
                }
//                if (failure is RegisterException) {
//                    this@RegisterViewModel.mViewState.update {
//                        it.copy(
//                            isLoading = false,
//                            codeError = "Check the code and try again.",
//                            invalidCodeAttempt = it.invalidCodeAttempt + 1
//                        )
//                    }
//                } else {
//                    this@RegisterViewModel.mViewState.update { it.copy(isLoading = false) }
//                    this@RegisterViewModel.mViewEffect.send(
//                        RegisterViewEffect.ShowError("Could not verify the code. Please try again.")
//                    )
//                }
            }
        }
    }

    private fun resendConfirmationCode(password: String) {
        val current = this.state.value
        if (current.step != RegisterViewStep.VERIFICATION || current.isLoading || current.isResending ||
            current.isEmailVerified || current.envelopeReplacement != EnvelopeReplacement.IDLE
        ) return

        this.mViewState.update { it.copy(isResending = true) }
        this.viewModelScope.launch {
            val result = this@RegisterViewModel.accountRepository.resendConfirmationCode(
                email = current.email,
                password = password
            )

            result.onSuccess {
                this@RegisterViewModel.mViewState.update {
                    it.copy(isResending = false, envelopeReplacement = EnvelopeReplacement.EXITING)
                }
            }.onFailure {
                // TODO: Remove from production-ready
                this@RegisterViewModel.mViewState.update {
                    it.copy(isResending = false, envelopeReplacement = EnvelopeReplacement.EXITING)
                }
//                this@RegisterViewModel.mViewState.update { it.copy(isResending = false) }
//                this@RegisterViewModel.mViewEffect.send(
//                    RegisterViewEffect.ShowError("Could not send a new code. Please try again.")
//                )
            }
        }
    }
}
