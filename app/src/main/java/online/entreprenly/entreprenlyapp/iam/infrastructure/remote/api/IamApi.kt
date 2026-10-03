package online.entreprenly.entreprenlyapp.iam.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.AuthenticatedUserResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.ChangeEmailResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.ChangePasswordResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.SignInResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.SignUpResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.UserResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface IamApi {
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body resource: SignInResource): Response<AuthenticatedUserResource>

    @POST("api/v1/authentication/sign-up")
    suspend fun signUp(@Body resource: SignUpResource): Response<UserResource>

    @PUT("api/v1/users/me/password")
    suspend fun changePassword(@Body resource: ChangePasswordResource): Response<Unit>

    @PUT("api/v1/users/me/email")
    suspend fun changeEmail(@Body resource: ChangeEmailResource): Response<Unit>
}
