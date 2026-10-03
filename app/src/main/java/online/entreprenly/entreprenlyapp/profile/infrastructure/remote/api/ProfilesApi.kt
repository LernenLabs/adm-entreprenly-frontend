package online.entreprenly.entreprenlyapp.profile.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.NotificationSettingsResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.PreferencesResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.ProfileResource
import online.entreprenly.entreprenlyapp.profile.interfaces.rest.resources.UpdateProfileResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfilesApi {
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
