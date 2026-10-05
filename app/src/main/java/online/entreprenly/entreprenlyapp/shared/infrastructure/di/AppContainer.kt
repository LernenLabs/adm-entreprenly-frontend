package online.entreprenly.entreprenlyapp.shared.infrastructure.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.BuildConfig
<<<<<<< Updated upstream
=======
import online.entreprenly.entreprenlyapp.subscription.application.internal.commandservices.SubscriptionCommandServiceImpl
import online.entreprenly.entreprenlyapp.subscription.application.internal.queryservices.SubscriptionQueryServiceImpl
import online.entreprenly.entreprenlyapp.subscription.application.commandservices.SubscriptionCommandService
import online.entreprenly.entreprenlyapp.subscription.application.queryservices.SubscriptionQueryService
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.api.SubscriptionApi
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.repositories.SubscriptionRepositoryImpl
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
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
    // Profile
    private val localPreferences = LocalPreferencesRepositoryImpl(context)
    private val profileRepository = ProfileRepositoryImpl(retrofit.create(ProfilesApi::class.java))

    val profileCommandService: ProfileCommandService =
        ProfileCommandServiceImpl(profileRepository, localPreferences)
    val profileQueryService: ProfileQueryService =
        ProfileQueryServiceImpl(profileRepository, localPreferences)

    // Subscription
    private val subscriptionRepository = SubscriptionRepositoryImpl(
        retrofit.create(SubscriptionApi::class.java)
    )
    val subscriptionQueryService: SubscriptionQueryService =
        SubscriptionQueryServiceImpl(subscriptionRepository)
    val subscriptionCommandService: SubscriptionCommandService =
        SubscriptionCommandServiceImpl(subscriptionRepository)

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

    // Inventory
    private val inventoryApi = retrofit.create(InventoryApi::class.java)
    private val productRepository = ProductRepositoryImpl(inventoryApi)
    private val lotRepository = LotRepositoryImpl(inventoryApi)
    private val stockAlertRepository = StockAlertRepositoryImpl(inventoryApi)

    val productQueryService: ProductQueryService = ProductQueryServiceImpl(productRepository)
    val lotQueryService: LotQueryService = LotQueryServiceImpl(lotRepository)
    val stockAlertQueryService: StockAlertQueryService = StockAlertQueryServiceImpl(stockAlertRepository)
    val productCommandService: ProductCommandService =
        ProductCommandServiceImpl(productRepository, lotRepository)
    val lotCommandService: LotCommandService = LotCommandServiceImpl(lotRepository)

    // Sales
    private val salesApi = retrofit.create(online.entreprenly.entreprenlyapp.sales.infrastructure.remote.api.SalesApi::class.java)
    private val salesRepository = online.entreprenly.entreprenlyapp.sales.infrastructure.remote.repositories.SalesRepositoryImpl(salesApi)
    
    val salesQueryService: online.entreprenly.entreprenlyapp.sales.application.queryservices.SalesQueryService = 
        online.entreprenly.entreprenlyapp.sales.application.internal.queryservices.SalesQueryServiceImpl(salesRepository)
    val salesCommandService: online.entreprenly.entreprenlyapp.sales.application.commandservices.SalesCommandService = 
        online.entreprenly.entreprenlyapp.sales.application.internal.commandservices.SalesCommandServiceImpl(salesRepository)

>>>>>>> Stashed changes
    init {
        // Mantiene el token en memoria para el interceptor HTTP.
        scope.launch { sessionRepository.session.collect { token = it?.token } }
    }
}
