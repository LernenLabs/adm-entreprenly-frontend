package online.entreprenly.entreprenlyapp.iam.application.commandservices

import online.entreprenly.entreprenlyapp.iam.domain.model.aggregates.User
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangeEmailCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.ChangePasswordCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignInCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignOutCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.commands.SignUpCommand
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface UserCommandService {
    suspend fun handle(command: SignInCommand): Result<AuthSession>
    suspend fun handle(command: SignUpCommand): Result<User>
    suspend fun handle(command: ChangePasswordCommand): Result<Unit>
    suspend fun handle(command: ChangeEmailCommand): Result<Unit>
    suspend fun handle(command: SignOutCommand)
}
