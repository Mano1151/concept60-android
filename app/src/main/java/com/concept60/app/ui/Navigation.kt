package com.concept60.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.concept60.app.ui.screens.*
import com.concept60.app.ui.components.AppScaffold
import com.concept60.app.ui.components.BottomNavBar
import com.concept60.app.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Result : Screen("result/{concept}/{category}") {
        fun createRoute(concept: String, category: String) =
            "result/${concept.encodeUrl()}/${category.encodeUrl()}"
    }
    object Saved : Screen("saved")
    object Trending : Screen("trending")
    object Categories : Screen("categories")
    object PdfQa : Screen("pdf_qa")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
}

private fun String.encodeUrl(): String =
    java.net.URLEncoder.encode(this, "UTF-8")

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState(initial = null)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Trending.route,
        Screen.Saved.route,
        Screen.PdfQa.route,
        Screen.Profile.route
    )

    AppScaffold(
        navBar = {
            if (currentRoute in bottomBarScreens) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route, // Default to Home, redirect logic below
            modifier = Modifier.padding(padding)
        ) {
            // ... (rest of NavHost remains the same)

            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }

            composable(
                route = Screen.Result.route,
                arguments = listOf(
                    navArgument("concept") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                )
            ) { backStack ->
                val concept = backStack.arguments?.getString("concept")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
                val category = backStack.arguments?.getString("category")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "General"
                ResultScreen(
                    concept = concept,
                    category = category,
                    navController = navController,
                )
            }

            composable(Screen.Saved.route) {
                SavedScreen(navController = navController)
            }

            composable(Screen.Trending.route) {
                TrendingScreen(navController = navController)
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(navController = navController)
            }

            composable(Screen.PdfQa.route) {
                PdfQaScreen(navController = navController)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }

            composable(Screen.Signup.route) {
                SignupScreen(navController = navController)
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(navController = navController)
            }
        }
    }
}
