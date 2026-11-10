package online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration

import retrofit2.Response
import retrofit2.http.HEAD

/** Cheap request used only to wake the backend up; any status (even 401) means it is awake. */
interface WarmUpApi {
    @HEAD(".")
    suspend fun ping(): Response<Void>
}
