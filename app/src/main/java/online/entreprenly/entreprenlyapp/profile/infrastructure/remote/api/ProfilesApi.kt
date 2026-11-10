package online.entreprenly.entreprenlyapp.profile.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.NotificationSettingsResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.PreferencesResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.ProfileResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.UpdateProfileResource
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class PhoneVerificationRequest(val phone: String)
data class ConfirmPhoneVerificationRequest(val phone: String, val code: String)

interface ProfilesApi {
    @Multipart
    @POST("api/v1/profiles/{profileId}/avatar")
    suspend fun uploadAvatar(
        @Path("profileId") profileId: Long,
        @Part image: MultipartBody.Part
    ): Response<ProfileResource>

    @POST("api/v1/profiles/{profileId}/phone-verifications")
    suspend fun requestPhoneVerification(
        @Path("profileId") profileId: Long,
        @Body request: PhoneVerificationRequest
    ): Response<Unit>

    @POST("api/v1/profiles/{profileId}/phone-verifications/confirm")
    suspend fun confirmPhoneVerification(
        @Path("profileId") profileId: Long,
        @Body request: ConfirmPhoneVerificationRequest
    ): Response<ProfileResource>

    @GET("api/v1/profiles")
    suspend fun getByUserId(@Query("userId") userId: Long): Response<ProfileResource>

    @PUT("api/v1/profiles/{profileId}")
    suspend fun updateProfile(
        @Path("profileId") profileId: Long,
        @Body resource: UpdateProfileResource
    ): Response<ProfileResource>

    @PUT("api/v1/profiles/{profileId}/preferences")
    suspend fun updatePreferences(
        @Path("profileId") profileId: Long,
        @Body resource: PreferencesResource
    ): Response<ProfileResource>

    @PUT("api/v1/profiles/{profileId}/notification-settings")
    suspend fun updateNotificationSettings(
        @Path("profileId") profileId: Long,
        @Body resource: NotificationSettingsResource
    ): Response<ProfileResource>
}
