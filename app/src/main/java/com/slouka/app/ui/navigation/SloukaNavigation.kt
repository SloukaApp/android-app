package com.slouka.app.ui.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slouka.app.ui.screens.home.HomeScreenRoot
import com.slouka.app.ui.screens.sessions.SessionsScreenRoot
import com.slouka.app.ui.screens.login.LoginScreenRoot
import com.slouka.app.ui.screens.register.RegisterScreenRoot
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

@Composable
fun SloukaNavigation() {
    val controller = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = controller,
            startDestination = LoginRoute
        ) {
            composable<LoginRoute>(
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { ExitTransition.KeepUntilTransitionsFinished },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { fadeOut(tween(500)) }
            ) {
                LoginScreenRoot(
                    visibilityScope = this,
                    onNavigateToRegister = {
                        controller.navigate(RegisterRoute)
                    },
                    onNavigateToHome = {
                        controller.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<RegisterRoute>(
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { fadeOut(tween(500)) },
                popEnterTransition = { fadeIn(tween(500)) },
                popExitTransition = { fadeOut(tween(500)) }
            ) {
                RegisterScreenRoot(
                    visibilityScope = this,
                    onNavigateBack = { controller.popBackStack() },
                    onNavigateToHome = {
                        controller.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<HomeRoute> {
                HomeScreenRoot(onSessionsClick = { controller.navigate(SessionsRoute) })
            }

            composable<SessionsRoute> {
                SessionsScreenRoot(onBack = { controller.popBackStack() })
            }
        }
    }
}
