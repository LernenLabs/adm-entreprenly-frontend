package online.entreprenly.entreprenlyapp.profile.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppCurrency
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppLanguage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTheme
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTimezone
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences
import online.entreprenly.entreprenlyapp.profile.domain.repositories.LocalPreferencesRepository

private val Context.preferencesDataStore by preferencesDataStore(name = "profile_preferences")

/** Keeps the user's display preferences in DataStore so they apply instantly and survive restarts. */
class LocalPreferencesRepositoryImpl(context: Context) : LocalPreferencesRepository {

    private val dataStore = context.applicationContext.preferencesDataStore

    override val preferences: Flow<Preferences?> = dataStore.data.map { prefs ->
        val language = prefs[LANGUAGE] ?: return@map null
        Preferences(
            language = AppLanguage.fromCode(language),
            timezone = AppTimezone.fromId(prefs[TIMEZONE]),
            theme = AppTheme.fromCode(prefs[THEME]),
            currency = AppCurrency.fromCode(prefs[CURRENCY])
        )
    }

    override suspend fun save(preferences: Preferences) {
        dataStore.edit {
            it[LANGUAGE] = preferences.language.code
            it[TIMEZONE] = preferences.timezone.id
            it[THEME] = preferences.theme.code
            it[CURRENCY] = preferences.currency.code
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val TIMEZONE = stringPreferencesKey("timezone")
        val THEME = stringPreferencesKey("theme")
        val CURRENCY = stringPreferencesKey("currency")
    }
}
