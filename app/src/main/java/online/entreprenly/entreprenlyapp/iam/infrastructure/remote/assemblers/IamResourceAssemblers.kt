package online.entreprenly.entreprenlyapp.iam.infrastructure.remote.assemblers

import online.entreprenly.entreprenlyapp.iam.domain.model.aggregates.User
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Roles
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.AuthenticatedUserResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.ChangeEmailResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.ChangePasswordResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.SignInResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.SignUpResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.UserResource

fun SignInCommand.toResource() = SignInResource(email.value, password.value)

fun SignUpCommand.toResource() = SignUpResource(
    email = email.value,
    password = password.value,
    firstName = firstName?.takeIf { it.isNotBlank() },
    lastName = lastName?.takeIf { it.isNotBlank() },
    phone = phone?.takeIf { it.isNotBlank() },
    timezone = timezone?.takeIf { it.isNotBlank() }
)

fun ChangeEmailCommand.toResource() = ChangeEmailResource(newEmail.value)

fun ChangePasswordCommand.toResource() =
    ChangePasswordResource(currentPassword.value, newPassword.value)

fun AuthenticatedUserResource.toSession() = AuthSession(userId = id, email = email, token = token)

fun UserResource.toEntity() = User(id = id, email = email, roles = roles.mapNotNull(Roles::fromName))
