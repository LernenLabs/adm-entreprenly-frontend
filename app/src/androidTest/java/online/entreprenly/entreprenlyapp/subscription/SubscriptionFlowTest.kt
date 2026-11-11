package online.entreprenly.entreprenlyapp.subscription

import android.content.res.Configuration
import androidx.lifecycle.SavedStateHandle
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import online.entreprenly.entreprenlyapp.R
import androidx.compose.ui.test.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.lang.reflect.Proxy
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl.AccessValidationResource
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl.SubscriptionAccessApi
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl.SubscriptionAccessFacadeImpl
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.AuthSession
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.EntreprenlyAppTheme
import online.entreprenly.entreprenlyapp.subscription.application.internal.commandservices.SubscriptionCommandServiceImpl
import online.entreprenly.entreprenlyapp.subscription.application.internal.queryservices.SubscriptionQueryServiceImpl
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.api.SubscriptionApi
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.repositories.SubscriptionRepositoryImpl
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.*
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.screens.SubscriptionScreen
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels.SubscriptionViewModel
import org.junit.Rule
import org.junit.Test
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class SubscriptionFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val localizedContext by lazy {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        base.createConfigurationContext(Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag("es-PE")) })
    }

    @Test fun paymentMethodsAreKeptAndPrimarySurvivesReload() {
        val server = SubscriptionTestServer()
        val viewModel = showSubscription(server)
        click("Add payment method")

        fill(0, "4242424242424241")
        click("Save payment method")
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_card_number_error)).performScrollTo().assertIsDisplayed()
        check(server.savedDashboardCount == 0)
        capture("payment-method-invalid")

        compose.onNodeWithContentDescription(localizedContext.getString(R.string.nav_back)).performClick()
        click("Add payment method")
        compose.onAllNodes(hasSetTextAction())[0].assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
        fill(0, "4242424242424242")
        fill(1, "1234")
        fill(2, "123")
        fill(3, "TITULAR PRUEBA")
        click("Save payment method")
        compose.onNodeWithText("Visa •••• 4242").assertIsDisplayed()
        check(server.savedDashboardCount == 1)
        check(server.billingSetup.fiscalData == null)
        capture("payment-method-saved")

        click("Add payment method")
        fill(0, "4111111111111111")
        compose.onNodeWithContentDescription(localizedContext.getString(R.string.nav_back)).performClick()
        compose.runOnUiThread { viewModel.load() }
        compose.waitForIdle()
        compose.onNodeWithText("Visa •••• 4242").assertIsDisplayed()
        check(server.savedDashboardCount == 1)
        capture("payment-method-after-back")

        click("Add payment method")
        fill(0, "5555555555554444")
        fill(1, "1234")
        fill(2, "123")
        fill(3, "TITULAR PRUEBA")
        click("Save payment method")
        check(server.savedDashboardCount == 2)
        check(server.billingSetup.paymentMethods.orEmpty().map { it.lastFour to it.isDefault } ==
            listOf("4242" to true, "4444" to false))
        capture("payment-methods-before-switch")

        click("Set as primary")
        check(server.savedDashboardCount == 3)
        check(server.billingSetup.paymentMethods.orEmpty().map { it.lastFour to it.isDefault } ==
            listOf("4242" to false, "4444" to true))
        compose.runOnUiThread { viewModel.load() }
        compose.waitForIdle()
        compose.onNodeWithText("Visa •••• 4242").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Mastercard •••• 4444").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(label("Primary")).assertIsDisplayed()
        check(viewModel.state.value.method?.lastFour == "4444")
        check(server.savedDashboardCount == 3)
        capture("payment-methods-after-reload")
    }

    @Test fun checkoutPreservesDetailsAcrossDeclinedAndPendingPayments() {
        val server = SubscriptionTestServer()
        val viewModel = showSubscription(server)
        capture("01-free")
        click("Upgrade plan")
        compose.onNodeWithText(label("Continue")).assertIsNotEnabled()
        capture("02-plans")
        compose.runOnUiThread { viewModel.start() }
        compose.onNodeWithText(label("Select a plan to continue")).assertIsDisplayed()
        click("Choose plan")
        compose.onNodeWithText(label("Plan selected")).assertIsDisplayed()
        capture("03-selected")
        click("Continue")
        click("Continue")
        compose.onAllNodesWithText(label("Check this field")).assertCountEquals(3)
        capture("04-billing-error")
        fill(0, "Luis Quispe — Bodega El Huerto")
        fill(1, "10456789123")
        fill(2, "Jr. Huánuco 412, La Victoria")
        fill(3, "lucho@example.com")
        capture("05-billing")
        click("Continue")
        fill(0, "4111111111111111")
        fill(1, "0829")
        fill(2, "123")
        fill(3, "LUIS QUISPE")
        capture("06-payment")
        click("Continue to payment")
        capture("07-summary")
        click("Pay and activate")
        compose.onNodeWithText(label("Payment declined")).assertIsDisplayed()
        compose.onNodeWithText(label("Visa •••• 1111")).assertIsDisplayed()
        capture("08-declined")

        compose.runOnUiThread { viewModel.showPanel() }
        compose.waitForIdle()
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_free_description)).assertIsDisplayed()
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_whatsapp)).assertDoesNotExist()
        check(viewModel.state.value.dashboard?.planCode == "plan-free")
        check(!viewModel.state.value.hasControl)
        checkChatbotAccess(server, expected = false)
        capture("activation-declined-panel-reopened")

        click("Upgrade plan")
        click("Choose plan")
        click("Continue")
        click("Continue")
        fill(0, "4111111111111111")
        fill(1, "0829")
        fill(2, "123")
        fill(3, "LUIS QUISPE")
        click("Continue to payment")
        click("Pay and activate")
        compose.onNodeWithText(label("Payment declined")).assertIsDisplayed()
        click("Pay and activate")
        compose.onNodeWithText(label("Check payment status")).assertIsDisplayed()
        compose.onAllNodesWithText(label("Payment under verification")).filter(hasClickAction()).onFirst().assertIsNotEnabled()
        capture("09-pending")
        server.approve()
        click("Check payment status")
        compose.onNodeWithText(label("Plan Control activated!")).assertIsDisplayed()
        capture("10-activated")
        click("Go to Subscription")
        compose.onNodeWithText(label("Active")).assertIsDisplayed()
        click("Payment history")
        compose.onNodeWithText(label("Payment approved")).assertIsDisplayed()
        compose.runOnUiThread { viewModel.showPanel() }
        compose.waitForIdle()
        compose.onNodeWithText("Plan Control").assertIsDisplayed()
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_whatsapp)).assertIsDisplayed()
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_iot_device)).assertIsDisplayed()
        check(viewModel.state.value.hasControl)
        checkChatbotAccess(server, expected = true)
        capture("activation-approved-panel-reopened")
        val writesBeforeCancellation = server.savedDashboardCount
        val expiration = requireNotNull(viewModel.state.value.dashboard?.endDate)
        val activeSubscription = requireNotNull(viewModel.state.value.subscription)
        click("Cancel subscription")
        capture("12-cancel-dialog")
        click("Back")
        compose.onNodeWithText(label("Cancel plan")).assertDoesNotExist()
        compose.onNodeWithText(label("Active")).assertIsDisplayed()
        check(server.savedDashboardCount == writesBeforeCancellation)

        click("Cancel subscription")
        androidx.test.espresso.Espresso.pressBack()
        compose.waitForIdle()
        compose.onNodeWithText(label("Cancel plan")).assertDoesNotExist()
        compose.onNodeWithText(label("Active")).assertIsDisplayed()
        check(server.savedDashboardCount == writesBeforeCancellation)

        click("Cancel subscription")
        click("Cancel plan")
        check(server.savedDashboardCount == writesBeforeCancellation + 1)
        click("Payment history")
        compose.runOnUiThread { viewModel.showPanel() }
        compose.waitForIdle()
        compose.onAllNodesWithText(localizedContext.getString(R.string.subscription_scheduled)).onFirst().assertIsDisplayed()
        compose.onNodeWithText(localizedContext.getString(R.string.subscription_access_until, expiration)).assertIsDisplayed()
        compose.onNodeWithText(label("Reactivate subscription")).assertIsDisplayed()
        check(viewModel.state.value.hasControl)
        check(viewModel.state.value.subscription == activeSubscription)
        check(viewModel.state.value.dashboard?.endDate == expiration)
        capture("cancellation-scheduled-reopened")
        click("Reactivate subscription")
        compose.onNodeWithText(label("Active")).assertIsDisplayed()
        click("Renew subscription")
        click("Continue")
        click("Pay and activate")
        compose.onNodeWithText(label("Subscription renewed!")).assertIsDisplayed()
        click("Go to Subscription")
        click("Payment history")
        compose.onAllNodesWithText(label("Payment approved")).assertCountEquals(2)
        capture("14-history")
    }

    private val sessions = object : SessionQueryService {
        override fun handle(query: GetCurrentSessionQuery) = flowOf(AuthSession(7, "test@example.com", "test-token"))
    }

    private fun checkChatbotAccess(server: SubscriptionTestServer, expected: Boolean) = runBlocking {
        val facade = SubscriptionAccessFacadeImpl(server, sessions)
        check(facade.hasChatbotAccess() == Result.Success(expected))
    }

    private fun showSubscription(server: SubscriptionTestServer): SubscriptionViewModel {
        val repository = SubscriptionRepositoryImpl(server.api)
        lateinit var viewModel: SubscriptionViewModel
        compose.runOnUiThread {
            compose.activity.enableEdgeToEdge()
            compose.activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            viewModel = SubscriptionViewModel(sessions, SubscriptionQueryServiceImpl(repository), SubscriptionCommandServiceImpl(repository), SavedStateHandle())
        }
        compose.setContent {
            CompositionLocalProvider(LocalContext provides localizedContext, LocalConfiguration provides localizedContext.resources.configuration) {
                EntreprenlyAppTheme(darkTheme = false) { SubscriptionScreen(viewModel, {}, {}) }
            }
        }
        compose.waitForIdle()
        return viewModel
    }

    private fun click(text: String) {
        val node = compose.onNodeWithText(label(text))
        runCatching { node.assertIsDisplayed() }.onFailure { node.performScrollTo() }
        node.performClick()
        compose.waitForIdle()
    }
    private fun fill(index: Int, value: String) {
        compose.onAllNodes(hasSetTextAction())[index].performScrollTo().performTextReplacement(value)
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        compose.runOnUiThread {
            val keyboard = compose.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(350)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "subscription-screens").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val output = "/sdcard/Download/entreprenly-subscription"
        listOf("mkdir -p $output", "cp ${File(directory, "$name.png").absolutePath} $output/").forEach { command ->
            ParcelFileDescriptor.AutoCloseInputStream(
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            ).use { it.readBytes() }
        }
    }
    private fun label(english: String): String {
        val resource = when (english) {
            "Primary" -> R.string.subscription_primary_method
            "Set as primary" -> R.string.subscription_set_primary_method
            "Add payment method" -> R.string.subscription_add_payment_method
            "Change payment method" -> R.string.subscription_change_card
            "Save payment method" -> R.string.subscription_save_payment_method
            "Active" -> R.string.subscription_active
            "Back" -> R.string.subscription_back
            "Cancel plan" -> R.string.subscription_cancel_confirm
            "Cancel subscription" -> R.string.subscription_cancel
            "Check payment status" -> R.string.subscription_check_payment
            "Check this field" -> R.string.subscription_invalid_field
            "Choose plan" -> R.string.subscription_choose
            "Continue" -> R.string.subscription_continue
            "Continue to payment" -> R.string.subscription_continue_payment
            "Go to Subscription" -> R.string.subscription_go_panel
            "Pay and activate" -> R.string.subscription_pay
            "Payment approved" -> R.string.subscription_payment_approved
            "Payment declined" -> R.string.subscription_declined
            "Payment history" -> R.string.subscription_history
            "Payment under verification" -> R.string.subscription_verifying
            "Plan Control activated!" -> R.string.subscription_activated
            "Plan selected" -> R.string.subscription_selected
            "Reactivate subscription" -> R.string.subscription_reactivate
            "Renew subscription" -> R.string.subscription_renew
            "Select a plan to continue" -> R.string.subscription_select_required
            "Subscription renewed!" -> R.string.subscription_renewed
            "Upgrade plan" -> R.string.subscription_upgrade
            else -> return english
        }
        return localizedContext.getString(resource)
    }
}

private class SubscriptionTestServer : SubscriptionAccessApi {
    private val end = Instant.now().plusSeconds(30 * 86400L).toString()
    private val dashboardDate = DateTimeFormatter.ofPattern("dd/MM/uuuu").withZone(ZoneOffset.UTC)
    private val free = DashboardPlanResource("plan-free", "Plan Free", null, 0.0, 0.0, "free", null, null, false, null, null, emptyList())
    private val control = free.copy(id = "plan-control", name = "Plan Control", monthlyPrice = 49.9, currentPeriodEndDate = dashboardDate.format(Instant.parse(end)), status = "active")
    private var dashboard = SubscriptionDashboardResponse(7, "monthly", free, control, emptyList(), DashboardBillingSetupResource(null, null, null, null, null, null, false, false, emptyList(), null), emptyList())
    var savedDashboardCount = 0
        private set
    val billingSetup get() = requireNotNull(dashboard.billingSetup)
    private var subscription: SubscriptionResource? = null
    private var payments = emptyList<PaymentResource>()

    override suspend fun validateAccess(userId: Long, feature: String): Response<AccessValidationResource> {
        check(userId == 7L && feature == "chatbot")
        val hasAccess = subscription?.let { it.planId == 2L && it.status == "ACTIVE" } == true
        return Response.success(AccessValidationResource(userId, feature, hasAccess))
    }

    fun approve() {
        subscription = subscription!!.copy(status = "ACTIVE", currentPeriodEnd = end)
        payments = payments.map { if (it.id == subscription!!.latestPaymentId) it.copy(status = "APPROVED") else it }
        dashboard = dashboard.copy(currentPlan = control)
    }
    val api = Proxy.newProxyInstance(SubscriptionApi::class.java.classLoader, arrayOf(SubscriptionApi::class.java)) { _, method, args ->
        when (method.name) {
            "plans" -> Response.success(listOf(PlanResource(1, "plan-free", "Plan Free", BigDecimal.ZERO, "PEN", true), PlanResource(2, "plan-control", "Plan Control", BigDecimal("49.90"), "PEN", true)))
            "dashboard" -> Response.success(dashboard)
            "saveDashboard" -> {
                savedDashboardCount++
                val saved = args!![1] as SubscriptionDashboardResponse
                dashboard = dashboard.copy(billingSetup = saved.billingSetup, currentPlan = saved.currentPlan ?: dashboard.currentPlan)
                Response.success(dashboard)
            }
            "active" -> if (subscription?.status == "ACTIVE") Response.success(subscription) else Response.error<SubscriptionResource>(404, "{}".toResponseBody())
            "create" -> {
                subscription = SubscriptionResource(10, 2, "PENDING_PAYMENT", null, 1)
                payments = listOf(PaymentResource(1, "DECLINED", BigDecimal("49.90"), "PEN", "Insufficient funds", Instant.now().toString()))
                Response.success(subscription)
            }
            "pay" -> {
                subscription = subscription!!.copy(latestPaymentId = 2)
                val payment = PaymentResource(2, "PENDING", BigDecimal("49.90"), "PEN", null, Instant.now().toString())
                payments = payments + payment
                Response.success(payment)
            }
            "get" -> Response.success(subscription)
            "payments" -> Response.success(payments)
            "renew" -> {
                subscription = subscription!!.copy(currentPeriodEnd = Instant.parse(end).plusSeconds(30 * 86400L).toString(), latestPaymentId = 3)
                payments = payments + PaymentResource(3, "APPROVED", BigDecimal("49.90"), "PEN", null, Instant.now().toString())
                dashboard = dashboard.copy(currentPlan = control.copy(currentPeriodEndDate = dashboardDate.format(Instant.parse(requireNotNull(subscription?.currentPeriodEnd)))))
                Response.success(subscription)
            }
            else -> error("Unexpected API operation: ${method.name}")
        }
    } as SubscriptionApi
}
