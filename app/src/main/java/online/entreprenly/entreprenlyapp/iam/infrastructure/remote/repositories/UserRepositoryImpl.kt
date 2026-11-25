package online.entreprenly.entreprenlyapp.iam.infrastructure.remote.repositories

import online.entreprenly.entreprenlyapp.iam.domain.model.aggregates.User
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Password
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.PasswordResetLink
import online.entreprenly.entreprenlyapp.iam.domain.repositories.UserRepository
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.api.IamApi
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.ChangeEmailResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.PasswordResetLinkResource
import online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources.PasswordResetResource
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.assemblers.toEntity
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.assemblers.toResource
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.assemblers.toSession
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class UserRepositoryImpl(private val api: IamApi) : UserRepository {

    override suspend fun requestPasswordReset(email: Email): Result<Unit> =
        safeApiCall({ api.requestPasswordReset(ChangeEmailResource(email.value)) }) { }

    override suspend fun validatePasswordReset(link: PasswordResetLink): Result<Unit> =
        safeApiCall({ api.validatePasswordReset(PasswordResetLinkResource(link.userId, link.token)) }) { }

    override suspend fun confirmPasswordReset(link: PasswordResetLink, password: Password): Result<Unit> =
        safeApiCall({ api.confirmPasswordReset(PasswordResetResource(link.userId, link.token, password.value)) }) { }

    override suspend fun signIn(command: SignInCommand): Result<AuthSession> =
        safeApiCallWithBody({ api.signIn(command.toResource()) }) { it.toSession() }

    override suspend fun signUp(command: SignUpCommand): Result<User> =
        safeApiCallWithBody({ api.signUp(command.toResource()) }) { it.toEntity() }

    override suspend fun changePassword(command: ChangePasswordCommand): Result<Unit> =
        safeApiCall({ api.changePassword(command.toResource()) }) { }

    override suspend fun changeEmail(command: ChangeEmailCommand): Result<Unit> =
        safeApiCall({ api.changeEmail(command.toResource()) }) { }
}
