package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmScrollbookColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    secondary = SecondaryBrown,
    tertiary = TertiaryBrown,
    background = SoftCreamBg,
    surface = PaperPageColor,
    onPrimary = Color.White,
    onSecondary = PencilGraphite,
    onTertiary = PencilGraphite,
    onBackground = PencilGraphite,
    onSurface = PencilGraphite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Always enforce cute warm sketchbook theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WarmScrollbookColorScheme,
        typography = Typography,
        content = content
    )
}
