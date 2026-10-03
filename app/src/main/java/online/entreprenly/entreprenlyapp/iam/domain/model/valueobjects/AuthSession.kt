package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

/** Authenticated session: user identity and the current JWT. */
data class AuthSession(val userId: Long, val email: String, val token: String)
