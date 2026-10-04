package online.entreprenly.entreprenlyapp.sales.interfaces.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import androidx.compose.runtime.remember
import online.entreprenly.entreprenlyapp.sales.interfaces.ui.screens.SalesScreen
import online.entreprenly.entreprenlyapp.sales.interfaces.ui.viewmodels.SalesViewModel

import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Sales destinations (the "Sell" tab). */
fun NavGraphBuilder.salesGraph(navController: NavController, container: AppContainer) {
    composable(Routes.SELL) {
        val viewModel: SalesViewModel = viewModel(factory = viewModelFactory {
            initializer { SalesViewModel(container.salesQueryService, container.salesCommandService) }
        })
        SalesScreen(viewModel = viewModel)
    }
}

