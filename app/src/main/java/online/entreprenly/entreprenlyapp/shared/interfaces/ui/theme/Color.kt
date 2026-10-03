package online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette taken from the Entreprenly icon (docs/images/capitulo2/entrepenly-icon-dark.png).
val BrandOrange = Color(0xFFF5871A)
val BrandOrangeDark = Color(0xFF9A4F00)
val BrandInk = Color(0xFF0B0F12)
val BrandInkSurface = Color(0xFF161B20)
val BrandSlate = Color(0xFF3A4550)
val BrandSlateLight = Color(0xFFB8C2CC)
val BrandPaper = Color(0xFFFAFAF8)
val BrandWhite = Color(0xFFFFFFFF)

// Status colors (badges, timelines, approve/reject actions). Containers are light tints
// paired with their darker content color, so they read well on both themes.
val StatusSuccess = Color(0xFF1E7B3A)
val StatusSuccessContainer = Color(0xFFDDF7E6)
val StatusDanger = Color(0xFFD12E26)
val StatusDangerContainer = Color(0xFFFDE2E1)
val StatusWarning = BrandOrangeDark
val StatusWarningContainer = Color(0xFFFDE4CC)
val StatusNeutral = BrandSlate
val StatusNeutralContainer = Color(0xFFE8E6E3)
val StatusInfo = Color(0xFF2F6FE0)
