package online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.BridgeQrStateResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ChatMessageResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ChatOrderResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ConversationResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.CreateChatMessageResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.UpdateChatOrderResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Every endpoint answers 403 when the user's plan does not include the chatbot. */
interface ChatbotApi {
    @GET("api/v1/conversations")
    suspend fun getConversations(): Response<List<ConversationResource>>

    @GET("api/v1/chat-messages")
    suspend fun getMessages(@Query("conversationId") conversationId: Long): Response<List<ChatMessageResource>>

    @POST("api/v1/chat-messages")
    suspend fun createMessage(@Body resource: CreateChatMessageResource): Response<ChatMessageResource>

    @GET("api/v1/chat-orders")
    suspend fun getOrders(): Response<List<ChatOrderResource>>

    @PUT("api/v1/chat-orders/{id}")
    suspend fun updateOrder(
        @Path("id") orderId: Long,
        @Body resource: UpdateChatOrderResource
    ): Response<ChatOrderResource>

    @GET("api/v1/chatbot/whatsapp/bridge/qr")
    suspend fun getBridgeQr(): Response<BridgeQrStateResource>
}
