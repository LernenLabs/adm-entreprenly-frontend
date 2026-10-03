package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.application.acl.SubscriptionAccessFacade
import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatMessageCommandService
import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatOrderCommandService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatMessageQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatOrderQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ConversationQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.WhatsAppConnectionQueryService
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.ApproveOrderPaymentCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.RejectOrderPaymentCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllChatOrdersQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllConversationsQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetChatMessagesByConversationIdQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetWhatsAppConnectionQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
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

/**
 * Orders tab: checks Plan Control access, then loads orders, conversations and WhatsApp state.
 * Also approves or rejects payment receipts from the order detail.
 */
class OrdersViewModel(
    private val subscriptionAccessFacade: SubscriptionAccessFacade,
    private val chatOrderQueryService: ChatOrderQueryService,
    private val conversationQueryService: ConversationQueryService,
    private val whatsAppConnectionQueryService: WhatsAppConnectionQueryService,
    private val chatOrderCommandService: ChatOrderCommandService
) : ViewModel() {

    private val _state = MutableStateFlow<ChatbotState>(ChatbotState.Loading)
    val state: StateFlow<ChatbotState> = _state.asStateFlow()

    /** Progress and outcome of the last approve/reject action. */
    private val _review = MutableStateFlow(FormState())
    val review: StateFlow<FormState> = _review.asStateFlow()

    init {
        refresh()
    }

    fun approvePayment(order: ChatOrder) = review(R.string.order_payment_approved) {
        chatOrderCommandService.handle(ApproveOrderPaymentCommand(order.id))
    }

    /** [message] is the reason the bot sends to the client. */
    fun rejectPayment(order: ChatOrder, message: String) = review(R.string.order_payment_rejected) {
        chatOrderCommandService.handle(RejectOrderPaymentCommand(order.id, order.conversationId, message))
    }

    fun clearReview() {
        _review.value = FormState()
    }

    /** On success the list is reloaded: the backend already deducted stock / counted the rejection. */
    private fun review(@StringRes success: Int, action: suspend () -> Result<ChatOrder>) {
        if (_review.value.loading) return
        _review.value = FormState(loading = true)
        viewModelScope.launch {
            _review.value = when (val r = action()) {
                is Result.Success -> {
                    refresh()
                    FormState(success = UiText.Res(success))
                }
                is Result.Failure -> FormState(error = r.error.toUiText())
            }
        }
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

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val loading: Boolean = true,
    val sending: Boolean = false,
    val planRequired: Boolean = false,
    /** Load error when [messages] is empty; otherwise a failed send. */
    val error: UiText? = null
) {
    val canSend: Boolean get() = draft.isNotBlank() && !sending
}

/** One conversation: the seller reads the history and replies as the bot. */
class ChatViewModel(
    private val conversationId: Long,
    private val chatMessageQueryService: ChatMessageQueryService,
    private val chatMessageCommandService: ChatMessageCommandService
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = chatMessageQueryService.handle(GetChatMessagesByConversationIdQuery(conversationId))
            _state.update {
                when (r) {
                    is Result.Success -> it.copy(messages = r.value, loading = false)
                    is Result.Failure -> it.copy(
                        loading = false,
                        planRequired = r.error is ApplicationError.Forbidden,
                        error = r.error.toUiText()
                    )
                }
            }
        }
    }

    fun onDraftChange(text: String) = _state.update { it.copy(draft = text, error = null) }

    fun send() {
        val current = _state.value
        if (!current.canSend) return
        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val r = chatMessageCommandService.handle(SendChatMessageCommand(conversationId, current.draft))
            _state.update {
                when (r) {
                    is Result.Success -> it.copy(messages = it.messages + r.value, draft = "", sending = false)
                    // Keep the draft so the seller can retry.
                    is Result.Failure -> it.copy(sending = false, error = r.error.toUiText())
                }
            }
        }
    }
}
