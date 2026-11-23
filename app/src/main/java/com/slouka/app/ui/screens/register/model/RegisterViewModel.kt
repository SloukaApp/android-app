package com.slouka.app.ui.screens.register.model

import androidx.annotation.StringRes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.R
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.account.validation.AuthFormValidator
import com.slouka.app.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
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
            is RegisterViewEvent.PasswordChanged -> this.editForm { it.copy(password = event.password) }
            is RegisterViewEvent.PasswordConfirmationChanged -> this.editForm {
                it.copy(passwordConfirmation = event.password)
            }
            is RegisterViewEvent.ConsentChanged -> this.editForm {
                it.copy(isConsentActive = event.isConsentActive)
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
            is RegisterViewEvent.VerificationEnvelopeDismissed -> {
                if (this.state.value.isEmailVerified && !verificationNavigationSent) {
                    verificationNavigationSent = true
                    this.viewModelScope.launch {
                        this@RegisterViewModel.mViewEffect.send(RegisterViewEffect.NavigateToHome)
                    }
                }
            }
            is RegisterViewEvent.BackToRegistrationForm -> {
                this.mViewState.update {
                    RegisterViewState(email = it.email, invalidAttempt = it.invalidAttempt)
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
        password = state.password,
        passwordConfirmation = state.passwordConfirmation,
        isConsentActive = state.isConsentActive
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
                password = current.password,
                isConsentActive = current.isConsentActive
            )

            result.onSuccess {
                this@RegisterViewModel.mViewState.update {
                    it.copy(isRegistrationAccepted = true, isLoading = false)
                }
            }.onFailure { failure ->
                this@RegisterViewModel.mViewState.update {
                    it.copy(isLoading = false)
                }
                this@RegisterViewModel.showRequestError(failure, R.string.register_error_request)
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
            val result = this@RegisterViewModel.accountRepository.verifyRegistration(
                email = current.email,
                code = current.code
            )

            result.onSuccess {
                this@RegisterViewModel.mViewState.update {
                    it.copy(isLoading = false, isEmailVerified = true)
                }
            }.onFailure { failure ->
                if (failure is AccountRequestException && failure.statusCode == HTTP_UNAUTHORIZED) {
                    this@RegisterViewModel.mViewState.update {
                        it.copy(
                            isLoading = false,
                            codeError = R.string.verification_error_invalid_code,
                            invalidCodeAttempt = it.invalidCodeAttempt + 1
                        )
                    }
                } else {
                    this@RegisterViewModel.mViewState.update { it.copy(isLoading = false) }
                    this@RegisterViewModel.showRequestError(failure, R.string.verification_error_request)
                }
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
            }.onFailure { failure ->
                this@RegisterViewModel.mViewState.update { it.copy(isResending = false) }
                this@RegisterViewModel.showRequestError(failure, R.string.verification_error_resend)
            }
        }
    }

    private suspend fun showRequestError(failure: Throwable, @StringRes fallback: Int) {
        val message = (failure as? AccountRequestException)?.detailMessage?.let { UiText.Raw(it) }
            ?: UiText.Resource(fallback)
        this.mViewEffect.send(RegisterViewEffect.ShowError(message))
    }
}
