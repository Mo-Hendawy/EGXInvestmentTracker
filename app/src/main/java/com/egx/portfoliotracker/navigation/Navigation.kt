package com.egx.portfoliotracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.egx.portfoliotracker.ui.screens.addstock.AddStockScreen
import com.egx.portfoliotracker.ui.screens.certificates.AddCertificateScreen
import com.egx.portfoliotracker.ui.screens.certificates.CertificatesScreen
import com.egx.portfoliotracker.ui.screens.dashboard.DashboardScreen
import com.egx.portfoliotracker.ui.screens.expenses.AddEditExpenseScreen
import com.egx.portfoliotracker.ui.screens.expenses.CategoryManagementScreen
import com.egx.portfoliotracker.ui.screens.expenses.ExpensesScreen
import com.egx.portfoliotracker.ui.screens.portfolio.PortfolioScreen
import com.egx.portfoliotracker.ui.screens.stockdetail.StockDetailScreen
import com.egx.portfoliotracker.ui.screens.performance.PerformanceChartsScreen
import com.egx.portfoliotracker.ui.screens.watchlist.WatchlistScreen
import com.egx.portfoliotracker.ui.screens.watchlist.AddEditWatchlistScreen
import com.egx.portfoliotracker.ui.screens.backup.BackupRestoreScreen
import com.egx.portfoliotracker.ui.screens.realizedgains.RealizedGainsScreen
import com.egx.portfoliotracker.ui.screens.editholding.EditHoldingScreen
import com.egx.portfoliotracker.ui.screens.stockanalysis.StockAnalysisScreen
import com.egx.portfoliotracker.ui.screens.dividendcalendar.DividendCalendarScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Portfolio : Screen("portfolio")
    object Certificates : Screen("certificates")
    object Expenses : Screen("expenses")
    object CategoryManagement : Screen("category_management")
    object Targets : Screen("targets")
    object AddStock : Screen("add_stock")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object AddCertificate : Screen("add_certificate")
    object EditCertificate : Screen("edit_certificate/{certificateId}") {
        fun createRoute(certificateId: String) = "edit_certificate/$certificateId"
    }
    object StockDetail : Screen("stock_detail/{holdingId}") {
        fun createRoute(holdingId: String) = "stock_detail/$holdingId"
    }
    object CertificateDetail : Screen("certificate_detail/{certificateId}") {
        fun createRoute(certificateId: String) = "certificate_detail/$certificateId"
    }
    object PerformanceCharts : Screen("performance_charts")
    object Watchlist : Screen("watchlist")
    object AddEditWatchlist : Screen("add_edit_watchlist/{watchlistId}") {
        fun createRoute(watchlistId: String?) = if (watchlistId != null) "add_edit_watchlist/$watchlistId" else "add_edit_watchlist/new"
    }
    object BackupRestore : Screen("backup_restore")
    object RealizedGains : Screen("realized_gains")
    object EditHolding : Screen("edit_holding/{holdingId}") {
        fun createRoute(holdingId: String) = "edit_holding/$holdingId"
    }
    object StockAnalysis : Screen("stock_analysis")
    object DividendCalendar : Screen("dividend_calendar")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screen.Dashboard.route
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", maxLines = 1) },
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        if (currentRoute != Screen.Dashboard.route) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Portfolio") },
                    label = { Text("Portfolio", maxLines = 1) },
                    selected = currentRoute == Screen.Portfolio.route,
                    onClick = {
                        if (currentRoute != Screen.Portfolio.route) {
                            navController.navigate(Screen.Portfolio.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Expenses") },
                    label = { Text("Expenses", maxLines = 1) },
                    selected = currentRoute == Screen.Expenses.route,
                    onClick = {
                        if (currentRoute != Screen.Expenses.route) {
                            navController.navigate(Screen.Expenses.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Targets") },
                    label = { Text("Targets", maxLines = 1) },
                    selected = currentRoute == Screen.Targets.route,
                    onClick = {
                        if (currentRoute != Screen.Targets.route) {
                            navController.navigate(Screen.Targets.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Certificates") },
                    label = { Text("Certificates", maxLines = 1) },
                    selected = currentRoute == Screen.Certificates.route,
                    onClick = {
                        if (currentRoute != Screen.Certificates.route) {
                            navController.navigate(Screen.Certificates.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
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
                },
                onNavigateToPerformanceCharts = {
                    navController.navigate(Screen.PerformanceCharts.route)
                },
                onNavigateToWatchlist = {
                    navController.navigate(Screen.Watchlist.route)
                },
                onNavigateToBackupRestore = {
                    navController.navigate(Screen.BackupRestore.route)
                },
                onNavigateToRealizedGains = {
                    navController.navigate(Screen.RealizedGains.route)
                },
                onNavigateToStockAnalysis = {
                    navController.navigate(Screen.StockAnalysis.route)
                },
                onNavigateToDividendCalendar = {
                    navController.navigate(Screen.DividendCalendar.route)
                },
                onNavigateToEditHolding = { holdingId ->
                    navController.navigate(Screen.EditHolding.createRoute(holdingId))
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
                },
                onNavigateToEditHolding = { holdingId ->
                    navController.navigate(Screen.EditHolding.createRoute(holdingId))
                },
                onNavigateToPerformanceCharts = {
                    navController.navigate(Screen.PerformanceCharts.route)
                },
                onNavigateToWatchlist = {
                    navController.navigate(Screen.Watchlist.route)
                },
                onNavigateToBackupRestore = {
                    navController.navigate(Screen.BackupRestore.route)
                },
                onNavigateToRealizedGains = {
                    navController.navigate(Screen.RealizedGains.route)
                },
                onNavigateToStockAnalysis = {
                    navController.navigate(Screen.StockAnalysis.route)
                },
                onNavigateToDividendCalendar = {
                    navController.navigate(Screen.DividendCalendar.route)
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
                },
                onNavigateToEdit = { editHoldingId ->
                    navController.navigate(Screen.EditHolding.createRoute(editHoldingId))
                },
                onNavigateToStockAnalysis = {
                    navController.navigate(Screen.StockAnalysis.route)
                }
            )
        }
        
        composable(Screen.Expenses.route) {
            ExpensesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToEditExpense = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                },
                onNavigateToCategoryManagement = {
                    navController.navigate(Screen.CategoryManagement.route)
                }
            )
        }
        
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AddExpense.route) {
            AddEditExpenseScreen(
                expenseId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
            AddEditExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Targets.route) {
            com.egx.portfoliotracker.ui.screens.targets.TargetsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStockDetail = { holdingId: String ->
                    navController.navigate(Screen.StockDetail.createRoute(holdingId))
                }
            )
        }
        
        composable(Screen.Certificates.route) {
            CertificatesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCertificate = {
                    navController.navigate(Screen.AddCertificate.route)
                },
                onNavigateToCertificateDetail = { certificateId ->
                    navController.navigate(Screen.CertificateDetail.createRoute(certificateId))
                }
            )
        }
        
        composable(Screen.AddCertificate.route) {
            AddCertificateScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCertificateAdded = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.CertificateDetail.route,
            arguments = listOf(
                navArgument("certificateId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val certificateId = backStackEntry.arguments?.getString("certificateId") ?: return@composable
            com.egx.portfoliotracker.ui.screens.certificates.CertificateDetailScreen(
                certificateId = certificateId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditCertificate.createRoute(certificateId))
                }
            )
        }
        
        composable(
            route = Screen.EditCertificate.route,
            arguments = listOf(
                navArgument("certificateId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val certificateId = backStackEntry.arguments?.getString("certificateId") ?: return@composable
            com.egx.portfoliotracker.ui.screens.certificates.EditCertificateScreen(
                certificateId = certificateId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PerformanceCharts.route) {
            PerformanceChartsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddEdit = { watchlistId ->
                    navController.navigate(Screen.AddEditWatchlist.createRoute(watchlistId))
                }
            )
        }
        
        composable(
            route = Screen.AddEditWatchlist.route,
            arguments = listOf(
                navArgument("watchlistId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val watchlistId = backStackEntry.arguments?.getString("watchlistId")
            AddEditWatchlistScreen(
                watchlistId = if (watchlistId == "new") null else watchlistId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.RealizedGains.route) {
            RealizedGainsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditHolding.route,
            arguments = listOf(
                navArgument("holdingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val holdingId = backStackEntry.arguments?.getString("holdingId") ?: return@composable
            EditHoldingScreen(
                holdingId = holdingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.StockAnalysis.route) {
            StockAnalysisScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.DividendCalendar.route) {
            DividendCalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        }
    }
}
