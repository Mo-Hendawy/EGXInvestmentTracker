package com.egx.portfoliotracker.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.PortfolioSummary
import com.egx.portfoliotracker.data.model.TimePeriod
import com.egx.portfoliotracker.ui.components.*
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAddStock: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit,
    onNavigateToPerformanceCharts: () -> Unit = {},
    onNavigateToWatchlist: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToRealizedGains: () -> Unit = {},
    onNavigateToStockAnalysis: () -> Unit = {},
    onNavigateToDividendCalendar: () -> Unit = {},
    onNavigateToEditHolding: (String) -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    val holdings by viewModel.holdings.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val stockAllocation by viewModel.stockAllocation.collectAsState()
    val periodPerformances by viewModel.periodPerformances.collectAsState()
    val stockAnalyses by viewModel.stockAnalyses.collectAsState()
    val realizedGains by viewModel.realizedGains.collectAsState()
    val allDividends by viewModel.allDividends.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Calculate totals for realized gains card
    val totalRealizedGains = realizedGains.sumOf { it.profitLoss }
    val totalDividends = allDividends.sumOf { it.totalAmount }
    val totalReturns = totalRealizedGains + totalDividends
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.lastRefreshMessage) {
        uiState.lastRefreshMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRefreshMessage()
        }
    }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    LaunchedEffect(showMenu) {
        if (showMenu) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
    
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) {
            showMenu = false
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                onDismiss = { 
                    showMenu = false
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToPerformanceCharts = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToPerformanceCharts() 
                },
                onNavigateToWatchlist = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToWatchlist() 
                },
                onNavigateToBackupRestore = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToBackupRestore() 
                },
                onNavigateToRealizedGains = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToRealizedGains() 
                },
                onNavigateToStockAnalysis = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToStockAnalysis() 
                },
                onNavigateToDividendCalendar = { 
                    coroutineScope.launch {
                        drawerState.close()
                        showMenu = false
                    }
                    onNavigateToDividendCalendar() 
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EGX Portfolio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your Investment Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        showMenu = true
                        coroutineScope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Recovery button with diagnostic
                    var showRecoveryDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { 
                            showRecoveryDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Recover Data")
                    }
                    
                    if (showRecoveryDialog) {
                        var recoveryStatus by remember { mutableStateOf("Checking...") }
                        var showInitButton by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            val counts = viewModel.getDatabaseCounts()
                            recoveryStatus = "Before: Holdings: ${counts["holdings"]}, Certificates: ${counts["certificates"]}, Expenses: ${counts["expenses"]}"
                            viewModel.attemptDataRecovery()
                            val newCounts = viewModel.getDatabaseCounts()
                            recoveryStatus = "After recovery: Holdings: ${newCounts["holdings"]}, Certificates: ${newCounts["certificates"]}, Expenses: ${newCounts["expenses"]}"
                            showInitButton = newCounts["holdings"] == 0 && newCounts["certificates"] == 0
                        }
                        
                        AlertDialog(
                            onDismissRequest = { showRecoveryDialog = false },
                            title = { Text("Data Recovery Status") },
                            text = { 
                                Column {
                                    Text(recoveryStatus)
                                    if (showInitButton) {
                                        Spacer(Modifier.height(8.dp))
                                        Text("No data found. Would you like to initialize from notebook?", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                if (showInitButton) {
                                    TextButton(onClick = { 
                                        viewModel.initializeHoldingsFromNotebook()
                                        viewModel.initializeCertificatesFromNotebook()
                                        showRecoveryDialog = false
                                    }) {
                                        Text("Initialize Data")
                                    }
                                } else {
                                    TextButton(onClick = { showRecoveryDialog = false }) {
                                        Text("OK")
                                    }
                                }
                            },
                            dismissButton = {
                                if (showInitButton) {
                                    TextButton(onClick = { showRecoveryDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshAllPrices() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Prices")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddStock,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Stock")
            }
        }
    ) { paddingValues ->
        if (holdings.isEmpty()) {
            EmptyDashboard(
                onAddStock = onNavigateToAddStock,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Refresh indicator
                if (uiState.isRefreshing) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Updating prices from market...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Portfolio Allocation Donut Chart (like user's image)
                summary?.let { portfolioSummary ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Portfolio Distribution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Donut chart with total in center - full width, centered
                                    PortfolioDonutChart(
                                        stockAllocations = stockAllocation,
                                        totalValue = portfolioSummary.totalValue,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Legend below
                                    StockAllocationLegend(
                                        allocations = stockAllocation.take(10),
                                        targetPercentages = holdings.associate { it.stockSymbol to it.targetPercentage },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    
                    // Portfolio Value Card
                    item {
                        PortfolioValueCard(summary = portfolioSummary)
                    }
                    
                    // Quick Stats
                    item {
                        QuickStatsRow(summary = portfolioSummary)
                    }
                    
                    // Realized Gains + Dividends Card
                    if (totalRealizedGains != 0.0 || totalDividends > 0.0) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToRealizedGains() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (totalReturns >= 0) ProfitGreen.copy(alpha = 0.1f) else LossRed.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Realized Returns",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Realized Gains",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format("%+,.0f EGP", totalRealizedGains),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (totalRealizedGains >= 0) ProfitGreen else LossRed
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Dividends",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format("+%,.0f EGP", totalDividends),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ProfitGreen
                                            )
                                        }
                                    }
                                    
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Realized",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = String.format("%+,.0f EGP", totalReturns),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalReturns >= 0) ProfitGreen else LossRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Period Performance Cards
                    if (periodPerformances.isNotEmpty()) {
                        item {
                            Text(
                                text = "Performance Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val periods = listOf(
                                    TimePeriod.WEEK,
                                    TimePeriod.MONTH,
                                    TimePeriod.TWO_MONTHS,
                                    TimePeriod.FIFTY_DAYS
                                )
                                items(periods) { period ->
                                    periodPerformances[period]?.let { perf ->
                                        PerformancePeriodCard(
                                            periodLabel = period.displayName,
                                            valueChange = perf.valueChange,
                                            valueChangePercent = perf.valueChangePercent,
                                            dividends = perf.dividendsReceived,
                                            totalReturn = perf.totalReturn,
                                            totalReturnPercent = perf.totalReturnPercent,
                                            modifier = Modifier.width(160.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Top Performers
                    if (portfolioSummary.topGainer != null || portfolioSummary.topLoser != null) {
                        item {
                            Text(
                                text = "Performance Highlights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        portfolioSummary.topGainer?.let { gainer ->
                            item {
                                TopPerformerCard(
                                    title = "Top Gainer",
                                    symbol = gainer.stockSymbol,
                                    name = gainer.stockNameEn,
                                    profitLossPercent = gainer.profitLossPercent,
                                    isGainer = true
                                )
                            }
                        }
                        
                        portfolioSummary.topLoser?.let { loser ->
                            item {
                                TopPerformerCard(
                                    title = "Biggest Loser",
                                    symbol = loser.stockSymbol,
                                    name = loser.stockNameEn,
                                    profitLossPercent = loser.profitLossPercent,
                                    isGainer = false
                                )
                            }
                        }
                    }
                    
                    // Sector Allocation Chart
                    if (portfolioSummary.sectorAllocation.isNotEmpty()) {
                        item {
                            SectorAllocationCard(
                                sectorAllocation = portfolioSummary.sectorAllocation
                            )
                        }
                    }
                    
                    // Role Allocation
                    if (portfolioSummary.roleAllocation.isNotEmpty()) {
                        item {
                            RoleAllocationCard(
                                roleAllocation = portfolioSummary.roleAllocation.mapKeys { it.key.displayName }
                            )
                        }
                    }
                }
                
                // Action Insights
                item {
                    Text(
                        text = "Insights & Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                val reviewItems = holdings.filter { it.status == com.egx.portfoliotracker.data.model.HoldingStatus.REVIEW }
                if (reviewItems.isNotEmpty()) {
                    item {
                        ActionInsightCard(
                            title = "${reviewItems.size} Holdings Need Review",
                            description = "Some positions require your attention",
                            actionText = "Review",
                            icon = Icons.Default.RateReview,
                            color = StatusReview,
                            onAction = onNavigateToPortfolio
                        )
                    }
                }
                
                val exitItems = holdings.filter { it.status == com.egx.portfoliotracker.data.model.HoldingStatus.EXIT }
                if (exitItems.isNotEmpty()) {
                    item {
                        ActionInsightCard(
                            title = "${exitItems.size} Exit Candidates",
                            description = "Consider selling these positions",
                            actionText = "View",
                            icon = Icons.Default.ExitToApp,
                            color = StatusExit,
                            onAction = onNavigateToPortfolio
                        )
                    }
                }
                
                val buyItems = holdings.filter { it.status == com.egx.portfoliotracker.data.model.HoldingStatus.ADD }
                if (buyItems.isNotEmpty()) {
                    item {
                        ActionInsightCard(
                            title = "${buyItems.size} Buy Opportunities",
                            description = "Stocks you marked to add on dips",
                            actionText = "View",
                            icon = Icons.Default.ShoppingCart,
                            color = StatusAdd,
                            onAction = onNavigateToPortfolio
                        )
                    }
                }
                
                // Recent Holdings
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Holdings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToPortfolio) {
                            Text("View All")
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                items(holdings) { holding ->
                    val analysis = stockAnalyses.find { it.stockSymbol == holding.stockSymbol }
                    HoldingCard(
                        holding = holding,
                        recommendation = analysis?.recommendation,
                        fairValue = analysis?.fairValue,
                        onClick = { onNavigateToStockDetail(holding.id) }
                    )
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        }
    }
}

@Composable
private fun EmptyDashboard(
    onAddStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Start Your Portfolio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first EGX stock to begin\ntracking your investments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onAddStock) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Stock")
        }
    }
}

@Composable
private fun SectorAllocationCard(
    sectorAllocation: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sector Allocation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonutChart(
                    data = sectorAllocation.toList(),
                    modifier = Modifier.size(120.dp)
                )
                
                ChartLegend(
                    data = sectorAllocation.toList().take(5),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RoleAllocationCard(
    roleAllocation: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Portfolio Strategy Mix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val barData = roleAllocation.map { (role, percent) ->
                val color = when (role) {
                    "Core" -> RoleCore
                    "Income" -> RoleIncome
                    "Growth" -> RoleGrowth
                    "Swing" -> RoleSwing
                    "Speculative" -> RoleSpeculative
                    else -> NileBlue
                }
                Triple(role, percent, color)
            }
            
            HorizontalBarChart(data = barData)
        }
    }
}

@Composable
fun NavigationDrawer(
    onDismiss: () -> Unit,
    onNavigateToPerformanceCharts: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToRealizedGains: () -> Unit,
    onNavigateToStockAnalysis: () -> Unit,
    onNavigateToDividendCalendar: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                label = { Text("Performance Charts") },
                selected = false,
                onClick = onNavigateToPerformanceCharts
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                label = { Text("Watchlist") },
                selected = false,
                onClick = onNavigateToWatchlist
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                label = { Text("Backup/Restore") },
                selected = false,
                onClick = onNavigateToBackupRestore
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                label = { Text("Realized Gains") },
                selected = false,
                onClick = onNavigateToRealizedGains
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                label = { Text("Stock Analysis") },
                selected = false,
                onClick = onNavigateToStockAnalysis
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                label = { Text("Dividend Calendar") },
                selected = false,
                onClick = onNavigateToDividendCalendar
            )
        }
    }
}

