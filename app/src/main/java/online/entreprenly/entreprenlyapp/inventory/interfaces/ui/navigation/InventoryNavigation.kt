package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.navigation

import androidx.compose.runtime.Composable
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
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens.InventoryScreen
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ComingSoonScreen

/**
 * Inventory destinations. Screen content lands in follow-up commits, one per
 * screen; this shell already wires the shared ViewModel and detail arguments.
 */
fun NavGraphBuilder.inventoryGraph(navController: NavController, container: AppContainer) {
    composable(Routes.INVENTORY) { entry ->
        InventoryScreen(
            viewModel = inventoryViewModel(navController, entry, container),
            onProductClick = { product ->
                navController.navigate(Routes.productDetail(product.type.value, product.id))
            },
            onLotClick = { type, id -> navController.navigate(Routes.lotDetail(type, id)) }
        )
    }
    composable(
        Routes.PRODUCT_DETAIL,
        arguments = listOf(
            navArgument(Routes.ARG_PRODUCT_TYPE) { type = NavType.StringType },
            navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.LongType }
        )
    ) { entry ->
        inventoryViewModel(navController, entry, container)
        ComingSoonScreen("product")
    }
    composable(
        Routes.LOT_DETAIL,
        arguments = listOf(
            navArgument(Routes.ARG_LOT_TYPE) { type = NavType.StringType },
            navArgument(Routes.ARG_LOT_ID) { type = NavType.LongType }
        )
    ) { entry ->
        inventoryViewModel(navController, entry, container)
        ComingSoonScreen("lot")
    }
}

/**
 * The inventory tab's ViewModel, scoped to its back stack entry so the product
 * and lot detail screens opened from it share the same loaded data.
 */
@Composable
fun inventoryViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
    container: AppContainer
): InventoryViewModel {
    // Resolved once per entry: during exit transitions the inventory entry may already be gone.
    val inventoryEntry = remember(entry) { navController.getBackStackEntry(Routes.INVENTORY) }
    return viewModel(viewModelStoreOwner = inventoryEntry, factory = viewModelFactory {
        initializer {
            InventoryViewModel(
                container.productQueryService,
                container.lotQueryService,
                container.stockAlertQueryService,
                container.productCommandService,
                container.lotCommandService
            )
        }
    })
}
