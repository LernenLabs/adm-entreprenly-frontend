package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageSender
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageType
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.EmptyContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.ErrorContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.InitialsAvatar
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.LoadingContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.PlanRequiredContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.decodeDataUrl
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.shortLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer

private const val CHAT_POLL_MILLIS = 4_000L
private val RECEIPT_PREVIEW_HEIGHT = 260.dp

/** Conversation with a client: client bubbles on the right, the bot's on the left. */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    clientName: String?,
    /** Receipt photo for an image message (the backend keeps it on the order, not the message). */
    receiptFor: (ChatMessage) -> String?,
    onBack: () -> Unit,
    onViewPlans: () -> Unit,
    onMessagesChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // "At the bottom" also counts the row just above the last one, so a message that arrives
    // while the newest one is visible still scrolls into view.
    val atBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= info.totalItemsCount - 2
        }
    }
    var firstLoad by remember { mutableStateOf(true) }
    var scrollAfterSend by remember { mutableStateOf(false) }
    var unseen by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(CHAT_POLL_MILLIS)
            viewModel.poll()
        }
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isEmpty()) return@LaunchedEffect
        // Follow new messages only when the seller is already at the end (or just sent one):
        // jumping down while they read older messages made the history impossible to read.
        if (firstLoad || atBottom || scrollAfterSend) {
            if (firstLoad) listState.scrollToItem(state.messages.lastIndex)
            else listState.animateScrollToItem(state.messages.lastIndex)
            unseen = 0
        } else {
            unseen++
        }
        firstLoad = false
        scrollAfterSend = false
        // A new message may come with a receipt or an order change: reload the orders too.
        onMessagesChanged()
    }
    LaunchedEffect(atBottom) { if (atBottom) unseen = 0 }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ChatTopBar(clientName, onBack)
        Box(Modifier.weight(1f)) {
            when {
                state.loading -> LoadingContent()
                state.planRequired -> PlanRequiredContent(onViewPlans = onViewPlans, onRetry = viewModel::load)
                state.messages.isEmpty() && state.error != null -> ErrorContent(state.error!!, onRetry = viewModel::load)
                state.messages.isEmpty() -> EmptyContent(stringResource(R.string.chat_empty))
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().scrollbar(listState, MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageItem(message, clientName, receiptFor(message))
                        }
                    }
                    if (!atBottom) {
                        JumpToLatestButton(
                            unseen = unseen,
                            onClick = { scope.launch { listState.animateScrollToItem(state.messages.lastIndex) } },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                        )
                    }
                }
            }
        }
        if (!state.planRequired) {
            if (state.messages.isNotEmpty()) {
                state.error?.let {
                    Text(
                        it.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            MessageInput(
                draft = state.draft,
                onDraftChange = viewModel::onDraftChange,
                canSend = state.canSend,
                sending = state.sending,
                onSend = {
                    scrollAfterSend = true
                    viewModel.send()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(clientName: String?, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InitialsAvatar(clientName, size = 40.dp)
                Column {
                    Text(
                        clientName ?: stringResource(R.string.chatbot_unknown_client),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(stringResource(R.string.chat_handled_by_bot), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun MessageItem(message: ChatMessage, clientName: String?, receiptImage: String?) {
    when (message.sender) {
        MessageSender.SYSTEM -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        MessageSender.CLIENT -> Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.Bottom
        ) {
            Bubble(
                message,
                receiptImage = receiptImage,
                container = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                content = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                modifier = Modifier.weight(1f, fill = false)
            )
            InitialsAvatar(clientName, size = 32.dp)
        }
        MessageSender.BOT -> Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BotAvatar()
            Bubble(
                message,
                receiptImage = null,
                container = MaterialTheme.colorScheme.surface,
                content = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
private fun Bubble(
    message: ChatMessage,
    receiptImage: String?,
    container: Color,
    content: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(color = container, contentColor = content, shape = shape, shadowElevation = 1.dp, modifier = modifier.widthIn(max = 300.dp)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // The backend keeps the receipt photo on the order; the message only marks that one arrived.
            val image = remember(message.id, receiptImage) {
                if (message.type == MessageType.IMAGE) decodeDataUrl(message.content) ?: decodeDataUrl(receiptImage) else null
            }
            var expanded by remember { mutableStateOf(false) }
            when {
                // Capped so a tall screenshot does not fill the whole chat; tap to see it whole.
                image != null -> Image(
                    bitmap = image,
                    contentDescription = stringResource(R.string.chat_receipt_message),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth().heightIn(max = RECEIPT_PREVIEW_HEIGHT)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { expanded = true }
                )
                message.type == MessageType.IMAGE ->
                    Text(stringResource(R.string.chat_receipt_message), style = MaterialTheme.typography.bodyLarge)
                else -> Text(message.content, style = MaterialTheme.typography.bodyLarge)
            }
            if (expanded && image != null) {
                Dialog(onDismissRequest = { expanded = false }) {
                    Image(
                        bitmap = image,
                        contentDescription = stringResource(R.string.chat_receipt_message),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { expanded = false }
                    )
                }
            }
            message.sentAt?.let {
                Text(
                    it.shortLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/** Shown while reading older messages; [unseen] counts the ones that arrived meanwhile. */
@Composable
private fun JumpToLatestButton(unseen: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BadgedBox(
        badge = { if (unseen > 0) Badge { Text("$unseen") } },
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.chat_jump_to_latest))
        }
    }
}

/**
 * Thin scroll indicator on the right edge: it shows where in the conversation the seller is
 * and gets stronger while scrolling. Compose lists have none built in.
 */
private fun Modifier.scrollbar(state: LazyListState, color: Color): Modifier = composed {
    val alpha by animateFloatAsState(if (state.isScrollInProgress) 0.7f else 0.3f, label = "scrollbar")
    drawWithContent {
        drawContent()
        val info = state.layoutInfo
        val visible = info.visibleItemsInfo
        val total = info.totalItemsCount
        if (visible.isEmpty() || total == 0 || !(state.canScrollBackward || state.canScrollForward)) return@drawWithContent
        val first = visible.first()
        val firstFraction = (first.index + (-first.offset).toFloat() / first.size.coerceAtLeast(1)) / total
        val thumbHeight = (size.height * visible.size / total).coerceAtLeast(32.dp.toPx())
        val top = (size.height * firstFraction).coerceIn(0f, size.height - thumbHeight)
        drawRoundRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(size.width - 6.dp.toPx(), top),
            size = Size(4.dp.toPx(), thumbHeight),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}

@Composable
private fun BotAvatar() {
    Box(
        modifier = Modifier.size(32.dp).background(StatusSuccessContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Face, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun MessageInput(
    draft: String,
    onDraftChange: (String) -> Unit,
    canSend: Boolean,
    sending: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        // Stays above the gesture bar, and above the keyboard while it is open.
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text(stringResource(R.string.chat_message_hint)) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(onClick = onSend, enabled = canSend) {
                if (sending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chat_send))
            }
        }
    }
}
