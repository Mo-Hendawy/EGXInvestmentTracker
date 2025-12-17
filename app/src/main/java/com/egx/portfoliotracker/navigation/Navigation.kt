package com.egx.portfoliotracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.egx.portfoliotracker.ui.screens.addstock.AddStockScreen
import com.egx.portfoliotracker.ui.screens.dashboard.DashboardScreen
import com.egx.portfoliotracker.ui.screens.portfolio.PortfolioScreen
import com.egx.portfoliotracker.ui.screens.stockdetail.StockDetailScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Portfolio : Screen("portfolio")
    object AddStock : Screen("add_stock")
    object StockDetail : Screen("stock_detail/{holdingId}") {
        fun createRoute(holdingId: String) = "stock_detail/$holdingId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToPortfolio = {
                    navController.navigate(Screen.Portfolio.route)
                },
                onNavigateToAddStock = {
                    navController.navigate(Screen.AddStock.route)
                },
                onNavigateToStockDetail = { holdingId ->
                    navController.navigate(Screen.StockDetail.createRoute(holdingId))
                }
            )
        }
        
        composable(Screen.Portfolio.route) {
            PortfolioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddStock = {
                    navController.navigate(Screen.AddStock.route)
                },
                onNavigateToStockDetail = { holdingId ->
                    navController.navigate(Screen.StockDetail.createRoute(holdingId))
                }
            )
        }
        
        composable(Screen.AddStock.route) {
            AddStockScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStockAdded = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(
                navArgument("holdingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val holdingId = backStackEntry.arguments?.getString("holdingId") ?: return@composable
            StockDetailScreen(
                holdingId = holdingId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
