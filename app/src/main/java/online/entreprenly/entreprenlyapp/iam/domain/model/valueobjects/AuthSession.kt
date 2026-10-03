package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

/** Sesión autenticada: identidad del usuario y JWT vigente. */
data class AuthSession(val userId: Long, val email: String, val token: String)
