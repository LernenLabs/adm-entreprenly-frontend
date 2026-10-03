package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ComingSoonScreen

/** Chatbot destinations (the "Orders" tab). TODO(chatbot team): replace the placeholder with the real screens. */
fun NavGraphBuilder.chatbotGraph(navController: NavController) {
    composable(Routes.ORDERS) {
        ComingSoonScreen(stringResource(R.string.nav_orders))
    }
}
