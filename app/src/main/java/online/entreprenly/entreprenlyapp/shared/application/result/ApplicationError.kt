package online.entreprenly.entreprenlyapp.shared.application.result

/** Application errors that cross layer boundaries (mirrors the backend). */
sealed class ApplicationError(open val message: String) {
    data class Validation(override val message: String) : ApplicationError(message)
    data class Unauthorized(override val message: String) : ApplicationError(message)
    /** Authenticated but not allowed, e.g. the user's plan does not include the feature. */
    data class Forbidden(override val message: String) : ApplicationError(message)
    data class NotFound(override val message: String) : ApplicationError(message)
    data class Conflict(override val message: String) : ApplicationError(message)
    data class Network(override val message: String) : ApplicationError(message)
    data class Unexpected(override val message: String) : ApplicationError(message)
}
