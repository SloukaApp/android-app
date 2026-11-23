package com.slouka.app.ui.screens.register.parts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.slouka.app.R
import com.slouka.app.ui.animations.VerificationEnvelope
import com.slouka.app.ui.animations.rememberInvalidInputFeedback
import com.slouka.app.ui.screens.register.model.EnvelopeReplacement
import com.slouka.app.ui.screens.register.model.RegisterViewEvent
import com.slouka.app.ui.screens.register.model.RegisterViewState
import com.slouka.app.ui.views.fields.OtpCodeField
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RegisterVerificationPart(
    state: RegisterViewState,
    showStamp: Boolean,
    event: (RegisterViewEvent) -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val replacement = state.envelopeReplacement
    val motion = remember(state.envelopeVersion) {
        Animatable(if (replacement == EnvelopeReplacement.ENTERING) 1f else 0f)
    }
    val stampImpression = remember(state.envelopeVersion) { Animatable(0f) }
    val dismissal = remember(state.envelopeVersion) { Animatable(0f) }
    val onEvent by rememberUpdatedState(event)
    val exitAngle = remember(state.envelopeVersion) {
        Random.nextFloat() * 4f + 4f
    } * if (state.envelopeVersion % 2 == 0) 1f else -1f
    val focusManager = LocalFocusManager.current
    val canEdit = !state.isLoading && !state.isResending && !state.isEmailVerified &&
        replacement == EnvelopeReplacement.IDLE

    LaunchedEffect(replacement) {
        when (replacement) {
            EnvelopeReplacement.EXITING -> {
                motion.animateTo(1f, tween(330, easing = FastOutSlowInEasing))
                event(RegisterViewEvent.OldEnvelopeDismissed)
            }
            EnvelopeReplacement.ENTERING -> {
                motion.animateTo(0f, tween(340, easing = FastOutSlowInEasing))
                event(RegisterViewEvent.NewEnvelopeArrived)
            }
            EnvelopeReplacement.IDLE -> Unit
        }
    }

    LaunchedEffect(showStamp, state.envelopeVersion) {
        if (!showStamp) return@LaunchedEffect

        stampImpression.animateTo(1f, tween(220))
        delay(650.milliseconds)
        dismissal.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
        onEvent(RegisterViewEvent.VerificationEnvelopeDismissed)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .then(modifier)
            .padding(bottom = innerPadding.calculateBottomPadding())
            .imePadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()
        ) {
            val travel = maxHeight + 40.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 20.dp)
            ) {
                key(state.envelopeVersion) {
                    VerificationEnvelope(
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .fillMaxWidth()
                            .graphicsLayer {
                                val dismissed = dismissal.value
                                translationY = travel.toPx() * motion.value + 24.dp.toPx() * dismissed
                                alpha = 1f - dismissed
                                if (replacement == EnvelopeReplacement.EXITING) {
                                    rotationZ = exitAngle * motion.value
                                }
                            },
                        open = replacement != EnvelopeReplacement.ENTERING
                    ) {
                        VerificationLetter(
                            state = state,
                            canEdit = canEdit,
                            showStamp = showStamp,
                            stampImpression = stampImpression.asState(),
                            event = event
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = {
                focusManager.clearFocus()
                event(RegisterViewEvent.ResendCodeClicked)
            },
            enabled = canEdit,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(stringResource(if (state.isResending) R.string.verification_resending else R.string.verification_resend))
        }
    }
}

@Composable
private fun VerificationLetter(
    state: RegisterViewState,
    canEdit: Boolean,
    showStamp: Boolean,
    stampImpression: State<Float>,
    event: (RegisterViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 22.dp)
    ) {
        Text(
            text = stringResource(R.string.verification_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (state.email.isBlank()) stringResource(R.string.verification_enter_code)
            else stringResource(R.string.verification_enter_code_for_email, state.email),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        BoxWithConstraints {
            val cellSize = ((maxWidth - 40.dp) / 6).coerceIn(1.dp, 48.dp)
            OtpCodeField(
                otpText = state.code,
                onOtpTextChange = { event(RegisterViewEvent.CodeChanged(it)) },
                onOtpComplete = { event(RegisterViewEvent.VerifyClicked) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                enabled = canEdit,
                cellSize = cellSize,
                animateCells = false,
                isError = state.codeError != null,
                modifier = Modifier.rememberInvalidInputFeedback(
                    if (state.codeError != null) state.invalidCodeAttempt else 0
                )
            )
        }

        Box(Modifier.heightIn(min = 20.dp), contentAlignment = Alignment.Center) {
            if (showStamp) {
                VerificationStamp(impression = stampImpression)
            } else {
                state.codeError?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationStamp(impression: State<Float>) {
    val ink = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(4.dp)

    Text(
        text = stringResource(R.string.verification_stamp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = ink,
        modifier = Modifier
            .semantics { liveRegion = LiveRegionMode.Polite }
            .graphicsLayer {
                alpha = impression.value
                scaleX = lerp(1.35f, 1f, impression.value)
                scaleY = scaleX
                rotationZ = -8f
            }
            .border(2.dp, ink, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
