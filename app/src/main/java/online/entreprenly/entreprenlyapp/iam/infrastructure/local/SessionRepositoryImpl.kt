package online.entreprenly.entreprenlyapp.iam.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.repositories.SessionRepository

private val Context.sessionDataStore by preferencesDataStore(name = "iam_session")

/** Persiste la sesión (JWT) en DataStore para sobrevivir al cierre de la app. */
class SessionRepositoryImpl(context: Context) : SessionRepository {

    private val dataStore = context.applicationContext.sessionDataStore

    override val session: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val id = prefs[USER_ID]
        val email = prefs[EMAIL]
        val token = prefs[TOKEN]
        if (id != null && email != null && token != null) AuthSession(id, email, token) else null
    }

    override suspend fun save(session: AuthSession) {
        dataStore.edit {
            it[USER_ID] = session.userId
            it[EMAIL] = session.email
            it[TOKEN] = session.token
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val USER_ID = longPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val TOKEN = stringPreferencesKey("token")
    }
}
