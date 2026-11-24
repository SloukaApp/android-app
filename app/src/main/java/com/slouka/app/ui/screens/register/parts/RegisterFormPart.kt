package com.slouka.app.ui.screens.register.parts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slouka.app.R
import com.slouka.app.ui.animations.UnrollCardAnimation
import com.slouka.app.ui.animations.rememberInvalidInputFeedback
import com.slouka.app.account.validation.FormField
import com.slouka.app.ui.screens.register.model.RegisterViewEvent
import com.slouka.app.ui.screens.register.model.RegisterViewState
import com.slouka.app.ui.views.buttons.AnimatedBottomButton
import com.slouka.app.ui.views.decos.ClubSignatureLine
import com.slouka.app.ui.views.dividers.TextDivider
import com.slouka.app.ui.views.fields.PasswordField
import com.slouka.app.ui.views.forms.AuthFormLayout
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
    val feedback = Modifier.rememberInvalidInputFeedback(state.invalidAttempt)

    var cardVisibility by rememberSaveable { mutableStateOf(false) }
    var registerButtonVisibility by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        cardVisibility = true

        delay(600.milliseconds)
        registerButtonVisibility = true
    }

    AuthFormLayout(
        contentPadding = innerPadding,
        description = stringResource(R.string.register_description),
        bottomAction = {
            AnimatedBottomButton(
                visible = registerButtonVisibility && canEdit,
                onClick = {
                    focusManager.clearFocus()
                    event(RegisterViewEvent.RegisterClicked)
                },
                text = stringResource(R.string.register_title),
                enabled = canEdit
            )
        }
    ) {
        UnrollCardAnimation(
            visible = cardVisibility,
            modifier = Modifier.fillMaxWidth(),
            rollInset = 10.dp
        ) {
            RegistrationFormCard(
                state = state,
                showSignature = showSignature,
                canEdit = canEdit,
                errors = state.fieldErrors,
                fieldFeedback = { field -> if (field in state.shakenFields) feedback else Modifier },
                onEvent = event
            )
        }
    }
}

@Composable
private fun RegistrationFormCard(
    state: RegisterViewState,
    showSignature: Boolean,
    canEdit: Boolean,
    errors: Map<FormField, Int>,
    fieldFeedback: (FormField) -> Modifier,
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
                text = stringResource(R.string.register_form_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterViewEvent.EmailChanged(it)) },
                label = { Text(stringResource(R.string.auth_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = errors[FormField.EMAIL] != null,
                supportingText = errors[FormField.EMAIL]?.let { message -> { Text(stringResource(message)) } },
                enabled = canEdit,
                leadingIcon = { Icon(Icons.Rounded.Email, null) },
                modifier = fieldFeedback(FormField.EMAIL)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            PasswordField(
                value = state.password,
                onValueChange = { onEvent(RegisterViewEvent.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password),
                error = errors[FormField.PASSWORD]?.let { stringResource(it) },
                enabled = canEdit,
                imeAction = ImeAction.Next,
                modifier = fieldFeedback(FormField.PASSWORD)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            PasswordField(
                value = state.passwordConfirmation,
                onValueChange = { onEvent(RegisterViewEvent.PasswordConfirmationChanged(it)) },
                label = stringResource(R.string.register_confirm_password),
                error = errors[FormField.CONFIRMATION]?.let { stringResource(it) },
                enabled = canEdit,
                imeAction = ImeAction.Done,
                modifier = fieldFeedback(FormField.CONFIRMATION).fillMaxWidth()
            )

            TextDivider(text = stringResource(R.string.register_divider_and), thickness = 4.dp, modifier = Modifier.padding(top = 6.dp))

            Column(modifier = fieldFeedback(FormField.CONSENT).fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(
                            value = state.isConsentActive,
                            enabled = canEdit,
                            role = Role.Checkbox,
                            onValueChange = { onEvent(RegisterViewEvent.ConsentChanged(it)) }
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Checkbox(
                        checked = state.isConsentActive,
                        onCheckedChange = null,
                        enabled = canEdit
                    )
                    Text(
                        text = stringResource(R.string.register_consent),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                errors[FormField.CONSENT]?.let { message ->
                    Text(
                        text = stringResource(message),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

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
