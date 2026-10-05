package online.entreprenly.entreprenlyapp.shared.application.result

/** Explicit operation result: success with a value or failure with an [ApplicationError]. */
sealed interface Result<out T> {
    data class Success<out T>(val value: T) : Result<T>
    data class Failure(val error: ApplicationError) : Result<Nothing>

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}
