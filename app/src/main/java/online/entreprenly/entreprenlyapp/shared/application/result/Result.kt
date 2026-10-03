package online.entreprenly.entreprenlyapp.shared.application.result

/** Resultado explícito de una operación: éxito con valor o fallo con [ApplicationError]. */
sealed interface Result<out T> {
    data class Success<out T>(val value: T) : Result<T>
    data class Failure(val error: ApplicationError) : Result<Nothing>

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}
