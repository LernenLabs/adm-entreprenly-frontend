package online.entreprenly.entreprenlyapp.shared.interfaces.rest.resources

/** Cuerpo de error estándar del backend: { code, message, details? }. */
data class ErrorResource(
    val code: String? = null,
    val message: String? = null,
    val details: String? = null
)
