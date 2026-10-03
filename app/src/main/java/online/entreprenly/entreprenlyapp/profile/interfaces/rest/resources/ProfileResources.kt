package online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources

// DTOs del contexto Profile del backend (api/v1/profiles).

data class PreferencesResource(
    val language: String,
    val timezone: String,
    val theme: String,
    val currency: String
)

data class NotificationSettingsResource(val stockAlerts: Boolean)

data class ProfileResource(
    val id: Long,
    val userId: Long,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val avatarUrl: String?,
    val biography: String?,
    val role: String?,
    val plan: String?,
    val preferences: PreferencesResource?,
    val notificationSettings: NotificationSettingsResource?
)

data class UpdateProfileResource(
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val avatarUrl: String?,
    val biography: String?
)
