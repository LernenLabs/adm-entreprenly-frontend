package online.entreprenly.entreprenlyapp.shared.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.application.acl.SubscriptionAccessFacade
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatOrderQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ConversationQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.WhatsAppConnectionQueryService
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllChatOrdersQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllConversationsQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetWhatsAppConnectionQuery
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.statusStyle
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.ProductQueryService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.StockAlertQueryService
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllProductsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetStockAlertsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertSeverity
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertType
import online.entreprenly.entreprenlyapp.sales.application.queryservices.SalesQueryService
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeAlert
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeAlertKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeOrder
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeUiState

private const val MAX_ALERTS = 3
private const val MAX_RECENT_ORDERS = 3

/**
 * Home dashboard: gathers today's sales, stock alerts and the chatbot's orders and connection.
 * Each source fails on its own: whatever could not be loaded keeps its last known value, so a
 * momentary network error does not turn the real figures into zeros.
 */
class HomeViewModel(
    private val salesQueryService: SalesQueryService,
    private val stockAlertQueryService: StockAlertQueryService,
    private val productQueryService: ProductQueryService,
    private val subscriptionAccessFacade: SubscriptionAccessFacade,
    private val chatOrderQueryService: ChatOrderQueryService,
    private val conversationQueryService: ConversationQueryService,
    private val whatsAppConnectionQueryService: WhatsAppConnectionQueryService
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch { _state.value = load(_state.value) }
    }

    private suspend fun load(current: HomeUiState): HomeUiState = coroutineScope {
        val sales = async { salesQueryService.getDailySummary(LocalDate.now()) }
        val alerts = async { stockAlertQueryService.handle(GetStockAlertsQuery) }
        val products = async { productQueryService.handle(GetAllProductsQuery) }
        val chatbot = async { loadChatbot() }

        var next = current
        (sales.await() as? Result.Success)?.value?.let {
            next = next.copy(
                salesToday = it.total,
                cashToday = it.cashTotal,
                digitalToday = it.digitalTotal,
                salesCount = it.count
            )
        }
        (alerts.await() as? Result.Success)?.value?.let {
            next = next.copy(
                expiringLots = it.count { alert -> alert.alertType == AlertType.EXPIRING_SOON },
                alerts = it.toHomeAlerts()
            )
        }
        (products.await() as? Result.Success)?.value?.let { next = next.copy(productCount = it.size) }
        chatbot.await()?.let { (connected, orders, conversations) ->
            next = next.copy(
                chatbotConnected = connected,
                ordersToValidate = orders.count { it.awaitingValidation },
                recentOrders = orders.toHomeOrders(conversations)
            )
        }
        next
    }

    /** Null when it could not be checked; a plan without the chatbot is simply "not connected". */
    private suspend fun loadChatbot(): Triple<Boolean, List<ChatOrder>, List<Conversation>>? = coroutineScope {
        when (val access = subscriptionAccessFacade.hasChatbotAccess()) {
            is Result.Failure -> null
            is Result.Success -> {
                if (!access.value) return@coroutineScope Triple(false, emptyList(), emptyList())
                val orders = async { chatOrderQueryService.handle(GetAllChatOrdersQuery) }
                val conversations = async { conversationQueryService.handle(GetAllConversationsQuery) }
                val connection = async { whatsAppConnectionQueryService.handle(GetWhatsAppConnectionQuery) }
                val loadedOrders = (orders.await() as? Result.Success)?.value ?: return@coroutineScope null
                val loadedConversations = (conversations.await() as? Result.Success)?.value.orEmpty()
                val connected = (connection.await() as? Result.Success)?.value?.connected == true
                Triple(connected, loadedOrders, loadedConversations)
            }
        }
    }

    /** The most urgent first; one line per product and problem, since each lot raises its own alert. */
    private fun List<StockAlert>.toHomeAlerts(): List<HomeAlert> =
        filter { it.alertType != null }
            .distinctBy { it.productId to it.alertType }
            .sortedByDescending { it.isUrgent() }
            .take(MAX_ALERTS)
            .map { alert ->
                HomeAlert(
                    kind = if (alert.isUrgent()) HomeAlertKind.ERROR else HomeAlertKind.WARNING,
                    title = when (alert.alertType) {
                        AlertType.EXPIRED -> R.string.home_alert_expired
                        AlertType.OUT_OF_STOCK -> R.string.home_alert_out_of_stock
                        AlertType.EXPIRING_SOON -> R.string.home_alert_expiring_soon
                        else -> R.string.home_alert_low_stock
                    },
                    detail = alert.productName.orEmpty()
                )
            }

    private fun StockAlert.isUrgent(): Boolean = severity == AlertSeverity.CRITICAL ||
        alertType == AlertType.EXPIRED || alertType == AlertType.OUT_OF_STOCK

    /** Newest first, with the ones waiting for the seller on top. */
    private fun List<ChatOrder>.toHomeOrders(conversations: List<Conversation>): List<HomeOrder> =
        sortedByDescending { it.awaitingValidation }
            .take(MAX_RECENT_ORDERS)
            .map { order ->
                HomeOrder(
                    code = order.orderNumber,
                    customer = conversations.firstOrNull { it.id == order.conversationId }?.displayName,
                    total = order.total,
                    status = order.statusStyle().label
                )
            }
}
