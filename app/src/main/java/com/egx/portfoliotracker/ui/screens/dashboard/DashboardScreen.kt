package com.egx.portfoliotracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.PortfolioSummary
import com.egx.portfoliotracker.data.model.TimePeriod
import com.egx.portfoliotracker.ui.components.*
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAddStock: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val stockAllocation by viewModel.stockAllocation.collectAsState()
    val periodPerformances by viewModel.periodPerformances.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isAmountsBlurred by viewModel.isAmountsBlurred.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.lastRefreshMessage) {
        uiState.lastRefreshMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRefreshMessage()
        }
    }
    
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
                actions = {
                    // Privacy/Blur toggle
                    IconButton(
                        onClick = { viewModel.toggleAmountsBlur() }
                    ) {
                        Icon(
                            imageVector = if (isAmountsBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isAmountsBlurred) "Show amounts" else "Hide amounts",
                            tint = if (isAmountsBlurred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Refresh button
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
                                
                                // Donut chart centered
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PortfolioDonutChart(
                                        stockAllocations = stockAllocation,
                                        totalValue = portfolioSummary.totalValue,
                                        isBlurred = isAmountsBlurred,
                                        modifier = Modifier.size(280.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Legend below chart
                                StockAllocationLegend(
                                    allocations = stockAllocation.take(10),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // Portfolio Value Card
                    item {
                        PortfolioValueCard(
                            summary = portfolioSummary,
                            isBlurred = isAmountsBlurred
                        )
                    }
                    
                    // Quick Stats
                    item {
                        QuickStatsRow(summary = portfolioSummary)
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
                
                items(holdings.take(5)) { holding ->
                    HoldingCard(
                        holding = holding,
                        onClick = { onNavigateToStockDetail(holding.id) },
                        isBlurred = isAmountsBlurred
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
