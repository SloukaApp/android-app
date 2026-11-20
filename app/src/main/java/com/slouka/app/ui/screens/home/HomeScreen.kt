package com.slouka.app.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(onSessionsClick: () -> Unit = {}) {
    Scaffold { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Text("This is home!")
            Button(onClick = onSessionsClick) { Text("Sessions") }
        }
    }
}

@Composable
fun HomeScreenRoot(onSessionsClick: () -> Unit = {}) {
    HomeScreen(onSessionsClick)
}
