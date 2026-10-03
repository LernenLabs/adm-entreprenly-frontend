package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

/** State shared by the chatbot screens (orders, conversations and order detail). */
sealed interface ChatbotState {
    data object Loading : ChatbotState
    /** The user's plan does not include the chatbot (access check false or HTTP 403). */
    data object PlanRequired : ChatbotState
    data class Error(val message: UiText) : ChatbotState
    data class Ready(
        val orders: List<ChatOrder>,
        val conversations: List<Conversation>,
        val connection: WhatsAppConnection?,
        val refreshing: Boolean = false
    ) : ChatbotState {
        fun order(id: Long): ChatOrder? = orders.firstOrNull { it.id == id }
        fun conversation(id: Long): Conversation? = conversations.firstOrNull { it.id == id }
    }
}

/** A 403 from any chatbot endpoint means the plan lacks the feature, not a broken session. */
internal fun ApplicationError.toChatbotState(): ChatbotState =
    if (this is ApplicationError.Forbidden) ChatbotState.PlanRequired else ChatbotState.Error(UiText.Raw(message))

internal fun ApplicationError.toUiText(): UiText =
    if (this is ApplicationError.Forbidden) UiText.Res(R.string.chatbot_plan_required_message) else UiText.Raw(message)

/** Orders tab: checks Plan Control access, then loads orders, conversations and WhatsApp state. */
class OrdersViewModel(
    private val subscriptionAccessFacade: SubscriptionAccessFacade,
    private val chatOrderQueryService: ChatOrderQueryService,
    private val conversationQueryService: ConversationQueryService,
    private val whatsAppConnectionQueryService: WhatsAppConnectionQueryService
) : ViewModel() {

    private val _state = MutableStateFlow<ChatbotState>(ChatbotState.Loading)
    val state: StateFlow<ChatbotState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Keeps the current content visible while reloading. */
    fun refresh() {
        val current = _state.value
        _state.value = if (current is ChatbotState.Ready) current.copy(refreshing = true) else ChatbotState.Loading
        viewModelScope.launch { _state.value = load() }
    }

    private suspend fun load(): ChatbotState = when (val access = subscriptionAccessFacade.hasChatbotAccess()) {
        is Result.Failure -> access.error.toChatbotState()
        is Result.Success -> if (!access.value) ChatbotState.PlanRequired else loadContent()
    }

    private suspend fun loadContent(): ChatbotState = coroutineScope {
        val orders = async { chatOrderQueryService.handle(GetAllChatOrdersQuery) }
        val conversations = async { conversationQueryService.handle(GetAllConversationsQuery) }
        val connection = async { whatsAppConnectionQueryService.handle(GetWhatsAppConnectionQuery) }
        when (val o = orders.await()) {
            is Result.Failure -> o.error.toChatbotState()
            is Result.Success -> when (val c = conversations.await()) {
                is Result.Failure -> c.error.toChatbotState()
                // The bridge state is informative only: a failure there does not block the screen.
                is Result.Success -> ChatbotState.Ready(
                    orders = o.value,
                    conversations = c.value,
                    connection = (connection.await() as? Result.Success)?.value
                )
            }
        }
    }
}
