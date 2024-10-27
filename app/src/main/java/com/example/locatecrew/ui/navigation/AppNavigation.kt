// File path: app/src/main/java/com/example/locatecrew/ui/navigation/AppNavigation.kt
package com.example.locatecrew.ui.navigation

import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.locatecrew.ui.screens.AddAnnouncementScreen
import com.example.locatecrew.ui.screens.CreateGroupScreen
import com.example.locatecrew.ui.screens.DashboardScreen
import com.example.locatecrew.ui.screens.GroupDetailsScreen
import com.example.locatecrew.ui.screens.LoginScreen
import com.example.locatecrew.ui.screens.MapViewScreen
import com.example.locatecrew.ui.screens.RegisterScreen
import com.example.locatecrew.ui.screens.SplashScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("create_group") {
            CreateGroupScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable(
            route = "group/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GroupDetailsScreen(groupId = groupId, navController = navController)
            } else {
                // Handle invalid groupId
            }
        }
        composable(
            route = "add_announcement/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                AddAnnouncementScreen(groupId, navController = navController)
            }
        }
        composable(
            route = "group/{groupId}/map",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                MapViewScreen(
                    groupId = groupId,
                    viewModel = hiltViewModel(),
                    userViewModel = hiltViewModel()
                )
            }
        }
    }
}