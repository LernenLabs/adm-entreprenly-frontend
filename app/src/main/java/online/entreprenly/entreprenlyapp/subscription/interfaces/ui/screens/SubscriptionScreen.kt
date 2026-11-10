package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.BrandBrown
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.CardDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentStatus
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.SubscriptionProgressNotice
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.BillingDetailsForm
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.CardDetailsForm
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.SubscriptionFeaturesCard
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.SubscriptionBadge
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.SubscriptionPlanCard
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.SubscriptionSummaryRow
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components.subscriptionMoney
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels.SubscriptionPage
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels.SubscriptionUiState
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels.SubscriptionViewModel

@Composable
fun SubscriptionScreen(viewModel: SubscriptionViewModel, onBack: () -> Unit, onWhatsApp: () -> Unit) {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(onPrimary = androidx.compose.ui.graphics.Color.White)) {
        SubscriptionContent(viewModel, onBack, onWhatsApp)
    }
}

@Composable
private fun SubscriptionContent(viewModel: SubscriptionViewModel, onBack: () -> Unit, onWhatsApp: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val back = { if (!viewModel.back()) onBack() }
    BackHandler(onBack = back)
    var card by remember { mutableStateOf(CardDetails()) }
    var invalidCardFields by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(state.page) {
        if (state.page != SubscriptionPage.PAYMENT) {
            card = CardDetails()
            invalidCardFields = emptySet()
        }
    }
    var confirmCancellation by remember { mutableStateOf(false) }
    var confirmRenewal by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
        if (state.page != SubscriptionPage.ACTIVATED) {
            AppTopBar(stringResource(when (state.page) {
                SubscriptionPage.PLANS -> R.string.subscription_plans
                SubscriptionPage.BILLING, SubscriptionPage.PAYMENT, SubscriptionPage.SUMMARY -> R.string.subscription_checkout
                SubscriptionPage.HISTORY -> R.string.subscription_history
                else -> R.string.more_subscription
            }), onBack = back, subtitle = when (state.page) {
                SubscriptionPage.PLANS -> stringResource(R.string.subscription_plans_description)
                SubscriptionPage.BILLING -> stringResource(R.string.subscription_step_billing)
                SubscriptionPage.PAYMENT -> stringResource(R.string.subscription_step_payment)
                SubscriptionPage.SUMMARY -> stringResource(R.string.subscription_step_summary)
                else -> null
            })
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        if (state.dashboard == null) {
            Column(Modifier.padding(16.dp)) {
                state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                PrimaryPillButton(stringResource(R.string.subscription_retry), viewModel::load)
            }
            return@Column
        }
        val step = when (state.page) { SubscriptionPage.BILLING -> 1; SubscriptionPage.PAYMENT -> 2; SubscriptionPage.SUMMARY -> 3; else -> 0 }
        if (step > 0) {
            Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { index -> Box(Modifier.weight(1f).height(4.dp).background(if (index < step) MaterialTheme.colorScheme.primary else MaterialTheme.extraColors.border)) }
            }
        }
        if (state.page == SubscriptionPage.ACTIVATED) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = BiasAlignment(0f, -0.25f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.extraColors.highlight) {
                        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_subscription_crown), null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(stringResource(if (state.renewing) R.string.subscription_renewed else R.string.subscription_activated),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text(stringResource(R.string.subscription_activated_description),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.extraColors.muted)
                }
            }
        } else Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            when (state.page) {
                SubscriptionPage.PANEL -> {
                    val plan = state.plans.firstOrNull { it.code == state.dashboard?.planCode }
                    if (state.cancelling) SubscriptionProgressNotice(
                        stringResource(R.string.subscription_cancelling),
                        stringResource(R.string.subscription_cancelling_description))
                    if (state.dashboard?.cancellationScheduled == true) MessageBanner(stringResource(R.string.subscription_scheduled), stringResource(R.string.subscription_access_until, state.dashboard?.endDate.orEmpty()), BannerKind.WARNING)
                    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = BrandBrown, contentColor = androidx.compose.ui.graphics.Color.White) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(plan?.name.orEmpty(), fontWeight = FontWeight.Bold)
                                val statusLabel = when (state.dashboard?.status) {
                                    "cancelled" -> R.string.subscription_cancelled
                                    "expired" -> R.string.subscription_expired
                                    "suspended" -> R.string.subscription_suspended
                                    "scheduled-cancellation" -> R.string.subscription_scheduled
                                    else -> if (state.hasControl) R.string.subscription_active else R.string.subscription_free
                                }
                                SubscriptionBadge(stringResource(statusLabel), warning = state.dashboard?.cancellationScheduled == true, neutral = !state.hasControl)
                            }
                            plan?.let { Text(subscriptionMoney(it.amount, it.currency) + stringResource(R.string.subscription_per_month), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                            state.dashboard?.endDate?.let { Text(stringResource(R.string.subscription_expires, it), style = MaterialTheme.typography.bodySmall) }
                            state.method?.let { Text(stringResource(R.string.subscription_masked_card, it.cardBrand, it.lastFour), style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                    if (state.hasControl) {
                        SubscriptionFeaturesCard(onWhatsApp, viewModel::showHistory)
                    } else Text(stringResource(R.string.subscription_free_description))
                }
                SubscriptionPage.PLANS -> {
                    state.plans.firstOrNull { it.code == "plan-free" }?.let { SubscriptionPlanCard(it, false, null) }
                    state.controlPlan?.let { SubscriptionPlanCard(it, state.selectedPlanId == it.id) { viewModel.selectPlan(it.id) } }
                    if (state.controlPlan == null) Text(stringResource(R.string.subscription_unavailable))
                }
                SubscriptionPage.BILLING -> BillingDetailsForm(state.billing, state.invalidFields, !state.busy, viewModel::updateBilling)
                SubscriptionPage.PAYMENT -> CardDetailsForm(card, invalidCardFields, !state.busy) {
                    card = it
                    invalidCardFields = emptySet()
                }
                SubscriptionPage.SUMMARY -> {
                    if (state.busy || state.verifying) SubscriptionProgressNotice(stringResource(R.string.subscription_verifying), stringResource(R.string.subscription_verifying_description))
                    if (!state.busy && !state.verifying && state.paymentStatus in listOf(PaymentStatus.DECLINED, PaymentStatus.FAILED)) {
                        Text(stringResource(R.string.subscription_declined), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text(state.payments.firstOrNull { it.id == state.subscription?.latestPaymentId }?.providerMessage ?: stringResource(R.string.subscription_declined_description), color = MaterialTheme.colorScheme.error)
                        SecondaryPillButton(stringResource(R.string.subscription_change_card), { viewModel.back() })
                    }
                    AppCard {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            SubscriptionSummaryRow(stringResource(R.string.subscription_plan), state.selectedPlan?.name.orEmpty())
                            HorizontalDivider()
                            SubscriptionSummaryRow(stringResource(R.string.subscription_card), state.method?.let { stringResource(R.string.subscription_masked_card, it.cardBrand, it.lastFour) }.orEmpty())
                            HorizontalDivider()
                            val end = if (state.renewing) state.subscription?.currentPeriodEnd?.atZone(ZoneId.systemDefault())?.plusDays(30)?.toLocalDate() else LocalDate.now().plusDays(30)
                            SubscriptionSummaryRow(stringResource(R.string.subscription_next_renewal), end?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).orEmpty())
                            HorizontalDivider()
                            state.selectedPlan?.let { SubscriptionSummaryRow(stringResource(R.string.subscription_total), subscriptionMoney(it.amount, it.currency), true) }
                        }
                    }
                    Text(stringResource(R.string.subscription_terms), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                }
                SubscriptionPage.ACTIVATED -> Unit
                SubscriptionPage.HISTORY -> {
                    if (state.payments.isEmpty()) Text(stringResource(R.string.subscription_no_payments))
                    state.payments.sortedByDescending { it.requestedAt }.forEach { payment ->
                        AppCard {
                            Column(Modifier.padding(16.dp)) {
                                Text(subscriptionMoney(payment.amount, payment.currency), fontWeight = FontWeight.Bold)
                                Text(payment.requestedAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                Text(stringResource(when (payment.status) { PaymentStatus.APPROVED -> R.string.subscription_payment_approved; PaymentStatus.PENDING -> R.string.subscription_verifying; else -> R.string.subscription_declined }))
                            }
                        }
                    }
                }
            }
        }
        if (state.page != SubscriptionPage.HISTORY) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.busy && state.page != SubscriptionPage.SUMMARY && !state.cancelling) LinearProgressIndicator(Modifier.fillMaxWidth())
                when (state.page) {
                    SubscriptionPage.PANEL -> if (state.hasControl) {
                        if (state.dashboard?.cancellationScheduled == true) {
                            PrimaryPillButton(stringResource(R.string.subscription_reactivate), { viewModel.setCancellation(false) }, enabled = !state.busy)
                        } else {
                            PrimaryPillButton(stringResource(R.string.subscription_renew), { confirmRenewal = true }, enabled = !state.busy)
                            OutlinedButton(onClick = { confirmCancellation = true }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text(stringResource(R.string.subscription_cancel))
                            }
                        }
                    } else PrimaryPillButton(stringResource(R.string.subscription_upgrade), viewModel::showPlans, enabled = !state.busy)
                    SubscriptionPage.PLANS -> PrimaryPillButton(stringResource(R.string.subscription_continue), viewModel::start, enabled = !state.busy && state.selectedPlan != null)
                    SubscriptionPage.BILLING -> PrimaryPillButton(stringResource(R.string.subscription_continue), viewModel::saveBilling, enabled = !state.busy)
                    SubscriptionPage.PAYMENT -> PrimaryPillButton(stringResource(R.string.subscription_continue_payment), {
                        invalidCardFields = card.invalidFields()
                        if (invalidCardFields.isEmpty()) viewModel.saveMethod(card.maskedMethod("fake-method-" + UUID.randomUUID()))
                    }, enabled = !state.busy)
                    SubscriptionPage.SUMMARY -> {
                        PrimaryPillButton(stringResource(if (state.busy || state.verifying) R.string.subscription_verifying else R.string.subscription_pay), viewModel::pay, enabled = !state.busy && !state.verifying)
                        if (state.verifying && !state.busy) SecondaryPillButton(stringResource(R.string.subscription_check_payment), viewModel::verifyPayment)
                    }
                    SubscriptionPage.ACTIVATED -> {
                        PrimaryPillButton(stringResource(R.string.subscription_link_whatsapp), onWhatsApp)
                        SecondaryPillButton(stringResource(R.string.subscription_go_panel), viewModel::showPanel)
                    }
                    else -> Unit
                }
            }
        }
    }
    // Resolve dialog text before Android creates its separate window context.
    val cancelTitle = stringResource(R.string.subscription_cancel_title)
    val cancelDescription = stringResource(R.string.subscription_cancel_description, state.dashboard?.endDate.orEmpty())
    val cancelAction = stringResource(R.string.subscription_cancel_confirm)
    val backAction = stringResource(R.string.subscription_back)
    val renewTitle = stringResource(R.string.subscription_renew)
    val renewDescription = stringResource(R.string.subscription_renew_confirm)
    val continueAction = stringResource(R.string.subscription_continue)
    if (confirmCancellation) AlertDialog(onDismissRequest = { confirmCancellation = false },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        title = { Text(cancelTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = { Text(cancelDescription) },
        confirmButton = { Button(onClick = { confirmCancellation = false; viewModel.setCancellation(true) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(cancelAction) } },
        dismissButton = { OutlinedButton(onClick = { confirmCancellation = false }) { Text(backAction) } })
    if (confirmRenewal) AlertDialog(onDismissRequest = { confirmRenewal = false },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        title = { Text(renewTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(renewDescription, color = MaterialTheme.extraColors.muted) },
        confirmButton = {
            Button(onClick = { confirmRenewal = false; viewModel.beginRenewal() },
                modifier = Modifier.height(44.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)) {
                Text(continueAction, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { confirmRenewal = false }, modifier = Modifier.height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.extraColors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                Text(backAction, fontWeight = FontWeight.SemiBold)
            }
        })
}
