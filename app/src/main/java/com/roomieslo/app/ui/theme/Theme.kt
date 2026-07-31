package com.roomieslo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RoomieSloPrimary = Color(0xFF2F6F5E)
private val RoomieSloSecondary = Color(0xFFE7B75F)

private val LightColors = lightColorScheme(
    primary = RoomieSloPrimary,
    secondary = RoomieSloSecondary,
    background = Color(0xFFFAFAF7),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FBCA8),
    secondary = RoomieSloSecondary
)

@Composable
fun RoomieSloTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
