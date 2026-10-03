package online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.iam.application.commandservices.UserCommandService
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignOutCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Password
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

/** Session state used to decide which navigation graph to show. */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val session: AuthSession) : SessionState
}

class SessionViewModel(
    sessionQueryService: SessionQueryService,
    private val userCommandService: UserCommandService
) : ViewModel() {

    val state: StateFlow<SessionState> = sessionQueryService.handle(GetCurrentSessionQuery)
        .map { if (it != null) SessionState.SignedIn(it) else SessionState.SignedOut }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    fun signOut() {
        viewModelScope.launch { userCommandService.handle(SignOutCommand) }
    }
}


class SignInViewModel(private val userCommandService: UserCommandService) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    fun signIn(email: String, password: String) {
        val e = Email.of(email) ?: return fail(UiText.Res(R.string.error_invalid_email))
        val p = Password.of(password) ?: return fail(UiText.Res(R.string.error_invalid_password))
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            // On success the session is stored and navigation reacts to SessionState.
            _form.value = when (val r = userCommandService.handle(SignInCommand(e, p))) {
                is Result.Success -> FormState()
                is Result.Failure -> when (r.error) {
                    is ApplicationError.Network, is ApplicationError.Unexpected ->
                        FormState(error = UiText.Raw(r.error.message))
                    else -> FormState(
                        errorTitle = UiText.Res(R.string.sign_in_error_title),
                        error = UiText.Res(R.string.sign_in_error_message)
                    )
                }
            }
        }
    }

    private fun fail(message: UiText) = _form.update { FormState(error = message) }
}

class SignUpViewModel(private val userCommandService: UserCommandService) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        val e = Email.of(email) ?: return fail(UiText.Res(R.string.error_invalid_email))
        val p = Password.of(password) ?: return fail(UiText.Res(R.string.error_invalid_password))
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            val created = userCommandService.handle(
                SignUpCommand(e, p, firstName.trim(), lastName.trim(), phone.trim())
            )
            _form.value = when (created) {
                is Result.Failure -> FormState(error = UiText.Raw(created.error.message))
                // Account created: sign in automatically.
                is Result.Success -> when (val r = userCommandService.handle(SignInCommand(e, p))) {
                    is Result.Success -> FormState()
                    is Result.Failure -> FormState(error = UiText.Raw(r.error.message))
                }
            }
        }
    }

    private fun fail(message: UiText) = _form.update { FormState(error = message) }
}

class AccountViewModel(private val userCommandService: UserCommandService) : ViewModel() {
    private val _passwordForm = MutableStateFlow(FormState())
    val passwordForm: StateFlow<FormState> = _passwordForm.asStateFlow()

    private val _emailForm = MutableStateFlow(FormState())
    val emailForm: StateFlow<FormState> = _emailForm.asStateFlow()

    fun changePassword(current: String, new: String) {
        val c = Password.of(current) ?: return _passwordForm.update { FormState(error = UiText.Res(R.string.error_invalid_password)) }
        val n = Password.of(new) ?: return _passwordForm.update { FormState(error = UiText.Res(R.string.error_invalid_password)) }
        _passwordForm.value = FormState(loading = true)
        viewModelScope.launch {
            _passwordForm.value = when (val r = userCommandService.handle(ChangePasswordCommand(c, n))) {
                is Result.Success -> FormState(success = UiText.Res(R.string.account_password_updated))
                is Result.Failure -> FormState(error = UiText.Raw(r.error.message))
            }
        }
    }

    /** On success the session is cleared (the JWT carries the old email); navigation returns to sign-in. */
    fun changeEmail(newEmail: String) {
        val e = Email.of(newEmail) ?: return _emailForm.update { FormState(error = UiText.Res(R.string.error_invalid_email)) }
        _emailForm.value = FormState(loading = true)
        viewModelScope.launch {
            _emailForm.value = when (val r = userCommandService.handle(ChangeEmailCommand(e))) {
                is Result.Success -> FormState(success = UiText.Res(R.string.account_email_updated))
                is Result.Failure -> FormState(error = UiText.Raw(r.error.message))
            }
        }
    }
}
