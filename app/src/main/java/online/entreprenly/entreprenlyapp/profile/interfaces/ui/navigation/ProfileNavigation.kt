package online.entreprenly.entreprenlyapp.profile.interfaces.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens.EditProfileScreen
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens.MoreScreen
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens.NotificationsScreen
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens.PreferencesScreen
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens.ProfileScreen
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes

/** Profile destinations plus the "More" tab that links to them. */
fun NavGraphBuilder.profileGraph(
    navController: NavController,
    viewModel: ProfileViewModel,
    email: String,
    onSignOut: () -> Unit
) {
    // Expired session (US-62): signing out clears the JWT and the app returns to sign-in.
    val signIn = onSignOut

    composable(Routes.MORE) {
        MoreScreen(
            viewModel = viewModel,
            onProfile = { navController.navigate(Routes.PROFILE) },
            onPreferences = { navController.navigate(Routes.PREFERENCES) },
            onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            onSubscription = { navController.navigate(Routes.SUBSCRIPTION) },
            onAccountSecurity = { navController.navigate(Routes.ACCOUNT) },
            onSignOut = onSignOut
        )
    }
    composable(Routes.PROFILE) {
        ProfileScreen(
            viewModel = viewModel,
            email = email,
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(Routes.PROFILE_EDIT) },
            onPreferences = { navController.navigate(Routes.PREFERENCES) },
            onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            onChangeEmail = { navController.navigate(Routes.ACCOUNT) },
            onSignIn = signIn
        )
    }
    composable(Routes.PROFILE_EDIT) {
        EditProfileScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
            onSignIn = signIn
        )
    }
    composable(Routes.PREFERENCES) {
        PreferencesScreen(viewModel, onBack = { navController.popBackStack() }, onSignIn = signIn)
    }
    composable(Routes.NOTIFICATIONS) {
        NotificationsScreen(viewModel, onBack = { navController.popBackStack() }, onSignIn = signIn)
    }
}
