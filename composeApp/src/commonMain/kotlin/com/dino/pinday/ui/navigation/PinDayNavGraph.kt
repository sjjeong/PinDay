package com.dino.pinday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dino.pinday.ui.add.AddEditScreen
import com.dino.pinday.ui.detail.DetailScreen
import com.dino.pinday.ui.home.HomeScreen
import com.dino.pinday.ui.onboarding.OnboardingScreen
import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Serializable
object HomeRoute

@Serializable
data class AddEditRoute(val id: Long = -1L)

@Serializable
data class DetailRoute(val id: Long)

@Composable
fun PinDayNavGraph(
    navController: NavHostController = rememberNavController(),
    startOnboarding: Boolean = true,
) {
    NavHost(
        navController = navController,
        startDestination = if (startOnboarding) OnboardingRoute else HomeRoute,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(HomeRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<HomeRoute> {
            HomeScreen(
                onAddClick = { navController.navigate(AddEditRoute()) },
                onItemClick = { id -> navController.navigate(DetailRoute(id)) },
            )
        }
        composable<AddEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AddEditRoute>()
            AddEditScreen(
                anniversaryId = if (route.id == -1L) null else route.id,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            DetailScreen(
                anniversaryId = route.id,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(AddEditRoute(id)) },
                onDeleted = { navController.popBackStack() },
            )
        }
    }
}
