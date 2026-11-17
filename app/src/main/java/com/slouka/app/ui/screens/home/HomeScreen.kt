package com.slouka.app.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.slouka.app.ui.views.bars.ProgressTopAppBar

@Composable
fun HomeScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "This is home!",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HomeScreenRoot() {
    HomeScreen()
}
