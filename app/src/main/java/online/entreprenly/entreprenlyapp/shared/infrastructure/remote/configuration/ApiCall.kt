package online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration

import com.google.gson.Gson
import java.io.IOException
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.rest.resources.ErrorResource
import retrofit2.Response

/** Runs a Retrofit call and maps HTTP/network errors to [Result]. */
suspend fun <D, T> safeApiCall(
    call: suspend () -> Response<D>,
    transform: (D?) -> T
): Result<T> = try {
    val response = call()
    if (response.isSuccessful) {
        Result.Success(transform(response.body()))
    } else {
        Result.Failure(response.toApplicationError())
    }
} catch (e: IOException) {
    Result.Failure(ApplicationError.Network("Could not reach the server"))
} catch (e: Exception) {
    Result.Failure(ApplicationError.Unexpected(e.message ?: "Unexpected error"))
}

private fun <D> Response<D>.toApplicationError(): ApplicationError {
    val message = runCatching {
        Gson().fromJson(errorBody()?.string(), ErrorResource::class.java)?.message
    }.getOrNull()
    return when (code()) {
        400 -> ApplicationError.Validation(message ?: "Invalid data")
        401 -> ApplicationError.Unauthorized(message ?: "Unauthorized")
        403 -> ApplicationError.Forbidden(message ?: "Forbidden")
        404 -> ApplicationError.NotFound(message ?: "Not found")
        409 -> ApplicationError.Conflict(message ?: "Conflict")
        else -> ApplicationError.Unexpected(message ?: "Server error (${code()})")
    }
}

/** Variant for responses that must carry a body: an empty body is an unexpected error. */
suspend fun <D : Any, T> safeApiCallWithBody(
    call: suspend () -> Response<D>,
    transform: (D) -> T
): Result<T> = when (val r = safeApiCall(call) { it }) {
    is Result.Failure -> r
    is Result.Success ->
        r.value?.let { Result.Success(transform(it)) }
            ?: Result.Failure(ApplicationError.Unexpected("Empty response from the server"))
}
