package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.EmptyContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.ErrorContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.LoadingContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.PlanRequiredContent
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.StatusBadge
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.decodeDataUrl
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.formatMoney
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.fullLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.paymentLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.shortLabel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components.statusStyle
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatbotState
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.OrdersViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.TextInputField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDanger
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDangerContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusInfo
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutral
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarning

private enum class RejectReason(@StringRes val label: Int) {
    INCORRECT_AMOUNT(R.string.order_reject_reason_amount),
    UNREADABLE(R.string.order_reject_reason_unreadable),
    DUPLICATE(R.string.order_reject_reason_duplicate),
    OTHER(R.string.order_reject_reason_other)
}

/** Order detail: receipt, items, timeline and the approve/reject decision on the payment. */
@Composable
fun OrderDetailScreen(
    viewModel: OrdersViewModel,
    orderId: Long,
    onBack: () -> Unit,
    onViewPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val review by viewModel.review.collectAsState()
    var showReject by rememberSaveable { mutableStateOf(false) }
    var showApprove by rememberSaveable { mutableStateOf(false) }

    // The review outcome belongs to this order only.
    LaunchedEffect(orderId) { viewModel.clearReview() }

    val ready = state as? ChatbotState.Ready
    val order = ready?.order(orderId)
    val clientName = order?.let { ready.conversation(it.conversationId)?.displayName }
        ?: stringResource(R.string.chatbot_unknown_client)

    Column(modifier.fillMaxSize()) {
        DetailTopBar(order, clientName, onBack)
        Box(Modifier.weight(1f)) {
            when (val s = state) {
                ChatbotState.Loading -> LoadingContent()
                ChatbotState.PlanRequired -> PlanRequiredContent(onViewPlans = onViewPlans, onRetry = viewModel::refresh)
                is ChatbotState.Error -> ErrorContent(s.message, onRetry = viewModel::refresh)
                is ChatbotState.Ready ->
                    if (order == null) EmptyContent(stringResource(R.string.order_not_found))
                    else OrderContent(order, review)
            }
        }
        if (order != null && order.awaitingValidation) {
            ReviewActions(
                loading = review.loading,
                onReject = { showReject = true },
                onApprove = { showApprove = true }
            )
        }
    }

    if (order != null && showReject) {
        RejectPaymentSheet(
            order = order,
            onDismiss = { showReject = false },
            onConfirm = { message ->
                showReject = false
                viewModel.rejectPayment(order, message)
            }
        )
    }
    if (order != null && showApprove) {
        ApprovePaymentDialog(
            order = order,
            clientName = clientName,
            onDismiss = { showApprove = false },
            onConfirm = {
                showApprove = false
                viewModel.approvePayment(order)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(order: ChatOrder?, clientName: String, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
            }
        },
        title = {
            if (order != null) {
                Column {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(clientName, paymentLabel(order.paymentMethod), formatMoney(order.total))
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
private fun OrderContent(order: ChatOrder, review: FormState) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(order.statusStyle())
            Spacer(Modifier.weight(1f))
            order.createdAt?.let {
                Text(it.fullLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        review.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        review.success?.let { Text(it.asString(), color = StatusSuccess, fontWeight = FontWeight.SemiBold) }

        DetailCard {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReceiptImage(order.receiptImage)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    order.items.forEach { item ->
                        Row {
                            Text(
                                stringResource(R.string.order_item_line, item.quantity, item.productName),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(formatMoney(item.subtotal), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    order.deliveryAddress?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            stringResource(R.string.order_detail_delivery, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                    Row {
                        Text(
                            stringResource(R.string.order_detail_total),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(formatMoney(order.total), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        DetailCard {
            Text(
                stringResource(R.string.order_detail_timeline).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Timeline(order)
        }
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun ReceiptImage(dataUrl: String?) {
    val bitmap = remember(dataUrl) { decodeDataUrl(dataUrl) }
    val shape = RoundedCornerShape(12.dp)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.order_detail_receipt),
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(104.dp).height(140.dp).clip(shape)
        )
    } else {
        Box(
            modifier = Modifier.width(104.dp).height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.order_detail_no_receipt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Steps derived from the order's state (the backend has no event history endpoint). */
@Composable
private fun Timeline(order: ChatOrder) {
    val registered = stringResource(R.string.order_timeline_registered)
    val steps = buildList {
        add(
            (order.createdAt?.let { stringResource(R.string.order_timeline_with_time, registered, it.shortLabel()) }
                ?: registered) to StatusSuccess
        )
        if (order.rejectionCount > 0) {
            add(
                pluralStringResource(R.plurals.order_timeline_rejections, order.rejectionCount, order.rejectionCount)
                    to StatusDanger
            )
        }
        if (order.hasReceipt) add(stringResource(R.string.order_timeline_receipt_received) to StatusWarning)
        add(
            when {
                order.awaitingValidation -> stringResource(R.string.order_timeline_pending_validation) to StatusWarning
                order.status == OrderStatus.WAITING_PAYMENT -> stringResource(R.string.order_timeline_awaiting_payment) to StatusInfo
                order.status == OrderStatus.CONFIRMED -> stringResource(R.string.order_timeline_confirmed) to StatusSuccess
                order.status == OrderStatus.CANCELLED -> stringResource(R.string.order_timeline_cancelled) to StatusDanger
                order.status == OrderStatus.BLOCKED -> stringResource(R.string.order_timeline_blocked) to StatusDanger
                else -> stringResource(R.string.order_timeline_in_progress) to StatusNeutral
            }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEach { (text, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(8.dp).background(color, CircleShape))
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ReviewActions(loading: Boolean, onReject: () -> Unit, onApprove: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                enabled = !loading,
                border = BorderStroke(1.dp, StatusDanger),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusDangerContainer, contentColor = StatusDanger),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.order_reject), fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onApprove,
                enabled = !loading,
                border = BorderStroke(1.dp, StatusSuccess),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusSuccessContainer, contentColor = StatusSuccess),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = StatusSuccess)
                } else {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.order_approve), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RejectPaymentSheet(order: ChatOrder, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by rememberSaveable { mutableStateOf(RejectReason.INCORRECT_AMOUNT) }
    var detail by rememberSaveable { mutableStateOf("") }
    val reasonLabel = stringResource(reason.label)
    val fullReason = if (detail.isBlank()) reasonLabel else "$reasonLabel: ${detail.trim()}"
    val message = stringResource(R.string.order_reject_message, order.orderNumber, fullReason)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.order_reject_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.order_reject_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(Modifier.selectableGroup()) {
                RejectReason.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(selected = option == reason, onClick = { reason = option }, role = Role.RadioButton)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == reason,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = StatusDanger)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(option.label),
                            fontWeight = if (option == reason) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            TextInputField(detail, { detail = it }, label = stringResource(R.string.order_reject_detail))
            Button(
                onClick = { onConfirm(message) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.order_reject_confirm), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ApprovePaymentDialog(order: ChatOrder, clientName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                Modifier.size(64.dp).background(StatusSuccessContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(32.dp)) }
        },
        title = {
            Text(
                stringResource(R.string.order_approve_title, formatMoney(order.total)),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(R.string.order_approve_message, clientName),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            OutlinedButton(
                onClick = onConfirm,
                border = BorderStroke(1.dp, StatusSuccess),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusSuccessContainer, contentColor = StatusSuccess)
            ) { Text(stringResource(R.string.order_approve_confirm), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
