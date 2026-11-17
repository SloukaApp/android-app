package com.slouka.app.ui.screens.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.ui.screens.register.model.RegisterViewEffect
import com.slouka.app.ui.screens.register.model.RegisterViewEvent
import com.slouka.app.ui.screens.register.model.RegisterViewModel
import com.slouka.app.ui.screens.register.model.RegisterViewState
import com.slouka.app.ui.screens.register.model.RegisterViewStep
import com.slouka.app.ui.screens.register.parts.RegisterFormPart
import com.slouka.app.ui.screens.register.parts.RegisterVerificationPart
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.bars.ProgressTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.RegisterScreen(
    state: RegisterViewState,
    event: (RegisterViewEvent) -> Unit,
    onNavigateBack: () -> Unit,
    visibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var progressIsIndeterminate by remember { mutableStateOf(false) }
    var showTopBarIcons by rememberSaveable { mutableStateOf(false) }

    val cornerRadius by visibilityScope.transition.animateDp(
        transitionSpec = { tween(500) },
        label = "corner_radius"
    ) { enterExit ->
        when (enterExit) {
            EnterExitState.PreEnter -> 24.dp
            EnterExitState.Visible -> 0.dp
            EnterExitState.PostExit -> 24.dp
        }
    }

    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        showTopBarIcons = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "register_container"),
                animatedVisibilityScope = visibilityScope,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
                boundsTransform = { _, _ -> tween(500) },
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(cornerRadius))
            ),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .imePadding()
            ) { snackbarData ->
                Snackbar(
                    shape = RoundedCornerShape(6.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(0.dp),
                    action = snackbarData.visuals.actionLabel?.let { actionLabel -> {
                            TextButton(onClick = { snackbarData.performAction() }) {
                                Text(
                                    text = actionLabel,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        topBar = {
            ProgressTopAppBar(
                showBackground = showTopBarIcons,
                visibilityScope = visibilityScope,
                backgroundEnterDelayMillis = 800,
                sharedTransitionScope = this,
                title = "Register",
                backgroundIconSize = 48.dp,
                onProgressIndeterminateChanged = { progressIsIndeterminate = it },
                showProgress = state.isLoading || state.isRegistrationAccepted ||
                    state.step == RegisterViewStep.VERIFICATION,
                steps = intArrayOf(
                    if (state.isRegistrationAccepted || state.step == RegisterViewStep.VERIFICATION) 100 else -1,
                    when {
                        state.isEmailVerified -> 100
                        state.step == RegisterViewStep.VERIFICATION && (state.isLoading || state.isResending) -> -1
                        else -> 0
                    }
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            label = "register_steps_animation",
            transitionSpec = {
                val diff = if (targetState - initialState > 0) 1 else -1
                slideInHorizontally(tween(400)) { width -> (diff * width) } + fadeIn() togetherWith
                        (slideOutHorizontally(tween(400)) { width -> (diff * -1 * width) } + fadeOut())
            }
        ) { currentStep ->
            when (currentStep) {
                RegisterViewStep.REGISTER_FORM -> {
                    RegisterFormPart(
                        state = state,
                        showSignature = state.isRegistrationAccepted && !progressIsIndeterminate,
                        event = event,
                        innerPadding = innerPadding
                    )
                }
                RegisterViewStep.VERIFICATION -> {
                    RegisterVerificationPart(
                        state = state,
                        showStamp = state.isEmailVerified && !progressIsIndeterminate,
                        event = event,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

@Composable
fun SharedTransitionScope.RegisterScreenRoot(
    visibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is RegisterViewEffect.ShowError -> this@repeatOnLifecycle.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is RegisterViewEffect.NavigateToHome -> onNavigateToHome()
                }
            }
        }
    }

    RegisterScreen(
        state = state,
        visibilityScope = visibilityScope,
        event = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    SloukaTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                RegisterScreen(
                    state = RegisterViewState(
                        email = "example@gmail.com",
                        name = "God",
                        isLoading = false
                    ),
                    event = {},
                    onNavigateBack = {},
                    visibilityScope = this,
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
        }
    }
}
