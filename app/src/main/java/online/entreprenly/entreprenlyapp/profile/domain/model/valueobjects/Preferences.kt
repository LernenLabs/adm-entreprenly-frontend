package online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects

/** Display preferences (US-67). Each option carries the code the backend stores. */

enum class AppLanguage(val code: String, val nativeName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code?.lowercase() } ?: ENGLISH
    }
}

enum class AppTheme(val code: String) {
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromCode(code: String?): AppTheme = entries.firstOrNull { it.code == code?.lowercase() } ?: LIGHT
    }
}

enum class AppCurrency(val code: String, val displayName: String, val symbol: String) {
    PEN("PEN", "Sol peruano", "S/"),
    USD("USD", "Dólar estadounidense", "US$");

    companion object {
        fun fromCode(code: String?): AppCurrency = entries.firstOrNull { it.code == code?.uppercase() } ?: PEN
    }
}

enum class AppTimezone(val id: String, val offset: String) {
    LIMA("America/Lima", "UTC−05:00"),
    UTC("UTC", "UTC+00:00");

    companion object {
        /** The backend may store "America/Lima (UTC-05:00)"; match on the zone id prefix. */
        fun fromId(raw: String?): AppTimezone =
            entries.firstOrNull { raw?.startsWith(it.id, ignoreCase = true) == true } ?: LIMA
    }
}

data class Preferences(
    val language: AppLanguage,
    val timezone: AppTimezone,
    val theme: AppTheme,
    val currency: AppCurrency
)
