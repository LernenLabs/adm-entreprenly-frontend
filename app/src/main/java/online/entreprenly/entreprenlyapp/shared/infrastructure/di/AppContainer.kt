package online.entreprenly.entreprenlyapp.shared.infrastructure.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.BuildConfig
import online.entreprenly.entreprenlyapp.iam.application.commandservices.UserCommandService
import online.entreprenly.entreprenlyapp.iam.application.internal.commandservices.UserCommandServiceImpl
import online.entreprenly.entreprenlyapp.iam.application.internal.queryservices.SessionQueryServiceImpl
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.infrastructure.local.SessionRepositoryImpl
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.api.IamApi
import online.entreprenly.entreprenlyapp.iam.infrastructure.remote.repositories.UserRepositoryImpl
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.RetrofitFactory

/** Composition root manual (sin framework de DI). Un solo contenedor por proceso. */
class AppContainer(context: Context) {

    @Volatile
    private var token: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val retrofit = RetrofitFactory(
        baseUrl = BuildConfig.API_BASE_URL,
        tokenProvider = { token },
        debug = BuildConfig.DEBUG
    ).retrofit

    private val sessionRepository = SessionRepositoryImpl(context)
    private val userRepository = UserRepositoryImpl(retrofit.create(IamApi::class.java))

    val userCommandService: UserCommandService =
        UserCommandServiceImpl(userRepository, sessionRepository)
    val sessionQueryService: SessionQueryService = SessionQueryServiceImpl(sessionRepository)

    init {
        // Mantiene el token en memoria para el interceptor HTTP.
        scope.launch { sessionRepository.session.collect { token = it?.token } }
    }
}
