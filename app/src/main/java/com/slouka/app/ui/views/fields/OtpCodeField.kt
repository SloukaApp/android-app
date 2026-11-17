package com.slouka.app.ui.views.fields

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OtpCodeField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    onOtpComplete: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    otpCount: Int = 6,
    enabled: Boolean = true,
    cellSize: Dp = 48.dp,
    animateCells: Boolean = true,
    isError: Boolean = false,
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        modifier = modifier
            .onFocusChanged { focusState ->
                isTextFieldFocused = focusState.isFocused
            },
        value = otpText,
        enabled = enabled,
        onValueChange = {
            val filteredText = it.filter { char ->
                char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9'
            }

            if (filteredText != otpText && filteredText.length <= otpCount) {
                onOtpTextChange(filteredText)
                if (filteredText.length == otpCount) {
                    keyboardController?.hide()
                    onOtpComplete(filteredText)
                }
            }
        },
        keyboardOptions = keyboardOptions,
        maxLines = 1,
        decorationBox = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(otpCount) { index ->
                        CharView(
                            index = index,
                            text = otpText,
                            cellSize = cellSize,
                            animate = animateCells,
                            isError = isError,
                            isTextFieldFocused = isTextFieldFocused
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun CharView(
    index: Int,
    text: String,
    isTextFieldFocused: Boolean,
    cellSize: Dp,
    animate: Boolean,
    isError: Boolean
) {
    val appearAnim = remember(animate) { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(animate) {
        if (!animate) return@LaunchedEffect
        delay((index * 100L).milliseconds)
        appearAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val char = when {
        index >= text.length -> ""
        else -> text[index].toString()
    }

    val isFocused = text.length == index && isTextFieldFocused
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 250),
        label = "border_color"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 250),
        label = "border_width"
    )

    Box(
        modifier = Modifier
            .size(cellSize)
            .graphicsLayer {
                alpha = appearAnim.value.coerceIn(0f, 1f)
                scaleX = appearAnim.value
                scaleY = appearAnim.value
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
