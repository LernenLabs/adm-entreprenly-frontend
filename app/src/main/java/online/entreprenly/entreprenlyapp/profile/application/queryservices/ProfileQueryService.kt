package online.entreprenly.entreprenlyapp.profile.application.queryservices

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetLocalPreferencesQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetProfileByUserIdQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ProfileQueryService {
    suspend fun handle(query: GetProfileByUserIdQuery): Result<Profile>
    fun handle(query: GetLocalPreferencesQuery): Flow<Preferences?>
}
