package online.entreprenly.entreprenlyapp.profile.domain.repositories

import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.PhoneNumber
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetProfileByUserIdQuery
import online.entreprenly.entreprenlyapp.shared.application.result.Result

/** Port hacia el contexto Profile del backend. */
interface ProfileRepository {
    suspend fun uploadAvatar(profileId: Long, image: AvatarImage): Result<Profile>
    suspend fun requestPhoneVerification(profileId: Long, phone: PhoneNumber): Result<Unit>
    suspend fun confirmPhoneVerification(profileId: Long, phone: PhoneNumber, code: String): Result<Profile>
    suspend fun getByUserId(query: GetProfileByUserIdQuery): Result<Profile>
    suspend fun updateProfile(command: UpdateProfileCommand): Result<Profile>
    suspend fun updatePreferences(command: UpdatePreferencesCommand): Result<Profile>
    suspend fun updateNotificationSettings(command: UpdateNotificationSettingsCommand): Result<Profile>
}
