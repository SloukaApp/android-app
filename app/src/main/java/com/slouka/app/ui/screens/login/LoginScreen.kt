package com.slouka.app.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.account.validation.FormField
import com.slouka.app.ui.animations.rememberInvalidInputFeedback
import com.slouka.app.ui.screens.login.model.LoginViewEffect
import com.slouka.app.ui.screens.login.model.LoginViewEvent
import com.slouka.app.ui.screens.login.model.LoginViewModel
import com.slouka.app.ui.screens.login.model.LoginViewState
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.bars.ProgressTopAppBar
import com.slouka.app.ui.views.buttons.AnimatedBottomButton
import com.slouka.app.ui.views.dividers.TextDivider
import com.slouka.app.ui.views.fields.PasswordField
import com.slouka.app.ui.views.snackbars.ErrorSnackbarHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.LoginScreen(
    state: LoginViewState,
    event: (LoginViewEvent) -> Unit,
    onRegisterClick: () -> Unit,
    visibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onHomeShortcut: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val focusManager = LocalFocusManager.current
    val feedback = Modifier.rememberInvalidInputFeedback(state.invalidAttempt)
    val emailError = state.fieldErrors[FormField.EMAIL]
    val emailFeedback = if (FormField.EMAIL in state.shakenFields) feedback else Modifier
    val passwordFeedback = if (FormField.PASSWORD in state.shakenFields) feedback else Modifier
    var clubCardVisibility by rememberSaveable { mutableStateOf(false) }
    var loginButtonVisibility by rememberSaveable { mutableStateOf(false) }
    var showTopBarBackground by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        clubCardVisibility = true
        delay(400.milliseconds)
        showTopBarBackground = true
        delay(600.milliseconds)
        loginButtonVisibility = true
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { ErrorSnackbarHost(snackbarHostState) },
        topBar = {
            ProgressTopAppBar(
                showBackground = showTopBarBackground,
                visibilityScope = visibilityScope,
                backgroundEnterDelayMillis = 800,
                sharedTransitionScope = this,
                title = "Log in",
                showProgress = state.isLoading,
                steps = IntArray(1) { -1 },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
        ) {
            Text(
                text = "Use your Slouka account to learn words. Login with it in form below!",
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            AnimatedVisibility(
                visible = clubCardVisibility,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(all = 10.dp)) {
                        Text(
                            text = "Club card",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .combinedClickable(onClick = {}, onLongClick = onHomeShortcut)
                                .alpha(0.5f)
                                .padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { event(LoginViewEvent.EmailChanged(it)) },
                            label = { Text("Email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            enabled = !state.isLoading,
                            leadingIcon = { Icon(Icons.Rounded.Email, null) },
                            isError = emailError != null,
                            supportingText = emailError?.let { message -> { Text(message) } },
                            modifier = emailFeedback
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        )

                        PasswordField(
                            value = state.password,
                            onValueChange = { event(LoginViewEvent.PasswordChanged(it)) },
                            label = "Password",
                            enabled = !state.isLoading,
                            imeAction = ImeAction.Done,
                            error = state.fieldErrors[FormField.PASSWORD],
                            modifier = passwordFeedback.fillMaxWidth()
                        )
                    }
                }
            }

            TextDivider(
                text = "OR",
                thickness = 4.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "register_container"),
                        animatedVisibilityScope = visibilityScope,
                        enter = fadeIn(tween(500)),
                        exit = fadeOut(tween(500)),
                        boundsTransform = { _, _ -> tween(500) },
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(50))
                    ),
                enabled = !state.isLoading
            ) {
                Text("Create an account")
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedBottomButton(
                visible = loginButtonVisibility && !state.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    event(LoginViewEvent.LoginClicked)
                },
                text = "Log in",
                enabled = !state.isLoading,
                bottomPadding = innerPadding.calculateBottomPadding()
            )
        }
    }
}

@Composable
fun SharedTransitionScope.LoginScreenRoot(
    visibilityScope: AnimatedVisibilityScope,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is LoginViewEffect.ShowError -> this@repeatOnLifecycle.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is LoginViewEffect.NavigateToHome -> onNavigateToHome()
                }
            }
        }
    }

    LoginScreen(
        state = state,
        event = viewModel::onEvent,
        onRegisterClick = onNavigateToRegister,
        visibilityScope = visibilityScope,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onHomeShortcut = onNavigateToHome
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SloukaTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                LoginScreen(
                    state = LoginViewState(
                        email = "example@gmail.com",
                        isLoading = false
                    ),
                    event = {},
                    onRegisterClick = {},
                    visibilityScope = this,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}
