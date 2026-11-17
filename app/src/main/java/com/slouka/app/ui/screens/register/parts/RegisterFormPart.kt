package com.slouka.app.ui.screens.register.parts

import android.util.Patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slouka.app.ui.animations.UnrollCardAnimation
import com.slouka.app.ui.animations.rememberInvalidInputFeedback
import com.slouka.app.ui.screens.register.model.FormField
import com.slouka.app.ui.screens.register.model.RegisterViewEvent
import com.slouka.app.ui.screens.register.model.RegisterViewState
import com.slouka.app.ui.views.buttons.AnimatedBottomButton
import com.slouka.app.ui.views.decos.ClubSignatureLine
import com.slouka.app.ui.views.dividers.TextDivider
import com.slouka.app.ui.views.fields.PasswordField
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RegisterFormPart(
    state: RegisterViewState,
    showSignature: Boolean,
    event: (RegisterViewEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    val focusManager = LocalFocusManager.current
    val canEdit = !state.isLoading && !state.isRegistrationAccepted
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }

    var submitted by rememberSaveable { mutableStateOf(false) }
    var invalidAttempt by rememberSaveable { mutableIntStateOf(0) }
    var shakenFields by remember { mutableStateOf(emptySet<FormField>()) }
    val feedback = rememberInvalidInputFeedback(invalidAttempt)
    val errors = buildMap {
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            put(FormField.EMAIL, "Enter a valid email address.")
        }
        if (state.name.isBlank()) put(FormField.NAME, "Enter your name.")
        if (password.isBlank()) put(FormField.PASSWORD, "Enter a password.")
        if (password != passwordConfirmation) {
            put(FormField.CONFIRMATION, "Passwords must match.")
        } else if (passwordConfirmation.isBlank()) {
            put(FormField.CONFIRMATION, "Confirm your password.")
        }
    }

    var cardVisibility by rememberSaveable { mutableStateOf(false) }
    var registerButtonVisibility by rememberSaveable { mutableStateOf(false) }
    var buttonEntered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        cardVisibility = true

        delay(600.milliseconds)
        registerButtonVisibility = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .imePadding()
    ) {
        Text(
            text = "Create new account just in 2 steps! Fill form below...",
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )

        Box(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            UnrollCardAnimation(
                visible = cardVisibility,
                modifier = Modifier.fillMaxWidth(),
                rollInset = 10.dp
            ) {
                RegistrationFormCard(
                    state = state,
                    showSignature = showSignature,
                    canEdit = canEdit,
                    errors = if (submitted) errors else emptyMap(),
                    fieldFeedback = { field -> if (field in shakenFields) feedback else Modifier },
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                    onPasswordChange = { password = it },
                    onPasswordConfirmationChange = { passwordConfirmation = it },
                    onEvent = event
                )
            }
        }

        AnimatedBottomButton(
            onEnterFinished = { buttonEntered = true },
            visible = registerButtonVisibility && canEdit,
            onClick = {
                submitted = true
                if (errors.isEmpty()) {
                    focusManager.clearFocus()
                    event(RegisterViewEvent.RegisterClicked(password))
                } else {
                    shakenFields = errors.keys
                    invalidAttempt++
                }
            },
            text = "Register",
            enabled = canEdit,
            bottomPadding = innerPadding.calculateBottomPadding()
        )
    }
}

@Composable
private fun RegistrationFormCard(
    state: RegisterViewState,
    showSignature: Boolean,
    canEdit: Boolean,
    errors: Map<FormField, String>,
    fieldFeedback: (FormField) -> Modifier,
    password: String,
    passwordConfirmation: String,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onEvent: (RegisterViewEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(all = 10.dp)
        ) {
            Text(
                text = "Form",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterViewEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = errors[FormField.EMAIL] != null,
                supportingText = errors[FormField.EMAIL]?.let { message -> { Text(message) } },
                enabled = canEdit,
                leadingIcon = { Icon(Icons.Rounded.Email, null) },
                modifier = fieldFeedback(FormField.EMAIL)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(RegisterViewEvent.NameChanged(it)) },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = errors[FormField.NAME] != null,
                supportingText = errors[FormField.NAME]?.let { message -> { Text(message) } },
                enabled = canEdit,
                leadingIcon = { Icon(Icons.Rounded.Person, null) },
                modifier = fieldFeedback(FormField.NAME)
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            TextDivider(text = "AND", thickness = 4.dp, modifier = Modifier.padding(bottom = 6.dp))

            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                error = errors[FormField.PASSWORD],
                enabled = canEdit,
                imeAction = ImeAction.Next,
                modifier = fieldFeedback(FormField.PASSWORD)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            PasswordField(
                value = passwordConfirmation,
                onValueChange = onPasswordConfirmationChange,
                label = "Again password",
                error = errors[FormField.CONFIRMATION],
                enabled = canEdit,
                imeAction = ImeAction.Done,
                modifier = fieldFeedback(FormField.CONFIRMATION).fillMaxWidth()
            )
            ClubSignatureLine(
                showSignature = showSignature,
                onFinished = { onEvent(RegisterViewEvent.ClubSignatureShown) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp, end = 8.dp, bottom = 4.dp)
            )
        }
    }
}
