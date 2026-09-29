package com.example.bille.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BilleGreen = Color(0xFF00A859)
val BilleYellow = Color(0xFFFFC107)
val BilleRed = Color(0xFFE53935)
val BilleTextGray = Color(0xFF6B7280)

private val BilleColors = lightColorScheme(
    primary = BilleGreen,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    error = BilleRed
)

@Composable
fun BilleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BilleColors, content = content)
}
