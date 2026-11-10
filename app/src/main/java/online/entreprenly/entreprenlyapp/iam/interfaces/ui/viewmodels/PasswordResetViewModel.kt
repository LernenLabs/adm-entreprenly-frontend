package online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.application.commandservices.UserCommandService
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Password
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.PasswordResetLink
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

class PasswordResetViewModel(
    private val users: UserCommandService,
    private val link: PasswordResetLink? = null
) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form = _form.asStateFlow()
    private val _linkReady = MutableStateFlow(false)
    val linkReady = _linkReady.asStateFlow()
    private val _completed = MutableStateFlow(false)
    val completed = _completed.asStateFlow()

    fun request(emailInput: String) {
        if (_form.value.loading) return
        val email = Email.of(emailInput)
        if (email == null) {
            _form.value = FormState(error = UiText.Res(R.string.error_invalid_email))
            return
        }
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            _form.value = when (val result = users.requestPasswordReset(email)) {
                is Result.Success -> FormState(success = UiText.Res(R.string.reset_request_accepted))
                is Result.Failure -> FormState(error = UiText.Raw(result.error.message))
            }
        }
    }

    fun clearRequest() {
        if (!_form.value.loading) _form.value = FormState()
    }

    fun validateLink() {
        if (_form.value.loading || _linkReady.value) return
        val target = link
        if (target == null) {
            _form.value = FormState(error = UiText.Res(R.string.reset_invalid_link))
            return
        }
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            _form.value = when (val result = users.validatePasswordReset(target)) {
                is Result.Success -> {
                    _linkReady.value = true
                    FormState()
                }
                is Result.Failure -> FormState(error = UiText.Raw(result.error.message))
            }
        }
    }

    fun confirm(input: String, confirmation: String) {
        val target = link ?: return
        if (_form.value.loading || !_linkReady.value || _completed.value) return
        val password = Password.of(input)?.takeIf { input.encodeToByteArray().size <= 72 }
        val error = when {
            password == null -> R.string.reset_password_requirements
            input != confirmation -> R.string.account_password_mismatch
            else -> null
        }
        if (error != null) {
            _form.value = FormState(error = UiText.Res(error))
            return
        }
        _form.value = FormState(loading = true)
        viewModelScope.launch {
            _form.value = when (val result = users.confirmPasswordReset(target, requireNotNull(password))) {
                is Result.Success -> {
                    _completed.value = true
                    FormState()
                }
                is Result.Failure -> FormState(error = UiText.Raw(result.error.message))
            }
        }
    }
}
