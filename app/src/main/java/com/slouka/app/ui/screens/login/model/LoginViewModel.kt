package com.slouka.app.ui.screens.login.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.LoginException
import com.slouka.app.account.validation.AuthFormValidator
import com.slouka.app.account.validation.FormField
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val validator: AuthFormValidator
) : ViewModel() {
    private val mViewEffect = Channel<LoginViewEffect>(Channel.BUFFERED)
    private val mViewState = MutableStateFlow(LoginViewState())

    val effect = this.mViewEffect.receiveAsFlow()
    val state = this.mViewState.asStateFlow()

    fun onEvent(event: LoginViewEvent) {
        when (event) {
            is LoginViewEvent.EmailChanged -> this.editForm { it.copy(email = event.email) }
            is LoginViewEvent.PasswordChanged -> this.editForm { it.copy(password = event.password) }
            is LoginViewEvent.LoginClicked -> this.login()
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
                    it.copy(isLoading = false, isLoginSuccessful = true, password = "")
                }
                this@LoginViewModel.mViewEffect.send(LoginViewEffect.NavigateToHome)
            }.onFailure { failure ->
                val message = if (failure is LoginException || (failure is HttpException && failure.code() == 401)) {
                    this@LoginViewModel.mViewState.update {
                        it.copy(
                            isLoading = false,
                            shakenFields = setOf(FormField.EMAIL, FormField.PASSWORD),
                            invalidAttempt = it.invalidAttempt + 1
                        )
                    }
                    "Email or password is incorrect. Please try again."
                } else {
                    this@LoginViewModel.mViewState.update { it.copy(isLoading = false) }
                    "Could not log in. Please try again."
                }
                this@LoginViewModel.mViewEffect.send(LoginViewEffect.ShowError(message))
            }
        }
    }
}
