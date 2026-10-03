package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens.ChatScreen
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.screens.OrdersScreen
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatViewModel
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.ChatbotState
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.viewmodels.OrdersViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.AccountScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignInScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignUpScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.AccountViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionState
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignInViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignUpViewModel
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer

object Routes {
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val ACCOUNT = "account"
    const val ORDERS = "orders"
    const val ARG_CONVERSATION_ID = "conversationId"
    const val CHAT = "conversations/{$ARG_CONVERSATION_ID}"

    fun chat(conversationId: Long) = "conversations/$conversationId"
}

/** Destinations reachable from the bottom navigation bar once signed in. */
private enum class TopLevelTab(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    ACCOUNT(Routes.ACCOUNT, R.string.nav_account, Icons.Filled.Person),
    ORDERS(Routes.ORDERS, R.string.nav_orders, Icons.Filled.ShoppingCart)
}

@Composable
fun AppNavigation(container: AppContainer, modifier: Modifier = Modifier) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory {
        initializer { SessionViewModel(container.sessionQueryService, container.userCommandService) }
    })
    val sessionState by sessionViewModel.state.collectAsState()

    if (sessionState is SessionState.Loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val signedIn = sessionState as? SessionState.SignedIn
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // TODO(subscription): navigate to the plans screen once the subscription context adds it.
    val onViewPlans: () -> Unit = { }

    // Signing in/out replaces the whole back stack.
    LaunchedEffect(signedIn != null) {
        val target = if (signedIn != null) Routes.ACCOUNT else Routes.SIGN_IN
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        modifier = modifier,
        // The activity's Scaffold already applies the system bar insets.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (signedIn != null && TopLevelTab.entries.any { it.route == currentRoute }) {
                BottomNavigationBar(currentRoute, onSelect = { navController.navigateToTab(it.route) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (signedIn != null) Routes.ACCOUNT else Routes.SIGN_IN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SIGN_IN) {
                val vm: SignInViewModel = viewModel(factory = viewModelFactory {
                    initializer { SignInViewModel(container.userCommandService) }
                })
                SignInScreen(vm, onGoToSignUp = { navController.navigateSingleTop(Routes.SIGN_UP) })
            }
            composable(Routes.SIGN_UP) {
                val vm: SignUpViewModel = viewModel(factory = viewModelFactory {
                    initializer { SignUpViewModel(container.userCommandService) }
                })
                SignUpScreen(vm, onGoToSignIn = { navController.popBackStack() })
            }
            composable(Routes.ACCOUNT) {
                val vm: AccountViewModel = viewModel(factory = viewModelFactory {
                    initializer { AccountViewModel(container.userCommandService) }
                })
                AccountScreen(
                    viewModel = vm,
                    email = signedIn?.session?.email.orEmpty(),
                    onSignOut = sessionViewModel::signOut
                )
            }
            composable(Routes.ORDERS) { entry ->
                val vm = ordersViewModel(navController, entry, container)
                OrdersScreen(
                    viewModel = vm,
                    onOpenOrder = { },
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
        }
    }
}

@Composable
private fun BottomNavigationBar(currentRoute: String?, onSelect: (TopLevelTab) -> Unit) {
    NavigationBar(windowInsets = WindowInsets(0)) {
        TopLevelTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) }
            )
        }
    }
}

/**
 * The orders tab's ViewModel, scoped to its back stack entry so the chat and order detail
 * screens opened from it share the same loaded orders and conversations.
 */
@Composable
private fun ordersViewModel(
    navController: NavHostController,
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
                container.whatsAppConnectionQueryService
            )
        }
    })
}

/** Switches tabs keeping a single copy of each and restoring its saved state. */
private fun NavHostController.navigateToTab(route: String) =
    navigate(route) {
        popUpTo(Routes.ACCOUNT) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

private fun NavHostController.navigateSingleTop(route: String) =
    navigate(route) { launchSingleTop = true }
