package online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    onPrimary = BrandInk,
    secondary = BrandSlateLight,
    onSecondary = BrandInk,
    tertiary = BrandOrange,
    background = BrandInk,
    onBackground = BrandWhite,
    surface = BrandInkSurface,
    onSurface = BrandWhite
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = BrandInk,
    secondary = BrandSlate,
    onSecondary = BrandWhite,
    tertiary = BrandOrangeDark,
    background = BrandPaper,
    onBackground = BrandInk,
    surface = BrandWhite,
    onSurface = BrandInk
)

/**
 * App-wide theme. Dynamic (wallpaper) color is intentionally not used so every screen
 * keeps the Entreprenly brand colors.
 */
@Composable
fun EntreprenlyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
