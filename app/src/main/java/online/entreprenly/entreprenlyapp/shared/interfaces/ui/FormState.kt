package online.entreprenly.entreprenlyapp.shared.interfaces.ui

/**
 * Common state of a form: in progress, error and success message.
 * [errorTitle] is optional and used by banners that show a headline plus [error] as detail.
 */
data class FormState(
    val loading: Boolean = false,
    val error: UiText? = null,
    val success: UiText? = null,
    val errorTitle: UiText? = null
)
