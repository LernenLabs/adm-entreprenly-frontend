package online.entreprenly.entreprenlyapp.profile.application.commandservices

import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.PhoneNumber
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ProfileCommandService {
    suspend fun uploadAvatar(profileId: Long, image: AvatarImage): Result<Profile>
    suspend fun requestPhoneVerification(profileId: Long, phone: PhoneNumber): Result<Unit>
    suspend fun confirmPhoneVerification(profileId: Long, phone: PhoneNumber, code: String): Result<Profile>
    suspend fun handle(command: UpdateProfileCommand): Result<Profile>
    suspend fun handle(command: UpdatePreferencesCommand): Result<Profile>
    suspend fun handle(command: UpdateNotificationSettingsCommand): Result<Profile>
}
