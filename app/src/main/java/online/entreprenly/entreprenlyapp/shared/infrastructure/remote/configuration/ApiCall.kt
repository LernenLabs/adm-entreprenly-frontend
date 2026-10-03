package online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration

import com.google.gson.Gson
import java.io.IOException
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.rest.resources.ErrorResource
import retrofit2.Response

/** Ejecuta una llamada Retrofit y traduce HTTP/errores de red a [Result]. */
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
    Result.Failure(ApplicationError.Network("No se pudo conectar con el servidor"))
} catch (e: Exception) {
    Result.Failure(ApplicationError.Unexpected(e.message ?: "Error inesperado"))
}

private fun <D> Response<D>.toApplicationError(): ApplicationError {
    val message = runCatching {
        Gson().fromJson(errorBody()?.string(), ErrorResource::class.java)?.message
    }.getOrNull()
    return when (code()) {
        400 -> ApplicationError.Validation(message ?: "Datos inválidos")
        401, 403 -> ApplicationError.Unauthorized(message ?: "No autorizado")
        404 -> ApplicationError.NotFound(message ?: "No encontrado")
        409 -> ApplicationError.Conflict(message ?: "Conflicto")
        else -> ApplicationError.Unexpected(message ?: "Error del servidor (${code()})")
    }
}

/** Variante para respuestas que deben traer cuerpo: un cuerpo vacío es un error inesperado. */
suspend fun <D : Any, T> safeApiCallWithBody(
    call: suspend () -> Response<D>,
    transform: (D) -> T
): Result<T> = when (val r = safeApiCall(call) { it }) {
    is Result.Failure -> r
    is Result.Success ->
        r.value?.let { Result.Success(transform(it)) }
            ?: Result.Failure(ApplicationError.Unexpected("Respuesta vacía del servidor"))
}
