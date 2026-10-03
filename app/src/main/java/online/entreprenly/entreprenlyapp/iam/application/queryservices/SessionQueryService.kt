package online.entreprenly.entreprenlyapp.iam.application.queryservices

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession

interface SessionQueryService {
    fun handle(query: GetCurrentSessionQuery): Flow<AuthSession?>
}
