package online.entreprenly.entreprenlyapp.sales.interfaces.ui.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ComingSoonScreen

/** Sales destinations (the "Sell" tab). TODO(sales team): replace the placeholder with the real screens. */
fun NavGraphBuilder.salesGraph(navController: NavController) {
    composable(Routes.SELL) {
        ComingSoonScreen(stringResource(R.string.nav_sell))
    }
}
