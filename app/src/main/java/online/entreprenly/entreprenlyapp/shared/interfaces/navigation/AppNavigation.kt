package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.AccountScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignInScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignUpScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.AccountViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionState
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SessionViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignInViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignUpViewModel
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer

<<<<<<< Updated upstream
object Routes {
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val ACCOUNT = "account"
}
=======
/** Routes where the bottom navigation bar is visible. */
private val barRoutes = setOf(
    Routes.HOME, Routes.INVENTORY, Routes.SELL, Routes.ORDERS, Routes.MORE,
    Routes.PROFILE, Routes.PREFERENCES, Routes.ACCOUNT,
    Routes.PRODUCT_DETAIL, Routes.LOT_DETAIL
)
>>>>>>> Stashed changes

@Composable
fun AppNavigation(container: AppContainer, modifier: Modifier = Modifier) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory {
        initializer { SessionViewModel(container.sessionQueryService, container.userCommandService) }
    })
<<<<<<< Updated upstream
=======
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
    val systemConfiguration = LocalConfiguration.current
    val languageConfiguration = remember(systemConfiguration, languageCode) {
        Configuration(systemConfiguration).apply {
            if (languageCode != null) setLocale(Locale.forLanguageTag(languageCode))
        }
    }
    val localized = remember(base, languageCode, languageConfiguration) {
        if (languageCode == null) {
            base
        } else {
            base.createConfigurationContext(languageConfiguration)
        }
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides languageConfiguration
    ) { content() }
}

@Composable
fun AppNavigation(
    container: AppContainer,
    sessionViewModel: SessionViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
>>>>>>> Stashed changes
    val sessionState by sessionViewModel.state.collectAsState()

    if (sessionState is SessionState.Loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val signedIn = sessionState as? SessionState.SignedIn
    val navController = rememberNavController()

    // Al iniciar/cerrar sesión se reemplaza todo el back stack.
    LaunchedEffect(signedIn != null) {
        val target = if (signedIn != null) Routes.ACCOUNT else Routes.SIGN_IN
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (signedIn != null) Routes.ACCOUNT else Routes.SIGN_IN,
        modifier = modifier
    ) {
        composable(Routes.SIGN_IN) {
            val vm: SignInViewModel = viewModel(factory = viewModelFactory {
                initializer { SignInViewModel(container.userCommandService) }
            })
            SignInScreen(vm, onGoToSignUp = { navController.navigateSingleTop(Routes.SIGN_UP) })
        }
        composable(Routes.SIGN_UP) {
            val vm: SignUpViewModel = viewModel(factory = viewModelFactory {
                initializer { SignUpViewModel(container.userCommandService) }
            })
            SignUpScreen(vm, onGoToSignIn = { navController.popBackStack() })
        }
        composable(Routes.ACCOUNT) {
            val vm: AccountViewModel = viewModel(factory = viewModelFactory {
                initializer { AccountViewModel(container.userCommandService) }
            })
            AccountScreen(
                viewModel = vm,
                email = signedIn?.session?.email.orEmpty(),
                onSignOut = sessionViewModel::signOut
            )
<<<<<<< Updated upstream
=======
            inventoryGraph(navController, container)
            salesGraph(navController, container)
            chatbotGraph(navController, container)
            subscriptionGraph(navController, container)
>>>>>>> Stashed changes
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) =
    navigate(route) { launchSingleTop = true }
