package online.entreprenly.entreprenlyapp.iam.domain.repositories

import online.entreprenly.entreprenlyapp.iam.domain.model.aggregates.User
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.shared.application.result.Result

/** Puerto hacia el IAM del backend. */
interface UserRepository {
    suspend fun signIn(command: SignInCommand): Result<AuthSession>
    suspend fun signUp(command: SignUpCommand): Result<User>
    suspend fun changePassword(command: ChangePasswordCommand): Result<Unit>
    suspend fun changeEmail(command: ChangeEmailCommand): Result<Unit>
}
