package online.entreprenly.entreprenlyapp.profile.application.commandservices

import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ProfileCommandService {
    suspend fun handle(command: UpdateProfileCommand): Result<Profile>
    suspend fun handle(command: UpdatePreferencesCommand): Result<Profile>
    suspend fun handle(command: UpdateNotificationSettingsCommand): Result<Profile>
}
