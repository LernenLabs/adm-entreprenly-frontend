package online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.CreateSubscriptionResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PaymentRequest
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PaymentResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PlanResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.SubscriptionDashboardResponse
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.SubscriptionResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SubscriptionApi {
    @GET("api/v1/subscription-plans") suspend fun plans(): Response<List<PlanResource>>
    @GET("api/v1/subscription-dashboard/{userId}") suspend fun dashboard(@Path("userId") userId: Long): Response<SubscriptionDashboardResponse>
    @PUT("api/v1/subscription-dashboard/{userId}") suspend fun saveDashboard(@Path("userId") userId: Long, @Body body: SubscriptionDashboardResponse): Response<SubscriptionDashboardResponse>
    @GET("api/v1/subscriptions/active") suspend fun active(@Query("userId") userId: Long): Response<SubscriptionResource>
    @GET("api/v1/subscriptions/{id}") suspend fun get(@Path("id") id: Long): Response<SubscriptionResource>
    @POST("api/v1/subscriptions") suspend fun create(@Body body: CreateSubscriptionResource): Response<SubscriptionResource>
    @POST("api/v1/subscriptions/{id}/payments") suspend fun pay(@Path("id") id: Long, @Body body: PaymentRequest): Response<PaymentResource>
    @POST("api/v1/subscriptions/{id}/renewals") suspend fun renew(@Path("id") id: Long, @Body body: PaymentRequest): Response<SubscriptionResource>
    @POST("api/v1/subscriptions/{id}/cancellations") suspend fun cancel(@Path("id") id: Long): Response<SubscriptionResource>
    @GET("api/v1/subscriptions/{id}/payments") suspend fun payments(@Path("id") id: Long): Response<List<PaymentResource>>
}
