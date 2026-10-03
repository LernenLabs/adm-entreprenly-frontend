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

object Routes {
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val ACCOUNT = "account"
}

@Composable
fun AppNavigation(container: AppContainer, modifier: Modifier = Modifier) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory {
        initializer { SessionViewModel(container.sessionQueryService, container.userCommandService) }
    })
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
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) =
    navigate(route) { launchSingleTop = true }
