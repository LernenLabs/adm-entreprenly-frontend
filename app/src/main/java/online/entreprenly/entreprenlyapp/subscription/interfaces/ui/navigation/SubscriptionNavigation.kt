package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.navigation

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.BottomTab
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.navigateToTab
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.screens.SubscriptionScreen
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.viewmodels.SubscriptionViewModel

fun NavGraphBuilder.subscriptionGraph(navController: NavController, container: AppContainer) {
    composable(Routes.SUBSCRIPTION) {
        val vm: SubscriptionViewModel = viewModel(factory = viewModelFactory {
            initializer { SubscriptionViewModel(container.sessionQueryService, container.subscriptionQueryService, container.subscriptionCommandService, createSavedStateHandle()) }
        })
        SubscriptionScreen(vm, onBack = { navController.popBackStack() }, onWhatsApp = { navController.navigateToTab(BottomTab.ORDERS) })
    }
}
