package online.entreprenly.entreprenlyapp.profile.infrastructure.remote.repositories

import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetProfileByUserIdQuery
import online.entreprenly.entreprenlyapp.profile.domain.repositories.ProfileRepository
import online.entreprenly.entreprenlyapp.profile.infrastructure.remote.api.ProfilesApi
import online.entreprenly.entreprenlyapp.profile.infrastructure.remote.assemblers.toEntity
import online.entreprenly.entreprenlyapp.profile.infrastructure.remote.assemblers.toResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.NotificationSettingsResource
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class ProfileRepositoryImpl(private val api: ProfilesApi) : ProfileRepository {

    override suspend fun getByUserId(query: GetProfileByUserIdQuery): Result<Profile> =
        safeApiCallWithBody({ api.getByUserId(query.userId) }) { it.toEntity() }

    override suspend fun updateProfile(command: UpdateProfileCommand): Result<Profile> =
        safeApiCallWithBody({ api.updateProfile(command.profileId, command.toResource()) }) { it.toEntity() }

    override suspend fun updatePreferences(command: UpdatePreferencesCommand): Result<Profile> =
        safeApiCallWithBody({ api.updatePreferences(command.profileId, command.preferences.toResource()) }) {
            it.toEntity()
        }

    override suspend fun updateNotificationSettings(command: UpdateNotificationSettingsCommand): Result<Profile> =
        safeApiCallWithBody({
            api.updateNotificationSettings(command.profileId, NotificationSettingsResource(command.stockAlerts))
        }) { it.toEntity() }
}
