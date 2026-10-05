package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText
import online.entreprenly.entreprenlyapp.subscription.application.commandservices.SubscriptionCommandService
import online.entreprenly.entreprenlyapp.subscription.application.queryservices.SubscriptionQueryService
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPayment
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPlan
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CancelSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CreateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.PaySubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.ReactivateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.RenewSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.SaveBillingCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetActiveSubscriptionQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionDashboardQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPaymentsQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPlansQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentStatus
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.SubscriptionStatus

enum class SubscriptionPage { PANEL, PLANS, BILLING, PAYMENT, SUMMARY, ACTIVATED, HISTORY }
data class SubscriptionUiState(
    val loading: Boolean = true, val busy: Boolean = false,
    val cancelling: Boolean = false,
    val page: SubscriptionPage = SubscriptionPage.PANEL,
    val dashboard: SubscriptionDashboard? = null,
    val plans: List<SubscriptionPlan> = emptyList(),
    val subscription: Subscription? = null,
    val selectedPlanId: Long? = null,
    val billing: BillingDetails = BillingDetails(),
    val invalidFields: Set<String> = emptySet(),
    val method: PaymentMethod? = null,
    val payments: List<SubscriptionPayment> = emptyList(),
    val error: UiText? = null,
    val paymentStatus: PaymentStatus? = null,
    val verifying: Boolean = false,
    val renewing: Boolean = false
) {
    val controlPlan get() = plans.firstOrNull { it.code == "plan-control" && it.active }
    val selectedPlan get() = plans.firstOrNull { it.id == selectedPlanId }
    val hasControl get() = dashboard?.planCode == "plan-control" && subscription?.isActiveAt(Instant.now()) == true
}

class SubscriptionViewModel(
    sessions: SessionQueryService,
    private val queries: SubscriptionQueryService,
    private val commands: SubscriptionCommandService,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionUiState())
    val state = _state.asStateFlow()
    private var userId: Long? = null
    private var token: String? = null
    private var operation: Job? = null

    init {
        viewModelScope.launch {
            sessions.handle(GetCurrentSessionQuery).collectLatest { session ->
                if (session?.userId != userId) {
                    operation?.cancel()
                    userId = session?.userId
                    token = null
                    _state.value = SubscriptionUiState()
                    if (session != null) loadFor(session.userId)
                }
            }
        }
    }

    fun load() {
        if (state.value.busy) return
        val id = userId ?: return
        viewModelScope.launch { loadFor(id) }
    }

    private suspend fun loadFor(id: Long) {
        _state.update { it.copy(loading = true, error = null) }
        val dashboard = queries.handle(GetSubscriptionDashboardQuery(id))
        val plans = queries.handle(GetSubscriptionPlansQuery)
        val active = queries.handle(GetActiveSubscriptionQuery(id))
        if (userId != id) return
        val failure = listOf(dashboard, plans, active).filterIsInstance<Result.Failure>().firstOrNull()
        if (failure != null) {
            _state.update { it.copy(loading = false, error = failure.error.asUiText()) }
            return
        }
        val d = (dashboard as Result.Success).value
        _state.update { it.copy(loading = false, dashboard = d, plans = (plans as Result.Success).value,
            subscription = (active as Result.Success).value, billing = d.billing,
            method = d.methods.firstOrNull { method -> method.isDefault } ?: d.methods.firstOrNull()) }
        if (savedState.get<Long>("paymentUserId") == id && savedState.get<Boolean>("paymentUnresolved") == true) {
            _state.update { it.copy(page = SubscriptionPage.SUMMARY, verifying = true,
                selectedPlanId = savedState["paymentPlanId"], renewing = savedState.get<Boolean>("paymentRenewal") == true) }
            val pendingId = savedState.get<Long>("paymentSubscriptionId")
            val restored = if (pendingId == null) (active as Result.Success).value else
                (queries.handle(GetSubscriptionQuery(pendingId)) as? Result.Success)?.value
            if (restored != null && restored.planId == state.value.selectedPlanId) resolvePayment(restored, id)
        }
    }

    fun showPlans() {
        if (state.value.busy || state.value.verifying) return
        token = null
        _state.update { it.copy(page = SubscriptionPage.PLANS, selectedPlanId = null, error = null, paymentStatus = null, renewing = false) }
    }
    fun selectPlan(id: Long) {
        if (state.value.controlPlan?.id != id || state.value.busy) return
        _state.update { it.copy(selectedPlanId = id, error = null) }
    }
    fun start() {
        if (state.value.selectedPlan == null) {
            _state.update { it.copy(error = UiText.Res(R.string.subscription_select_required)) }
        } else _state.update { it.copy(page = SubscriptionPage.BILLING, error = null) }
    }
    fun updateBilling(value: BillingDetails) {
        if (!state.value.busy) _state.update { it.copy(billing = value, invalidFields = emptySet(), error = null) }
    }
    fun saveBilling() {
        val invalid = state.value.billing.invalidFields()
        if (invalid.isNotEmpty()) {
            _state.update { it.copy(invalidFields = invalid) }
            return
        }
        perform { id ->
            when (val result = commands.handle(SaveBillingCommand(id, state.value.billing))) {
                is Result.Success -> _state.update { it.copy(dashboard = result.value, page = SubscriptionPage.PAYMENT) }
                is Result.Failure -> fail(result.error)
            }
        }
    }
    fun saveMethod(method: PaymentMethod) {
        perform { id ->
            when (val result = commands.handle(SaveBillingCommand(id, state.value.billing, method))) {
                is Result.Success -> {
                    // The backend uses a fake gateway. PAN and CVV never enter its contract.
                    token = "fake-card-" + UUID.randomUUID()
                    _state.update { it.copy(method = method, dashboard = result.value, page = SubscriptionPage.SUMMARY, paymentStatus = null) }
                }
                is Result.Failure -> fail(result.error)
            }
        }
    }
    fun beginRenewal() {
        val s = state.value
        if (!s.hasControl || s.dashboard?.cancellationScheduled == true) {
            _state.update { it.copy(error = UiText.Res(R.string.subscription_not_renewable)) }
            return
        }
        token = "fake-card-" + UUID.randomUUID()
        _state.update { it.copy(renewing = true, selectedPlanId = it.controlPlan?.id, page = if (it.method == null) SubscriptionPage.PAYMENT else SubscriptionPage.SUMMARY, paymentStatus = null, error = null) }
    }
    fun pay() {
        val s = state.value
        if (s.busy || s.verifying || s.page != SubscriptionPage.SUMMARY) return
        val plan = s.selectedPlan ?: return
        val paymentToken = token ?: return
        perform { id ->
            savedState["paymentUserId"] = id
            savedState["paymentPlanId"] = plan.id
            savedState["paymentRenewal"] = s.renewing
            savedState["previousPaymentId"] = s.subscription?.takeIf { it.planId == plan.id }?.latestPaymentId
            savedState["paymentUnresolved"] = true
            savedState["paymentSubscriptionId"] = s.subscription?.takeIf { it.planId == plan.id }?.id
            val result: Result<Subscription> = when {
                s.renewing -> commands.handle(RenewSubscriptionCommand(requireNotNull(s.subscription).id, paymentToken))
                s.subscription?.status == SubscriptionStatus.PENDING_PAYMENT -> when (val payment = commands.handle(PaySubscriptionCommand(s.subscription.id, paymentToken))) {
                    is Result.Failure -> payment
                    is Result.Success -> queries.handle(GetSubscriptionQuery(s.subscription.id))
                }
                else -> commands.handle(CreateSubscriptionCommand(id, plan.id, paymentToken))
            }
            when (result) {
                is Result.Failure -> {
                    fail(result.error)
                    // An interrupted response can still represent a completed charge. Do not send it again.
                    if (result.error is ApplicationError.Network || result.error is ApplicationError.Unexpected) {
                        _state.update { it.copy(verifying = true) }
                    } else savedState["paymentUnresolved"] = false
                }
                is Result.Success -> resolvePayment(result.value, id)
            }
        }
    }
    private suspend fun resolvePayment(subscription: Subscription, id: Long) {
        savedState["paymentSubscriptionId"] = subscription.id
        _state.update { it.copy(subscription = subscription) }
        when (val result = queries.handle(GetSubscriptionPaymentsQuery(subscription.id))) {
            is Result.Failure -> { fail(result.error); _state.update { it.copy(verifying = true) } }
            is Result.Success -> {
                val payment = result.value.firstOrNull { it.id == subscription.latestPaymentId }
                    ?.takeUnless { savedState.get<Boolean>("paymentUnresolved") == true && it.id == savedState.get<Long>("previousPaymentId") }
                val approved = payment?.status == PaymentStatus.APPROVED && subscription.isActiveAt(Instant.now())
                _state.update { it.copy(payments = result.value, paymentStatus = payment?.status, verifying = payment == null || payment.status == PaymentStatus.PENDING) }
                savedState["paymentUnresolved"] = payment == null || payment.status == PaymentStatus.PENDING
                if (token == null && payment?.status in listOf(PaymentStatus.DECLINED, PaymentStatus.FAILED)) {
                    _state.update { it.copy(page = SubscriptionPage.PAYMENT) }
                }
                if (approved) {
                    token = null
                    val dashboard = queries.handle(GetSubscriptionDashboardQuery(id))
                    _state.update { it.copy(page = SubscriptionPage.ACTIVATED, verifying = false, dashboard = (dashboard as? Result.Success)?.value ?: it.dashboard) }
                }
            }
        }
    }
    fun verifyPayment() {
        perform { id ->
            val subscription = state.value.subscription
            val result = if (subscription != null) queries.handle(GetSubscriptionQuery(subscription.id)) else queries.handle(GetActiveSubscriptionQuery(id))
            when (result) {
                is Result.Failure -> fail(result.error)
                is Result.Success -> if (result.value != null) resolvePayment(result.value!!, id)
                    else _state.update { it.copy(error = UiText.Res(R.string.subscription_unconfirmed)) }
            }
        }
    }
    fun setCancellation(scheduled: Boolean) {
        if (!state.value.hasControl) return
        perform(cancelling = scheduled) { id ->
            val result = if (scheduled) commands.handle(CancelSubscriptionCommand(id)) else commands.handle(ReactivateSubscriptionCommand(id))
            when (result) {
                is Result.Success -> _state.update { it.copy(dashboard = result.value) }
                is Result.Failure -> fail(result.error)
            }
        }
    }
    fun showPanel() { _state.update { it.copy(page = SubscriptionPage.PANEL, error = null) }; load() }
    fun showHistory() {
        val subscription = state.value.subscription ?: return
        perform {
            when (val result = queries.handle(GetSubscriptionPaymentsQuery(subscription.id))) {
                is Result.Success -> _state.update { it.copy(page = SubscriptionPage.HISTORY, payments = result.value) }
                is Result.Failure -> fail(result.error)
            }
        }
    }
    fun back(): Boolean {
        val s = state.value
        if (s.busy || s.verifying) return true
        val page = when (s.page) {
            SubscriptionPage.PANEL -> return false
            SubscriptionPage.PLANS, SubscriptionPage.ACTIVATED, SubscriptionPage.HISTORY -> SubscriptionPage.PANEL
            SubscriptionPage.BILLING -> SubscriptionPage.PLANS
            SubscriptionPage.PAYMENT -> if (s.renewing) SubscriptionPage.PANEL else SubscriptionPage.BILLING
            SubscriptionPage.SUMMARY -> SubscriptionPage.PAYMENT
        }
        _state.update { it.copy(page = page, error = null, invalidFields = emptySet()) }
        return true
    }
    private fun perform(cancelling: Boolean = false, action: suspend (Long) -> Unit) {
        val id = userId ?: return
        if (state.value.busy) return
        _state.update { it.copy(busy = true, cancelling = cancelling, error = null) }
        operation = viewModelScope.launch {
            try { action(id) } finally { if (userId == id) _state.update { it.copy(busy = false, cancelling = false) } }
        }
    }
    private fun fail(error: ApplicationError) { _state.update { it.copy(error = error.asUiText()) } }
    private fun ApplicationError.asUiText(): UiText = when (this) {
        is ApplicationError.Network -> UiText.Res(R.string.subscription_network_error)
        is ApplicationError.Unauthorized -> UiText.Res(R.string.subscription_session_error)
        else -> UiText.Raw(message)
    }
}
