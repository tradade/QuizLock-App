package com.quizlock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimaryContainerText,
    secondary = QuizAmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = QuizAmberLight,
    tertiary = DarkSuccess,
    onTertiary = Color.Black,
    tertiaryContainer = DarkSuccessContainer,
    onTertiaryContainer = DarkSuccessContainerText,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceBorder,
    error = QuizRedError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = QuizBluePrimary,
    onPrimary = Color.White,
    primaryContainer = QuizBlueLight,
    onPrimaryContainer = QuizBlueDark,
    secondary = QuizAmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = QuizAmberLight,
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = QuizGreenSuccess,
    onTertiary = Color.White,
    tertiaryContainer = QuizGreenLight,
    onTertiaryContainer = Color(0xFF065F46),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = QuizRedError,
    onError = Color.White
)

@Composable
fun QuizGateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
