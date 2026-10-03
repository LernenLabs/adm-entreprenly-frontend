package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ComingSoonScreen

/** Inventory destinations. TODO(inventory team): replace the placeholder with the real screens. */
fun NavGraphBuilder.inventoryGraph(navController: NavController) {
    composable(Routes.INVENTORY) {
        ComingSoonScreen(stringResource(R.string.nav_inventory))
    }
}
