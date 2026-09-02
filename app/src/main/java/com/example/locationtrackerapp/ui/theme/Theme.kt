package com.example.locationtrackerapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandRed700,
    onPrimary = Color.White,
    primaryContainer = BrandRedTint,
    onPrimaryContainer = BrandRed900,
    secondary = BrandRed800,
    onSecondary = Color.White,
    secondaryContainer = BrandRedTint,
    onSecondaryContainer = BrandRed900,
    tertiary = BrandRed600,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = ErrorRed,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = DividerColor,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
)

/**
 * Uriel Cafe app theme.
 *
 * The app is intentionally light-mode-first and restaurant-branded, so the
 * color scheme does not switch with the system dark-mode setting: it always
 * renders the light, dark-red-accented palette described above.
 */
@Composable
fun LocationTrackerAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
