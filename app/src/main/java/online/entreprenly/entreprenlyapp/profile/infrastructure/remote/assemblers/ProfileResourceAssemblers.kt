package online.entreprenly.entreprenlyapp.profile.infrastructure.remote.assemblers

import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppCurrency
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppLanguage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTheme
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTimezone
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.NotificationSettings
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.PreferencesResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.ProfileResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.UpdateProfileResource

fun ProfileResource.toEntity() = Profile(
    id = id,
    userId = userId,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    phone = phone,
    avatarUrl = avatarUrl,
    biography = biography,
    role = role,
    plan = plan,
    preferences = Preferences(
        language = AppLanguage.fromCode(preferences?.language),
        timezone = AppTimezone.fromId(preferences?.timezone),
        theme = AppTheme.fromCode(preferences?.theme),
        currency = AppCurrency.fromCode(preferences?.currency)
    ),
    notificationSettings = NotificationSettings(stockAlerts = notificationSettings?.stockAlerts ?: true)
)

fun UpdateProfileCommand.toResource() = UpdateProfileResource(
    firstName = firstName.trim(),
    lastName = lastName.trim(),
    phone = phone,
    avatarUrl = avatarUrl,
    biography = biography?.trim()?.takeIf { it.isNotEmpty() }
)

fun Preferences.toResource() = PreferencesResource(
    language = language.code,
    timezone = timezone.id,
    theme = theme.code,
    currency = currency.code
)
