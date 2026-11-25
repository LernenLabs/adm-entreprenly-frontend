package online.entreprenly.entreprenlyapp.profile.infrastructure.remote.repositories

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.PhoneNumber
import online.entreprenly.entreprenlyapp.profile.infrastructure.remote.api.ConfirmPhoneVerificationRequest
import online.entreprenly.entreprenlyapp.profile.infrastructure.remote.api.PhoneVerificationRequest
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
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

    override suspend fun uploadAvatar(profileId: Long, image: AvatarImage): Result<Profile> {
        val content = image.bytes.toRequestBody(image.mimeType.toMediaType())
        val extension = image.mimeType.substringAfter('/')
        val part = MultipartBody.Part.createFormData("image", "avatar.$extension", content)
        return safeApiCallWithBody({ api.uploadAvatar(profileId, part) }) { it.toEntity() }
    }

    override suspend fun requestPhoneVerification(profileId: Long, phone: PhoneNumber): Result<Unit> =
        safeApiCall({ api.requestPhoneVerification(profileId, PhoneVerificationRequest(phone.value)) }) { }

    override suspend fun confirmPhoneVerification(profileId: Long, phone: PhoneNumber, code: String): Result<Profile> =
        safeApiCallWithBody({
            api.confirmPhoneVerification(profileId, ConfirmPhoneVerificationRequest(phone.value, code))
        }) { it.toEntity() }

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
