package online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import online.entreprenly.entreprenlyapp.R

/** Reddit Sans (SIL OFL, see third_party/RedditSans-OFL.txt): one variable font, one entry per weight. */
@OptIn(ExperimentalTextApi::class)
val RedditSans = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold
    ).map { weight ->
        Font(
            resId = R.font.reddit_sans,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

private val Default = Typography()

/** Material 3 type scale using Reddit Sans, the font of the Figma design. */
val Typography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = RedditSans),
    displayMedium = Default.displayMedium.copy(fontFamily = RedditSans),
    displaySmall = Default.displaySmall.copy(fontFamily = RedditSans),
    headlineLarge = Default.headlineLarge.copy(fontFamily = RedditSans),
    headlineMedium = Default.headlineMedium.copy(fontFamily = RedditSans),
    headlineSmall = Default.headlineSmall.copy(fontFamily = RedditSans),
    titleLarge = Default.titleLarge.copy(fontFamily = RedditSans),
    titleMedium = Default.titleMedium.copy(fontFamily = RedditSans),
    titleSmall = Default.titleSmall.copy(fontFamily = RedditSans),
    bodyLarge = Default.bodyLarge.copy(fontFamily = RedditSans),
    bodyMedium = Default.bodyMedium.copy(fontFamily = RedditSans),
    bodySmall = Default.bodySmall.copy(fontFamily = RedditSans),
    labelLarge = Default.labelLarge.copy(fontFamily = RedditSans),
    labelMedium = Default.labelMedium.copy(fontFamily = RedditSans),
    labelSmall = Default.labelSmall.copy(fontFamily = RedditSans)
)
