package online.entreprenly.entreprenlyapp.iam.domain.model.commands

import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Password

data class SignInCommand(val email: Email, val password: Password)

data class SignUpCommand(
    val email: Email,
    val password: Password,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val timezone: String? = null
)

data class ChangeEmailCommand(val newEmail: Email)

data class ChangePasswordCommand(val currentPassword: Password, val newPassword: Password)

/** Cierra la sesión local (US-61): descarta el JWT almacenado. */
data object SignOutCommand
