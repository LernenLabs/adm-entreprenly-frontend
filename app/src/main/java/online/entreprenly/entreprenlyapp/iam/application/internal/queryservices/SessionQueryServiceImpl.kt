package online.entreprenly.entreprenlyapp.iam.application.internal.queryservices

import kotlinx.coroutines.flow.Flow
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.iam.domain.repositories.SessionRepository

class SessionQueryServiceImpl(private val sessionRepository: SessionRepository) : SessionQueryService {
    override fun handle(query: GetCurrentSessionQuery): Flow<AuthSession?> = sessionRepository.session
}
