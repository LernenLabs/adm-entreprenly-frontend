package online.entreprenly.entreprenlyapp.profile.application.internal.queryservices

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.profile.application.queryservices.ProfileQueryService
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetLocalPreferencesQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetProfileByUserIdQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences
import online.entreprenly.entreprenlyapp.profile.domain.repositories.LocalPreferencesRepository
import online.entreprenly.entreprenlyapp.profile.domain.repositories.ProfileRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ProfileQueryServiceImpl(
    private val profileRepository: ProfileRepository,
    private val localPreferences: LocalPreferencesRepository
) : ProfileQueryService {

    /** Loads the profile and caches its preferences so theme/language apply on the next launch. */
    override suspend fun handle(query: GetProfileByUserIdQuery): Result<Profile> =
        profileRepository.getByUserId(query).also {
            if (it is Result.Success) localPreferences.save(it.value.preferences)
        }

    override fun handle(query: GetLocalPreferencesQuery): Flow<Preferences?> = localPreferences.preferences
}
