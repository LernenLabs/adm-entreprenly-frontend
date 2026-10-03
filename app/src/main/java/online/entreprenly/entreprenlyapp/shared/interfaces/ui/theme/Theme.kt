package online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Design-system colors that Material 3's ColorScheme has no slot for. */
@Immutable
data class ExtraColors(
    val muted: Color,
    val border: Color,
    val highlight: Color,
    val success: Color,
    val successBackground: Color,
    val warning: Color,
    val warningBackground: Color
)

private val LightExtraColors = ExtraColors(
    muted = LightMuted,
    border = LightBorder,
    highlight = LightHighlight,
    success = LightSuccess,
    successBackground = LightSuccessBg,
    warning = LightWarning,
    warningBackground = LightWarningBg
)

private val DarkExtraColors = ExtraColors(
    muted = DarkMuted,
    border = DarkBorder,
    highlight = DarkHighlight,
    success = DarkSuccess,
    successBackground = DarkSuccessBg,
    warning = DarkWarning,
    warningBackground = DarkWarningBg
)

private val LocalExtraColors = staticCompositionLocalOf { LightExtraColors }

/** Access the extra design tokens: `MaterialTheme.extraColors.muted`. */
val MaterialTheme.extraColors: ExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtraColors.current

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkPrimaryText,
    secondary = DarkMuted,
    onSecondary = DarkBackground,
    tertiary = DarkPrimary,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    onSurfaceVariant = DarkMuted,
    surfaceVariant = DarkSurface,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    primaryContainer = DarkHighlight,
    onPrimaryContainer = DarkText,
    error = DarkError
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightPrimaryText,
    secondary = LightMuted,
    onSecondary = LightSurface,
    tertiary = LightPrimary,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    onSurfaceVariant = LightMuted,
    surfaceVariant = LightSurface,
    outline = LightBorder,
    outlineVariant = LightBorder,
    primaryContainer = LightHighlight,
    onPrimaryContainer = LightText,
    error = LightError
)

/**
 * App-wide theme. Dynamic (wallpaper) color is intentionally not used so every screen
 * keeps the Entreprenly design tokens.
 */
@Composable
fun EntreprenlyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
