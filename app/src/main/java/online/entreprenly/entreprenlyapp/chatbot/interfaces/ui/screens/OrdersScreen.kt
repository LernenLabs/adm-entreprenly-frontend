package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.EmptyContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.ErrorContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.InitialsAvatar
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.LoadingContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.PlanRequiredContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.StatusBadge
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.formatMoney
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.paymentLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.shortLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.statusStyle
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatbotState
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.OrdersViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutral
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess

private enum class ChatbotTab(@StringRes val label: Int) {
    ORDERS(R.string.chatbot_tab_orders),
    CONVERSATIONS(R.string.chatbot_tab_conversations)
}

private enum class OrderFilter(@StringRes val label: Int, val matches: (ChatOrder) -> Boolean) {
    ALL(R.string.chatbot_filter_all, { true }),
    TO_VALIDATE(R.string.chatbot_filter_to_validate, { it.awaitingValidation }),
    CONFIRMED(R.string.chatbot_filter_confirmed, { it.status == OrderStatus.CONFIRMED }),
    CANCELLED(R.string.chatbot_filter_cancelled, {
        it.status == OrderStatus.CANCELLED || it.status == OrderStatus.BLOCKED
    })
}

/** Chatbot orders and conversations received through WhatsApp. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onOpenOrder: (Long) -> Unit,
    onOpenConversation: (Long) -> Unit,
    onViewPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var tab by rememberSaveable { mutableStateOf(ChatbotTab.ORDERS) }
    var filter by rememberSaveable { mutableStateOf(OrderFilter.ALL) }

    Column(modifier.fillMaxSize()) {
        OrdersTopBar(
            connection = (state as? ChatbotState.Ready)?.connection,
            onRefresh = viewModel::refresh
        )
        when (val s = state) {
            ChatbotState.Loading -> LoadingContent()
            ChatbotState.PlanRequired -> PlanRequiredContent(onViewPlans = onViewPlans, onRetry = viewModel::refresh)
            is ChatbotState.Error -> ErrorContent(s.message, onRetry = viewModel::refresh)
            is ChatbotState.Ready -> {
                TabSelector(tab, onSelect = { tab = it }, modifier = Modifier.padding(16.dp))
                PullToRefreshBox(
                    isRefreshing = s.refreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (tab) {
                        ChatbotTab.ORDERS -> OrdersTab(s, filter, { filter = it }, onOpenOrder)
                        ChatbotTab.CONVERSATIONS -> ConversationsTab(s.conversations, onOpenConversation)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersTopBar(connection: WhatsAppConnection?, onRefresh: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.chatbot_orders_title), fontWeight = FontWeight.Bold)
                if (connection != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(8.dp).background(
                                if (connection.connected) StatusSuccess else StatusNeutral,
                                CircleShape
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(
                                if (connection.connected) R.string.chatbot_connected
                                else R.string.chatbot_disconnected
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.chatbot_refresh))
            }
        },
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun TabSelector(selected: ChatbotTab, onSelect: (ChatbotTab) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        ChatbotTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                shape = SegmentedButtonDefaults.itemShape(index, ChatbotTab.entries.size),
                icon = {}
            ) { Text(stringResource(tab.label)) }
        }
    }
}

@Composable
private fun OrdersTab(
    state: ChatbotState.Ready,
    filter: OrderFilter,
    onFilter: (OrderFilter) -> Unit,
    onOpenOrder: (Long) -> Unit
) {
    val visible = state.orders.filter(filter.matches)
    val toValidate = state.orders.count(OrderFilter.TO_VALIDATE.matches)

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(OrderFilter.entries) { option ->
                val label = stringResource(option.label)
                FilterChip(
                    selected = option == filter,
                    onClick = { onFilter(option) },
                    label = {
                        Text(
                            if (option == OrderFilter.TO_VALIDATE && toValidate > 0)
                                stringResource(R.string.chatbot_filter_with_count, label, toValidate)
                            else label
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    )
                )
            }
        }
        if (visible.isEmpty()) {
            EmptyContent(
                stringResource(
                    if (state.orders.isEmpty()) R.string.chatbot_empty_orders
                    else R.string.chatbot_empty_orders_filter
                )
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visible, key = { it.id }) { order ->
                    OrderCard(order, state.conversation(order.conversationId), onClick = { onOpenOrder(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: ChatOrder, conversation: Conversation?, onClick: () -> Unit) {
    val clientName = conversation?.displayName ?: stringResource(R.string.chatbot_unknown_client)
    val subtitle = listOfNotNull(
        paymentLabel(order.paymentMethod),
        formatMoney(order.total),
        order.createdAt?.shortLabel()
    ).joinToString(" · ")

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(conversation?.clientName)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.chatbot_order_title, order.orderNumber, clientName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(order.statusStyle())
        }
    }
}

@Composable
private fun ConversationsTab(conversations: List<Conversation>, onOpenConversation: (Long) -> Unit) {
    if (conversations.isEmpty()) {
        EmptyContent(stringResource(R.string.chatbot_empty_conversations))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                conversations.forEachIndexed { index, conversation ->
                    if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ConversationRow(conversation, onClick = { onOpenConversation(conversation.id) })
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InitialsAvatar(conversation.clientName, size = 48.dp)
        Column(Modifier.weight(1f)) {
            Text(
                conversation.displayName ?: stringResource(R.string.chatbot_unknown_client),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            conversation.lastMessage?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        conversation.lastMessageTime?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
