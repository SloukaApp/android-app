package com.slouka.app.ui.screens.home.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slouka.app.ui.theme.SloukaTheme

@Composable
internal fun AccountPageView(
    onSessionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isLoggingOut: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val canClick = enabled && !isLoggingOut
    Column(modifier) {
        ListItem(
            headlineContent = { Text("Sessions") },
            leadingContent = { Icon(Icons.Rounded.Devices, contentDescription = null) },
            modifier = Modifier.clickable(enabled = canClick, onClick = onSessionsClick)
        )
        ListItem(
            headlineContent = { Text("Logout") },
            leadingContent = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
            trailingContent = {
                if (isLoggingOut) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            modifier = Modifier.clickable(enabled = canClick, onClick = onLogoutClick)
        )
    }
}

@Preview(name = "Account page", showBackground = true)
@Composable
private fun AccountPageViewPreview() {
    SloukaTheme {
        AccountPageView(onSessionsClick = {}, onLogoutClick = {}, isLoggingOut = false)
    }
}
