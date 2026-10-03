package online.entreprenly.entreprenlyapp.shared.infrastructure.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.BuildConfig
import online.entreprenly.entreprenlyapp.chatbot.application.acl.SubscriptionAccessFacade
import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatMessageCommandService
import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatOrderCommandService
import online.entreprenly.entreprenlyapp.chatbot.application.internal.commandservices.ChatMessageCommandServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.internal.commandservices.ChatOrderCommandServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.internal.queryservices.ChatMessageQueryServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.internal.queryservices.ChatOrderQueryServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.internal.queryservices.ConversationQueryServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.internal.queryservices.WhatsAppConnectionQueryServiceImpl
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatMessageQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatOrderQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ConversationQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.WhatsAppConnectionQueryService
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl.SubscriptionAccessApi
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl.SubscriptionAccessFacadeImpl
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.api.ChatbotApi
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.repositories.ChatMessageRepositoryImpl
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.repositories.ChatOrderRepositoryImpl
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.repositories.ConversationRepositoryImpl
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.repositories.WhatsAppConnectionRepositoryImpl
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

    // Chatbot
    private val chatbotApi = retrofit.create(ChatbotApi::class.java)
    private val chatMessageRepository = ChatMessageRepositoryImpl(chatbotApi)
    private val chatOrderRepository = ChatOrderRepositoryImpl(chatbotApi)

    val conversationQueryService: ConversationQueryService =
        ConversationQueryServiceImpl(ConversationRepositoryImpl(chatbotApi))
    val chatMessageQueryService: ChatMessageQueryService = ChatMessageQueryServiceImpl(chatMessageRepository)
    val chatMessageCommandService: ChatMessageCommandService = ChatMessageCommandServiceImpl(chatMessageRepository)
    val chatOrderQueryService: ChatOrderQueryService = ChatOrderQueryServiceImpl(chatOrderRepository)
    val chatOrderCommandService: ChatOrderCommandService =
        ChatOrderCommandServiceImpl(chatOrderRepository, chatMessageRepository)
    val whatsAppConnectionQueryService: WhatsAppConnectionQueryService =
        WhatsAppConnectionQueryServiceImpl(WhatsAppConnectionRepositoryImpl(chatbotApi))
    val subscriptionAccessFacade: SubscriptionAccessFacade = SubscriptionAccessFacadeImpl(
        retrofit.create(SubscriptionAccessApi::class.java),
        sessionQueryService
    )

    init {
        // Mantiene el token en memoria para el interceptor HTTP.
        scope.launch { sessionRepository.session.collect { token = it?.token } }
    }
}
