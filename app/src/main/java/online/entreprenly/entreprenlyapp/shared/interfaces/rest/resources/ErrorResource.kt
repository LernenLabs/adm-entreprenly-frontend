package online.entreprenly.entreprenlyapp.shared.interfaces.rest.resources

/** Standard backend error body: { code, message, details? }. */
data class ErrorResource(
    val code: String? = null,
    val message: String? = null,
    val details: String? = null
)
