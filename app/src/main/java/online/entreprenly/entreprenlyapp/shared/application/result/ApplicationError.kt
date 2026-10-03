package online.entreprenly.entreprenlyapp.shared.application.result

/** Errores de aplicación que cruzan la frontera entre capas (equivalente al backend). */
sealed class ApplicationError(open val message: String) {
    data class Validation(override val message: String) : ApplicationError(message)
    data class Unauthorized(override val message: String) : ApplicationError(message)
    data class NotFound(override val message: String) : ApplicationError(message)
    data class Conflict(override val message: String) : ApplicationError(message)
    data class Network(override val message: String) : ApplicationError(message)
    data class Unexpected(override val message: String) : ApplicationError(message)
}
