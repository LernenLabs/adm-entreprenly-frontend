package online.entreprenly.entreprenlyapp.profile.domain.model.aggregates

import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.NotificationSettings
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences

data class Profile(
    val id: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val avatarUrl: String?,
    val biography: String?,
    val role: String?,
    val plan: String?,
    val preferences: Preferences,
    val notificationSettings: NotificationSettings
) {
    val fullName: String get() = "$firstName $lastName".trim()

    val initials: String
        get() = listOf(firstName, lastName)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
}
