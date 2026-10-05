package online.entreprenly.entreprenlyapp.profile.domain.model.commands

import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences

/**
 * Updates the editable profile data (US-63). The backend replaces phone and avatar too,
 * so callers must pass the current values to keep them.
 */
data class UpdateProfileCommand(
    val profileId: Long,
    val firstName: String,
    val lastName: String,
    val biography: String?,
    val phone: String?,
    val avatarUrl: String?
)

data class UpdatePreferencesCommand(val profileId: Long, val preferences: Preferences)

data class UpdateNotificationSettingsCommand(val profileId: Long, val stockAlerts: Boolean)
