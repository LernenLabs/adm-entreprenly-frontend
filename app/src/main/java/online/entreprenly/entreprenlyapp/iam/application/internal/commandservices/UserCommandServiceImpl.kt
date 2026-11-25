package online.entreprenly.entreprenlyapp.iam.application.internal.commandservices

import kotlinx.coroutines.flow.first
import online.entreprenly.entreprenlyapp.iam.application.commandservices.UserCommandService
import online.entreprenly.entreprenlyapp.iam.domain.model.aggregates.User
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignOutCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Email
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Password
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.PasswordResetLink
import online.entreprenly.entreprenlyapp.iam.domain.repositories.SessionRepository
import online.entreprenly.entreprenlyapp.iam.domain.repositories.UserRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class UserCommandServiceImpl(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) : UserCommandService {

    override suspend fun requestPasswordReset(email: Email): Result<Unit> =
        userRepository.requestPasswordReset(email)

    override suspend fun validatePasswordReset(link: PasswordResetLink): Result<Unit> =
        userRepository.validatePasswordReset(link)

    override suspend fun confirmPasswordReset(link: PasswordResetLink, password: Password): Result<Unit> =
        userRepository.confirmPasswordReset(link, password).also { result ->
            if (result is Result.Success && sessionRepository.session.first()?.userId == link.userId) {
                sessionRepository.clear()
            }
        }

    override suspend fun handle(command: SignInCommand): Result<AuthSession> =
        userRepository.signIn(command).also {
            if (it is Result.Success) sessionRepository.save(it.value)
        }

    override suspend fun handle(command: SignUpCommand): Result<User> =
        userRepository.signUp(command)

    override suspend fun handle(command: ChangePasswordCommand): Result<Unit> =
        userRepository.changePassword(command)

    /** Request a confirmation link. The current email and session remain active while pending. */
    override suspend fun handle(command: ChangeEmailCommand): Result<Unit> =
        userRepository.changeEmail(command)

    override suspend fun handle(command: SignOutCommand) = sessionRepository.clear()
}
