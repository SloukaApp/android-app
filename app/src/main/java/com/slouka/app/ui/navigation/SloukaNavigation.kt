package com.slouka.app.ui.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import com.slouka.app.ui.screens.home.HomeScreenRoot
import com.slouka.app.ui.screens.sessions.SessionsScreenRoot
import com.slouka.app.ui.screens.login.LoginScreenRoot
import com.slouka.app.ui.screens.register.RegisterScreenRoot
import com.slouka.app.ui.animations.AuthPanelTransitionMillis
import com.slouka.app.ui.views.decos.memberAvatarSharedElement
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun SloukaNavigation(
    startDestination: Any,
    allowHome: Boolean,
    onHomeOpened: () -> Unit
) {
    val controller = rememberNavController()
    val homeAllowed = rememberUpdatedState(allowHome)
    var playMemberAvatarTransition by remember { mutableStateOf(false) }
    var closeLoginPanels by remember { mutableStateOf(false) }
    var revealHomeOnEnter by rememberSaveable { mutableStateOf(startDestination == HomeRoute) }
    val openHome = { fromClubCard: Boolean ->
        playMemberAvatarTransition = fromClubCard
        revealHomeOnEnter = true
        onHomeOpened()
        controller.navigate(HomeRoute) {
            popUpTo(LoginRoute) { inclusive = true }
        }
    }
    val returnToLogin = {
        val currentDestination = controller.currentDestination
        if (currentDestination?.hasRoute<LoginRoute>() != true) {
            closeLoginPanels = currentDestination?.hasRoute<HomeRoute>() == true
            playMemberAvatarTransition = false
            controller.navigate(LoginRoute) {
                popUpTo(controller.graph.id)
                launchSingleTop = true
            }
        }
    }

    SharedTransitionLayout {
        NavHost(
            navController = controller,
            startDestination = startDestination
        ) {
            composable<LoginRoute>(
                enterTransition = {
                    if (closeLoginPanels && initialState.destination.hasRoute<HomeRoute>()) {
                        EnterTransition.None
                    } else {
                        fadeIn(tween(500))
                    }
                },
                exitTransition = { ExitTransition.KeepUntilTransitionsFinished },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { fadeOut(tween(500)) }
            ) {
                val entered = transition.currentState == EnterExitState.Visible
                LaunchedEffect(entered) {
                    if (entered) closeLoginPanels = false
                }
                LoginScreenRoot(
                    visibilityScope = this,
                    onNavigateToRegister = {
                        controller.navigate(RegisterRoute)
                    },
                    onNavigateToHome = { openHome(true) },
                    onHomeShortcut = { openHome(false) },
                    closePanelsOnEnter = closeLoginPanels
                )
            }

            composable<RegisterRoute>(
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = {
                    if (targetState.destination.hasRoute<HomeRoute>()) {
                        ExitTransition.KeepUntilTransitionsFinished
                    } else {
                        fadeOut(tween(500))
                    }
                },
                popEnterTransition = { fadeIn(tween(500)) },
                popExitTransition = { fadeOut(tween(500)) }
            ) {
                RegisterScreenRoot(
                    visibilityScope = this,
                    onNavigateBack = { controller.popBackStack() },
                    onNavigateToHome = { openHome(false) }
                )
            }

            composable<HomeRoute>(
                enterTransition = {
                    if (initialState.destination.hasRoute<LoginRoute>() ||
                        initialState.destination.hasRoute<RegisterRoute>()
                    ) {
                        fadeIn(tween(AuthPanelTransitionMillis))
                    } else null
                },
                exitTransition = {
                    if (closeLoginPanels && targetState.destination.hasRoute<LoginRoute>()) {
                        ExitTransition.KeepUntilTransitionsFinished
                    } else null
                }
            ) {
                HomeDestination(homeAllowed.value, returnToLogin, keepContentOnExit = true) {
                    val animateOnEnter = remember { revealHomeOnEnter }
                    val keepAvatarInPlace = remember { playMemberAvatarTransition }
                    val finished = transition.currentState == EnterExitState.Visible && !isTransitionActive
                    LaunchedEffect(finished) {
                        if (finished) {
                            playMemberAvatarTransition = false
                            revealHomeOnEnter = false
                        }
                    }
                    HomeScreenRoot(
                        onSessionsClick = { controller.navigate(SessionsRoute) },
                        avatarModifier = memberAvatarSharedElement(this),
                        showPageContent = !animateOnEnter || finished,
                        isInteractive = homeAllowed.value,
                        animateOnEnter = animateOnEnter,
                        keepAvatarInPlace = keepAvatarInPlace
                    )
                }
            }

            composable<SessionsRoute> {
                HomeDestination(homeAllowed.value, returnToLogin) {
                    SessionsScreenRoot(onBack = { controller.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun HomeDestination(
    allowed: Boolean,
    onSessionMissing: () -> Unit,
    keepContentOnExit: Boolean = false,
    content: @Composable () -> Unit
) {
    var wasAllowed by remember { mutableStateOf(allowed) }

    LaunchedEffect(allowed) {
        if (allowed) wasAllowed = true else onSessionMissing()
    }

    if (allowed || (keepContentOnExit && wasAllowed)) content()
}
