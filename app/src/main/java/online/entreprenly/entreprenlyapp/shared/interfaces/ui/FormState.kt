package online.entreprenly.entreprenlyapp.shared.interfaces.ui

/** Common state of a form: in progress, error and success message. */
data class FormState(
    val loading: Boolean = false,
    val error: UiText? = null,
    val success: UiText? = null
)
