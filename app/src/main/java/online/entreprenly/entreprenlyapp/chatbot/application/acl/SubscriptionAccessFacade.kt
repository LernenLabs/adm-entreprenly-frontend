package online.entreprenly.entreprenlyapp.chatbot.application.acl

import online.entreprenly.entreprenlyapp.shared.application.result.Result

/**
 * What the chatbot needs from the subscription context: whether the signed-in user's plan
 * includes the chatbot (Plan Control). The subscription context can provide its own
 * implementation and replace the temporary one in `chatbot/infrastructure/acl`.
 */
interface SubscriptionAccessFacade {
    suspend fun hasChatbotAccess(): Result<Boolean>
}
