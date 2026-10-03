package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens.ChatScreen
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens.OrderDetailScreen
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens.OrdersScreen
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatViewModel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatbotState
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.OrdersViewModel
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes

/** Chatbot destinations: the "Orders" tab, a conversation's chat and an order's detail. */
fun NavGraphBuilder.chatbotGraph(navController: NavController, container: AppContainer) {
    val onViewPlans: () -> Unit = { navController.navigate(Routes.SUBSCRIPTION) }

    composable(Routes.ORDERS) { entry ->
        OrdersScreen(
            viewModel = ordersViewModel(navController, entry, container),
            onOpenOrder = { navController.navigate(Routes.orderDetail(it)) },
            onOpenConversation = { navController.navigate(Routes.chat(it)) },
            onViewPlans = onViewPlans
        )
    }
    composable(
        Routes.CHAT,
        arguments = listOf(navArgument(Routes.ARG_CONVERSATION_ID) { type = NavType.LongType })
    ) { entry ->
        val conversationId = entry.arguments?.getLong(Routes.ARG_CONVERSATION_ID) ?: 0L
        val vm: ChatViewModel = viewModel(factory = viewModelFactory {
            initializer {
                ChatViewModel(conversationId, container.chatMessageQueryService, container.chatMessageCommandService)
            }
        })
        val chatbotState by ordersViewModel(navController, entry, container).state.collectAsState()
        ChatScreen(
            viewModel = vm,
            clientName = (chatbotState as? ChatbotState.Ready)?.conversation(conversationId)?.displayName,
            onBack = { navController.popBackStack() },
            onViewPlans = onViewPlans
        )
    }
    composable(
        Routes.ORDER_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_ORDER_ID) { type = NavType.LongType })
    ) { entry ->
        OrderDetailScreen(
            viewModel = ordersViewModel(navController, entry, container),
            orderId = entry.arguments?.getLong(Routes.ARG_ORDER_ID) ?: 0L,
            onBack = { navController.popBackStack() },
            onViewPlans = onViewPlans
        )
    }
}

/**
 * The orders tab's ViewModel, scoped to its back stack entry so the chat and order detail
 * screens opened from it share the same loaded orders and conversations.
 */
@Composable
private fun ordersViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
    container: AppContainer
): OrdersViewModel {
    // Resolved once per entry: during exit transitions the orders entry may already be gone.
    val ordersEntry = remember(entry) { navController.getBackStackEntry(Routes.ORDERS) }
    return viewModel(viewModelStoreOwner = ordersEntry, factory = viewModelFactory {
        initializer {
            OrdersViewModel(
                container.subscriptionAccessFacade,
                container.chatOrderQueryService,
                container.conversationQueryService,
                container.whatsAppConnectionQueryService,
                container.chatOrderCommandService
            )
        }
    })
}
