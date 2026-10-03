package online.entreprenly.entreprenlyapp.iam.domain.repositories

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession

/** Port for local session persistence. */
interface SessionRepository {
    val session: Flow<AuthSession?>
    suspend fun save(session: AuthSession)
    suspend fun clear()
}
