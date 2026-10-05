package online.entreprenly.entreprenlyapp.shared.interfaces.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Text a ViewModel can emit without a Context: a literal (e.g. server message) or a string resource. */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Res(@StringRes val id: Int) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Raw -> value
        is Res -> stringResource(id)
    }
}
