package com.slouka.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slouka.app.ui.navigation.HomeRoute
import com.slouka.app.ui.navigation.LoginRoute
import com.slouka.app.ui.navigation.SloukaNavigation
import com.slouka.app.ui.startup.StartupState
import com.slouka.app.ui.startup.StartupViewModel
import com.slouka.app.ui.theme.SloukaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SloukaTheme {
                val viewModel: StartupViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                when (state) {
                    StartupState.CHECKING -> Surface(Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    StartupState.SIGNED_IN, StartupState.SIGNED_OUT -> {
                        val route = if (state == StartupState.SIGNED_IN) HomeRoute else LoginRoute
                        key(route) {
                            SloukaNavigation(
                                startDestination = route,
                                allowHome = viewModel.allowHome,
                                onHomeOpened = viewModel::onHomeOpened
                            )
                        }
                    }
                }
            }
        }
    }
}
