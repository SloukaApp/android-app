package com.slouka.app.ui.screens.login.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.R
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.account.validation.AuthFormValidator
import com.slouka.app.account.validation.FormField
import com.slouka.app.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val validator: AuthFormValidator
) : ViewModel() {
    private val mViewEffect = Channel<LoginViewEffect>(Channel.BUFFERED)
    private val mViewState = MutableStateFlow(LoginViewState())
    private var mNavigationSent = false

    val effect = this.mViewEffect.receiveAsFlow()
    val state = this.mViewState.asStateFlow()

    fun onEvent(event: LoginViewEvent) {
        when (event) {
            is LoginViewEvent.EmailChanged -> this.editForm { it.copy(email = event.email) }
            is LoginViewEvent.PasswordChanged -> this.editForm { it.copy(password = event.password) }
            is LoginViewEvent.LoginClicked -> this.login()
            is LoginViewEvent.SuccessAnimationFinished -> this.openHome()
        }
    }

    private fun editForm(edit: (LoginViewState) -> LoginViewState) {
        this.mViewState.update { current ->
            if (current.isLoading || current.isLoginSuccessful) return@update current

            val edited = edit(current)
            if (edited.invalidAttempt == 0) edited
            else edited.copy(fieldErrors = validator.validateLogin(edited.email, edited.password))
        }
    }

    private fun login() {
        val current = this.state.value
        if (current.isLoading || current.isLoginSuccessful) return

        val errors = validator.validateLogin(current.email, current.password)
        if (errors.isNotEmpty()) {
            this.mViewState.update {
                it.copy(fieldErrors = errors, shakenFields = errors.keys, invalidAttempt = it.invalidAttempt + 1)
            }
            return
        }

        this.mViewState.update { it.copy(isLoading = true, fieldErrors = emptyMap()) }
        this.viewModelScope.launch {
            val result = this@LoginViewModel.accountRepository.login(
                email = current.email,
                password = current.password
            )

            result.onSuccess {
                this@LoginViewModel.mViewState.update {
                    it.copy(isLoading = false, isLoginSuccessful = true)
                }
            }.onFailure { failure ->
                val requestFailure = failure as? AccountRequestException
                val message = if (requestFailure?.statusCode == HTTP_UNAUTHORIZED) {
                    this@LoginViewModel.mViewState.update {
                        it.copy(
                            isLoading = false,
                            shakenFields = setOf(FormField.EMAIL, FormField.PASSWORD),
                            invalidAttempt = it.invalidAttempt + 1
                        )
                    }
                    UiText.Resource(R.string.login_error_invalid_credentials)
                } else {
                    this@LoginViewModel.mViewState.update { it.copy(isLoading = false) }
                    requestFailure?.detailMessage?.let { UiText.Raw(it) }
                        ?: UiText.Resource(R.string.login_error_request)
                }
                this@LoginViewModel.mViewEffect.send(LoginViewEffect.ShowError(message))
            }
        }
    }

    private fun openHome() {
        if (!this.state.value.isLoginSuccessful || this.mNavigationSent) return
        this.mNavigationSent = true
        this.mViewState.update { it.copy(password = "") }
        this.viewModelScope.launch {
            this@LoginViewModel.mViewEffect.send(LoginViewEffect.NavigateToHome)
        }
    }
}
