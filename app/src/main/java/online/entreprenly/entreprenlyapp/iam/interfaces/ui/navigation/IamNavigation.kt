package online.entreprenly.entreprenlyapp.iam.interfaces.ui.navigation

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.AccountScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignInScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.SignUpScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens.WelcomeScreen
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.AccountViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignInViewModel
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignUpViewModel
import online.entreprenly.entreprenlyapp.shared.infrastructure.di.AppContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.Routes

/** IAM destinations: sign in, sign up and account security (change password / email). */
fun NavGraphBuilder.iamGraph(navController: NavController, container: AppContainer, email: String) {
    composable(Routes.WELCOME) {
        WelcomeScreen(
            onCreateAccount = { navController.navigate(Routes.SIGN_UP) { launchSingleTop = true } },
            onSignIn = { navController.navigate(Routes.SIGN_IN) { launchSingleTop = true } }
        )
    }
    composable(Routes.SIGN_IN) {
        val vm: SignInViewModel = viewModel(factory = viewModelFactory {
            initializer { SignInViewModel(container.userCommandService) }
        })
        SignInScreen(
            vm,
            onBack = { navController.popBackStack() },
            onGoToSignUp = { navController.navigate(Routes.SIGN_UP) { launchSingleTop = true } }
        )
    }
    composable(Routes.SIGN_UP) {
        val vm: SignUpViewModel = viewModel(factory = viewModelFactory {
            initializer { SignUpViewModel(container.userCommandService) }
        })
        SignUpScreen(
            vm,
            onBack = { navController.popBackStack() },
            onGoToSignIn = { navController.navigate(Routes.SIGN_IN) { popUpTo(Routes.WELCOME) } }
        )
    }
    composable(Routes.ACCOUNT) {
        val vm: AccountViewModel = viewModel(factory = viewModelFactory {
            initializer { AccountViewModel(container.userCommandService) }
        })
        AccountScreen(
            viewModel = vm,
            email = email,
            onBack = { navController.popBackStack() },
            modifier = Modifier.statusBarsPadding()
        )
    }
}
