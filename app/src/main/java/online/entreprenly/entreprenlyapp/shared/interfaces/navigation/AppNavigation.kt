package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.Locale
import kotlinx.coroutines.delay
import online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.navigation.chatbotGraph
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.navigation.iamGraph
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionState
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionViewModel
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.navigation.inventoryGraph
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTheme
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.navigation.profileGraph
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileUiState
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.sales.interfaces.ui.navigation.salesGraph
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.screens.HomeScreen
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.screens.SplashScreen
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.EntreprenlyAppTheme
import online.entreprenly.entreprenlyapp.subscription.interfaces.ui.navigation.subscriptionGraph

/** Routes where the bottom navigation bar is visible. */
private val barRoutes = setOf(
    Routes.HOME, Routes.INVENTORY, Routes.SELL, Routes.ORDERS, Routes.MORE,
    Routes.PROFILE, Routes.PREFERENCES, Routes.SUBSCRIPTION, Routes.ACCOUNT
)

/**
 * App entry point: applies the user's saved theme and language (US-67), then shows the navigation.
 * Preferences are cached on the device, so they apply before the network answers.
 */
@Composable
fun AppRoot(container: AppContainer) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory {
        initializer { SessionViewModel(container.sessionQueryService, container.userCommandService) }
    })
    val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory {
        initializer {
            ProfileViewModel(
                container.sessionQueryService,
                container.profileQueryService,
                container.profileCommandService
            )
        }
    })
    val preferences by profileViewModel.appPreferences.collectAsState()
    val darkTheme = preferences?.theme?.let { it == AppTheme.DARK } ?: isSystemInDarkTheme()

    WithLanguage(preferences?.language?.code) {
        EntreprenlyAppTheme(darkTheme = darkTheme) {
            AppNavigation(container, sessionViewModel, profileViewModel)
        }
    }
}

/** Overrides the resources locale for everything inside [content]; null keeps the device language. */
@Composable
private fun WithLanguage(languageCode: String?, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val localized = remember(base, languageCode) {
        if (languageCode == null) {
            base
        } else {
            val configuration = Configuration(base.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(languageCode))
            }
            base.createConfigurationContext(configuration)
        }
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration
    ) { content() }
}

@Composable
fun AppNavigation(
    container: AppContainer,
    sessionViewModel: SessionViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val sessionState by sessionViewModel.state.collectAsState()

    // Keep the splash visible for a moment even when the saved session loads instantly.
    var splashDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1200)
        splashDone = true
    }
    if (sessionState is SessionState.Loading || !splashDone) {
        SplashScreen(modifier)
        return
    }

    val signedIn = sessionState as? SessionState.SignedIn
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val profileState by profileViewModel.uiState.collectAsState()
    val loadedProfile = (profileState as? ProfileUiState.Loaded)?.profile
    val userName = loadedProfile?.firstName.orEmpty()
    val currencySymbol = loadedProfile?.preferences?.currency?.symbol ?: "S/"

    // Signing in/out replaces the whole back stack.
    LaunchedEffect(signedIn != null) {
        val target = if (signedIn != null) Routes.HOME else Routes.WELCOME
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (signedIn != null && currentRoute in barRoutes) {
                BottomNavigationBar(
                    selected = tabForRoute(currentRoute),
                    onSelect = navController::navigateToTab
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (signedIn != null) Routes.HOME else Routes.WELCOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            iamGraph(navController, container, signedIn?.session?.email.orEmpty())
            composable(Routes.HOME) {
                HomeScreen(
                    userName = userName,
                    currencySymbol = currencySymbol,
                    onSell = { navController.navigateToTab(BottomTab.SELL) },
                    onInventory = { navController.navigateToTab(BottomTab.INVENTORY) },
                    onOrders = { navController.navigateToTab(BottomTab.ORDERS) }
                )
            }
            profileGraph(
                navController = navController,
                viewModel = profileViewModel,
                email = signedIn?.session?.email.orEmpty(),
                onSignOut = sessionViewModel::signOut
            )
            inventoryGraph(navController)
            salesGraph(navController)
            chatbotGraph(navController)
            subscriptionGraph(navController)
        }
    }
}
