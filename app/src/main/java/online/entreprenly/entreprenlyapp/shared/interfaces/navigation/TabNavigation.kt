package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

import androidx.navigation.NavController

/**
 * Navigates to a bottom-bar tab. Always use this (also for shortcuts) so the back stack stays
 * consistent: Home is the root, every other tab sits right above it.
 */
fun NavController.navigateToTab(tab: BottomTab) {
    if (tab == BottomTab.HOME) {
        // Home is the root of the signed-in graph: go back to it instead of navigating to it again.
        // Navigating with popUpTo(HOME) + restoreState leaves the user stuck on the current tab.
        if (!popBackStack(Routes.HOME, inclusive = false)) {
            navigate(Routes.HOME) { launchSingleTop = true }
        }
        return
    }
    navigate(tab.route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
