package com.tillzo.pos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * TillzoPOS Material Design 3 Theme.
 * ALWAYS dark — matches Casio calculator aesthetic from OPT-2.
 * No light theme variant — this app is designed for dark environments (shops).
 */
private val TillzoDarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = TextPrimary,
    primaryContainer = AccentBlueDark,
    onPrimaryContainer = TextPrimary,

    secondary        = AccentBlueLight,
    onSecondary      = TextPrimary,

    background       = BackgroundDark,
    onBackground     = TextPrimary,

    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error            = ErrorRed,
    onError          = TextPrimary,

    outline          = SurfaceVariant,
    outlineVariant   = SurfaceHighlight
)

@Composable
fun TillzoPOSTheme(
    content: @Composable () -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as android.app.Activity).window
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightNavigationBars = false // white icons
                isAppearanceLightStatusBars = false     // white status icons
            }
        }
    }

    MaterialTheme(
        colorScheme = TillzoDarkColorScheme,
        typography  = TillzoTypography,
        content     = content
    )
}
