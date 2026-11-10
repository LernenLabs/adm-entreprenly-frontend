package online.entreprenly.entreprenlyapp.subscription

import java.lang.reflect.Proxy
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.api.SubscriptionApi
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.repositories.SubscriptionRepositoryImpl
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class SubscriptionRepositoryTest {
    private val free = PlanResource(1, "plan-free", "Plan Free", BigDecimal.ZERO, "PEN", true)
    private val control = PlanResource(2, "plan-control", "Plan Control", BigDecimal("49.90"), "PEN", true)
    private val plan = DashboardPlanResource("plan-free", "Plan Free", null, 0.0, 0.0, "free", null, null, false, null, null, emptyList())
    private val setup = DashboardBillingSetupResource(null, null, null, null, null, null, false, false, emptyList(), null)
    private val dashboard = SubscriptionDashboardResponse(7, "monthly", plan, plan.copy(id = "plan-control"), emptyList(), setup, emptyList())

    private fun api(handler: (String, Array<out Any?>) -> Any): SubscriptionApi = Proxy.newProxyInstance(
        SubscriptionApi::class.java.classLoader, arrayOf(SubscriptionApi::class.java)
    ) { _, method, args -> handler(method.name, args ?: emptyArray()) } as SubscriptionApi

    @Test fun fiscalSaveCannotActivateOrResumeAPlan() = runBlocking {
        var saved: SubscriptionDashboardResponse? = null
        val repository = SubscriptionRepositoryImpl(api { name, args ->
            when (name) {
                "dashboard" -> Response.success(dashboard)
                "saveDashboard" -> {
                    saved = args[1] as SubscriptionDashboardResponse
                    Response.success(dashboard.copy(billingSetup = saved!!.billingSetup))
                }
                else -> error("Unexpected API call: $name")
            }
        })
        assertTrue(repository.saveBilling(7, BillingDetails("Bodega", "12345678", "Lima", "owner@example.com")) is Result.Success)
        assertNull(saved!!.currentPlan)
        assertEquals("12345678", saved!!.billingSetup!!.fiscalData!!.documentNumber)
        assertEquals(dashboard.recommendedPlan, saved!!.recommendedPlan)
    }

    @Test fun upgradeClosesOnlyTheBackendFreeRowBeforeCharging() = runBlocking {
        val calls = mutableListOf<String>()
        val repository = SubscriptionRepositoryImpl(api { name, args ->
            calls.add(name)
            when (name) {
                "active", "cancel" -> Response.success(SubscriptionResource(10, 1, "ACTIVE", null, null))
                "plans" -> Response.success(listOf(free, control))
                "create" -> {
                    val request = args[0] as CreateSubscriptionResource
                    assertEquals(2L, request.planId)
                    assertEquals("fake-token", request.cardToken)
                    Response.success(SubscriptionResource(11, 2, "ACTIVE", "2026-11-05T00:00:00Z", 99))
                }
                else -> error(name)
            }
        })
        assertTrue(repository.create(7, 2, "fake-token") is Result.Success)
        assertEquals(listOf("active", "plans", "cancel", "create"), calls)
    }

    @Test fun anExistingPaidPlanIsNeverCancelledByCheckout() = runBlocking {
        val repository = SubscriptionRepositoryImpl(api { name, _ ->
            when (name) {
                "active" -> Response.success(SubscriptionResource(10, 2, "ACTIVE", "2026-11-05T00:00:00Z", 99))
                "plans" -> Response.success(listOf(free, control))
                else -> error("Must not cancel or charge: $name")
            }
        })
        assertTrue(repository.create(7, 2, "fake-token") is Result.Failure)
    }

    @Test fun scheduledCancellationPreservesThePlanAndPeriod() = runBlocking {
        val paid = dashboard.copy(currentPlan = plan.copy(id = "plan-control", status = "active", currentPeriodEndDate = "05/11/2026"))
        val repository = SubscriptionRepositoryImpl(api { name, args ->
            when (name) {
                "dashboard" -> Response.success(paid)
                "saveDashboard" -> {
                    val saved = args[1] as SubscriptionDashboardResponse
                    assertEquals("plan-control", saved.currentPlan!!.id)
                    assertEquals("scheduled-cancellation", saved.currentPlan.status)
                    assertEquals("05/11/2026", saved.currentPlan.currentPeriodEndDate)
                    Response.success(saved)
                }
                else -> error(name)
            }
        })
        val result = repository.setCancellation(7, true) as Result.Success
        assertTrue(result.value.cancellationScheduled)
    }
}
