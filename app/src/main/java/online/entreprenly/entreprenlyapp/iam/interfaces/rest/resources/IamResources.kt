package online.entreprenly.entreprenlyapp.iam.interfaces.rest.resources

// DTOs del IAM del backend (POST api/v1/authentication, PUT api/v1/users/me).

data class SignInResource(val email: String, val password: String)

data class SignUpResource(
    val email: String,
    val password: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val timezone: String?
)

data class AuthenticatedUserResource(val id: Long, val email: String, val token: String)

data class UserResource(val id: Long, val email: String, val roles: List<String>)

data class ChangeEmailResource(val email: String)

data class ChangePasswordResource(val currentPassword: String, val newPassword: String)
