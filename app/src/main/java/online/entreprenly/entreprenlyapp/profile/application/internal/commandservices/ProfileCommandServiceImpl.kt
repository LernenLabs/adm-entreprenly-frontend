package online.entreprenly.entreprenlyapp.profile.application.internal.commandservices

import kotlinx.coroutines.flow.first
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.PhoneNumber
import online.entreprenly.entreprenlyapp.profile.application.commandservices.ProfileCommandService
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.profile.domain.repositories.LocalPreferencesRepository
import online.entreprenly.entreprenlyapp.profile.domain.repositories.ProfileRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ProfileCommandServiceImpl(
    private val profileRepository: ProfileRepository,
    private val localPreferences: LocalPreferencesRepository
) : ProfileCommandService {

    override suspend fun uploadAvatar(profileId: Long, image: AvatarImage) =
        profileRepository.uploadAvatar(profileId, image)

    override suspend fun requestPhoneVerification(profileId: Long, phone: PhoneNumber) =
        profileRepository.requestPhoneVerification(profileId, phone)

    override suspend fun confirmPhoneVerification(profileId: Long, phone: PhoneNumber, code: String) =
        profileRepository.confirmPhoneVerification(profileId, phone, code)

    override suspend fun handle(command: UpdateProfileCommand): Result<Profile> =
        profileRepository.updateProfile(command)

    /** Preferences apply immediately on the device and are rolled back if the server rejects them. */
    override suspend fun handle(command: UpdatePreferencesCommand): Result<Profile> {
        val previous = localPreferences.preferences.first()
        localPreferences.save(command.preferences)
        val result = profileRepository.updatePreferences(command)
        if (result is Result.Failure) previous?.let { localPreferences.save(it) }
        return result
    }

    override suspend fun handle(command: UpdateNotificationSettingsCommand): Result<Profile> =
        profileRepository.updateNotificationSettings(command)
}
