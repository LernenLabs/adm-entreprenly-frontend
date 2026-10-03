package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDanger
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDangerContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutral
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutralContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarning
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarningContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

/** Label and colors used to show an order's status. */
data class OrderStatusStyle(@StringRes val label: Int, val content: Color, val container: Color)

fun ChatOrder.statusStyle(): OrderStatusStyle = when {
    awaitingValidation ->
        OrderStatusStyle(R.string.chatbot_status_to_validate, StatusWarning, StatusWarningContainer)
    status == OrderStatus.WAITING_PAYMENT ->
        OrderStatusStyle(R.string.chatbot_status_waiting_payment, StatusNeutral, StatusNeutralContainer)
    status == OrderStatus.CONFIRMED ->
        OrderStatusStyle(R.string.chatbot_status_confirmed, StatusSuccess, StatusSuccessContainer)
    status == OrderStatus.CANCELLED ->
        OrderStatusStyle(R.string.chatbot_status_cancelled, StatusDanger, StatusDangerContainer)
    status == OrderStatus.BLOCKED ->
        OrderStatusStyle(R.string.chatbot_status_blocked, StatusDanger, StatusDangerContainer)
    else -> OrderStatusStyle(R.string.chatbot_status_pending, StatusNeutral, StatusNeutralContainer)
}

@Composable
fun StatusBadge(style: OrderStatusStyle, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(style.label),
        color = style.content,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(style.container, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Circle with the client's initials, as in the reference designs. */
@Composable
fun InitialsAvatar(name: String?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier.size(size).background(StatusWarningContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            color = StatusWarning,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorContent(message: UiText, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CenteredMessage(modifier) {
        Text(message.asString(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.chatbot_retry)) }
    }
}

@Composable
fun EmptyContent(text: String, modifier: Modifier = Modifier) {
    CenteredMessage(modifier) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

/** Shown when the user's plan does not include the chatbot. */
@Composable
fun PlanRequiredContent(onViewPlans: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CenteredMessage(modifier) {
        Box(
            modifier = Modifier.size(72.dp).background(StatusWarningContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(36.dp))
        }
        Text(
            stringResource(R.string.chatbot_plan_required_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.chatbot_plan_required_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Button(onClick = onViewPlans, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.chatbot_view_plans))
        }
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.chatbot_retry))
        }
    }
}

@Composable
private fun CenteredMessage(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}
