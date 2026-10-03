package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ComingSoonScreen

/** Subscription destinations. TODO(subscription team): replace the placeholder with the real screens. */
fun NavGraphBuilder.subscriptionGraph(navController: NavController) {
    composable(Routes.SUBSCRIPTION) {
        ComingSoonScreen(stringResource(R.string.more_subscription), onBack = { navController.popBackStack() })
    }
}
