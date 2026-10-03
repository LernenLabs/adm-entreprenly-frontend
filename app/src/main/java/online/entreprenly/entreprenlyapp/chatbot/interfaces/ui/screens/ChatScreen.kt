package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

/** Conversation with a client: client bubbles on the right, the bot's on the left. */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    clientName: String?,
    onBack: () -> Unit,
    onViewPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ChatTopBar(clientName, onBack)
        Box(Modifier.weight(1f)) {
            when {
                state.loading -> LoadingContent()
                state.planRequired -> PlanRequiredContent(onViewPlans = onViewPlans, onRetry = viewModel::load)
                state.messages.isEmpty() && state.error != null -> ErrorContent(state.error!!, onRetry = viewModel::load)
                state.messages.isEmpty() -> EmptyContent(stringResource(R.string.chat_empty))
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.messages, key = { it.id }) { message -> MessageItem(message, clientName) }
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
                onSend = viewModel::send
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
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun MessageItem(message: ChatMessage, clientName: String?) {
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
    container: Color,
    content: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Surface(color = container, contentColor = content, shape = shape, shadowElevation = 1.dp, modifier = modifier.widthIn(max = 300.dp)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            val image = remember(message.id) {
                if (message.type == MessageType.IMAGE) decodeDataUrl(message.content) else null
            }
            when {
                image != null -> Image(
                    bitmap = image,
                    contentDescription = stringResource(R.string.chat_image_message),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
                message.type == MessageType.IMAGE -> Text(stringResource(R.string.chat_image_message))
                else -> Text(message.content, style = MaterialTheme.typography.bodyLarge)
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
        // The activity already pads the navigation bar; only add what the keyboard covers beyond it.
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
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
