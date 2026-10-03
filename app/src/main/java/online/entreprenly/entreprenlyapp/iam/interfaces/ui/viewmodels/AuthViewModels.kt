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
import online.entreprenly.entreprenlyapp.shared.application.result.Result

/** Estado de la sesión para decidir qué grafo de navegación mostrar. */
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

/** Estado común de un formulario: en curso, error de validación/servidor y éxito. */
data class FormState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

private const val INVALID_EMAIL = "Ingresa un email válido"
private const val INVALID_PASSWORD = "La contraseña debe tener entre 8 y 255 caracteres"

class SignInViewModel(private val userCommandService: UserCommandService) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    fun signIn(email: String, password: String) {
        val e = Email.of(email) ?: return fail(INVALID_EMAIL)
        val p = Password.of(password) ?: return fail(INVALID_PASSWORD)
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            // En éxito la sesión se guarda y la navegación reacciona al SessionState.
            _form.value = when (val r = userCommandService.handle(SignInCommand(e, p))) {
                is Result.Success -> FormState()
                is Result.Failure -> FormState(error = r.error.message)
            }
        }
    }

    private fun fail(message: String) = _form.update { FormState(error = message) }
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
        val e = Email.of(email) ?: return fail(INVALID_EMAIL)
        val p = Password.of(password) ?: return fail(INVALID_PASSWORD)
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            val created = userCommandService.handle(
                SignUpCommand(e, p, firstName.trim(), lastName.trim(), phone.trim())
            )
            _form.value = when (created) {
                is Result.Failure -> FormState(error = created.error.message)
                // Cuenta creada: inicia sesión automáticamente.
                is Result.Success -> when (val r = userCommandService.handle(SignInCommand(e, p))) {
                    is Result.Success -> FormState()
                    is Result.Failure -> FormState(error = r.error.message)
                }
            }
        }
    }

    private fun fail(message: String) = _form.update { FormState(error = message) }
}

class AccountViewModel(private val userCommandService: UserCommandService) : ViewModel() {
    private val _passwordForm = MutableStateFlow(FormState())
    val passwordForm: StateFlow<FormState> = _passwordForm.asStateFlow()

    private val _emailForm = MutableStateFlow(FormState())
    val emailForm: StateFlow<FormState> = _emailForm.asStateFlow()

    fun changePassword(current: String, new: String) {
        val c = Password.of(current) ?: return _passwordForm.update { FormState(error = INVALID_PASSWORD) }
        val n = Password.of(new) ?: return _passwordForm.update { FormState(error = INVALID_PASSWORD) }
        _passwordForm.value = FormState(loading = true)
        viewModelScope.launch {
            _passwordForm.value = when (val r = userCommandService.handle(ChangePasswordCommand(c, n))) {
                is Result.Success -> FormState(success = "Contraseña actualizada")
                is Result.Failure -> FormState(error = r.error.message)
            }
        }
    }

    /** En éxito se cierra la sesión (el JWT lleva el email anterior); la navegación vuelve al login. */
    fun changeEmail(newEmail: String) {
        val e = Email.of(newEmail) ?: return _emailForm.update { FormState(error = INVALID_EMAIL) }
        _emailForm.value = FormState(loading = true)
        viewModelScope.launch {
            _emailForm.value = when (val r = userCommandService.handle(ChangeEmailCommand(e))) {
                is Result.Success -> FormState(success = "Email actualizado")
                is Result.Failure -> FormState(error = r.error.message)
            }
        }
    }
}
