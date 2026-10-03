package online.entreprenly.entreprenlyapp.profile.domain.repositories

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences

/** Port for the preferences cached on the device. */
interface LocalPreferencesRepository {
    val preferences: Flow<Preferences?>
    suspend fun save(preferences: Preferences)
    suspend fun clear()
}
