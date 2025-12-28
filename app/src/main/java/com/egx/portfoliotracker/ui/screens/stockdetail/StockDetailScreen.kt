package com.egx.portfoliotracker.ui.screens.stockdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.*
import com.egx.portfoliotracker.ui.components.RoleChip
import com.egx.portfoliotracker.ui.components.StatusChip
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    holdingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToStockAnalysis: () -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val holding = holdings.find { it.id == holdingId }
    
    // Cost history
    val costHistory by viewModel.getCostHistoryAsc(holdingId).collectAsState(initial = emptyList())
    val transactions by viewModel.getTransactionsByHolding(holdingId).collectAsState(initial = emptyList())
    val dividends by viewModel.getDividendsByHolding(holdingId).collectAsState(initial = emptyList())
    val performanceBreakdown by viewModel.performanceBreakdown.collectAsState()
    val allRealizedGains by viewModel.realizedGains.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var showDividendDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Calculate total dividends for this holding
    val totalDividends = dividends.sumOf { it.totalAmount }
    
    // Calculate realized gains for this stock
    val stockRealizedGains = allRealizedGains.filter { it.stockSymbol == holding?.stockSymbol }
    val totalRealizedGains = stockRealizedGains.sumOf { it.profitLoss }
    val totalRealizedReturns = totalRealizedGains + totalDividends
    
    if (holding == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Get performance breakdown for this stock
    val stockPerformance = performanceBreakdown.find { it.stockSymbol == holding.stockSymbol }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Holding") },
            text = { Text("Are you sure you want to delete ${holding.stockSymbol} from your portfolio?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHolding(holding)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LossRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Buy dialog
    if (showBuyDialog) {
        TransactionDialog(
            title = "Buy More Shares",
            holding = holding,
            isBuy = true,
            onDismiss = { showBuyDialog = false },
            onConfirm = { shares, price, notes ->
                viewModel.buyMoreShares(holding, shares, price, notes)
                showBuyDialog = false
            }
        )
    }
    
    // Sell dialog
    if (showSellDialog) {
        TransactionDialog(
            title = "Sell Shares",
            holding = holding,
            isBuy = false,
            onDismiss = { showSellDialog = false },
            onConfirm = { shares, price, notes ->
                viewModel.sellShares(holding, shares, price, notes)
                showSellDialog = false
            }
        )
    }
    
    // Dividend dialog
    if (showDividendDialog) {
        DividendDialog(
            holding = holding,
            onDismiss = { showDividendDialog = false },
            onConfirm = { amountPerShare, notes ->
                viewModel.addDividend(
                    holdingId = holding.id,
                    stockSymbol = holding.stockSymbol,
                    amountPerShare = amountPerShare,
                    totalShares = holding.shares,
                    paymentDate = System.currentTimeMillis(),
                    notes = notes
                )
                showDividendDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(holding.stockSymbol)
                        Text(
                            text = holding.stockNameEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LossRed)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main value card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Market Value",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "EGP ${String.format("%,.0f", holding.marketValue)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (holding.isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (holding.isProfit) ProfitGreenLight else LossRedLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${if (holding.isProfit) "+" else ""}EGP ${String.format("%,.0f", holding.profitLoss)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (holding.isProfit) ProfitGreenLight else LossRedLight
                            )
                            Text(
                                text = " (${String.format("%.2f", holding.profitLossPercent)}%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (holding.isProfit) ProfitGreenLight else LossRedLight
                            )
                        }
                    }
                }
            }
            
            // Quick action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showBuyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Buy", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Button(
                        onClick = { showSellDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sell", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Button(
                        onClick = { showDividendDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EgyptianGold),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dividend", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    Button(
                        onClick = { onNavigateToStockAnalysis() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Analysis", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
            
            // Notes section
            if (holding.notes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = holding.notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Tags
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleChip(role = holding.role)
                    StatusChip(status = holding.status)
                    if (holding.sector.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = holding.sector,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            // Fair Value Card
            if (holding.fairValue != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Fair Value",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Current Price",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        String.format("%.2f EGP", holding.currentPrice),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Fair Value",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        String.format("%.2f EGP", holding.fairValue),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val fairValuePercent = ((holding.fairValue / holding.currentPrice) - 1) * 100
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Fair Value %",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    String.format("%+.2f%%", fairValuePercent),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fairValuePercent > 0) ProfitGreen else LossRed
                                )
                            }
                        }
                    }
                }
            }
            
            // Total Return Breakdown Card (Price Gain + Dividends)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Return Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val totalReturn = holding.profitLoss + totalDividends
                        val totalReturnPercent = if (holding.totalCost > 0) {
                            (totalReturn / holding.totalCost) * 100
                        } else 0.0
                        val dividendYield = if (holding.totalCost > 0) {
                            (totalDividends / holding.totalCost) * 100
                        } else 0.0
                        
                        // Price Change Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (holding.isProfit) ProfitGreen else LossRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Price Change")
                            }
                            Text(
                                text = "${if (holding.isProfit) "+" else ""}EGP ${String.format("%,.0f", holding.profitLoss)} (${String.format("%.2f", holding.profitLossPercent)}%)",
                                fontWeight = FontWeight.Bold,
                                color = if (holding.isProfit) ProfitGreen else LossRed
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dividends Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = EgyptianGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dividends Received")
                            }
                            Text(
                                text = "+EGP ${String.format("%,.0f", totalDividends)} (${String.format("%.2f", dividendYield)}%)",
                                fontWeight = FontWeight.Bold,
                                color = EgyptianGold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Total Return Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Return",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (totalReturn >= 0) "+" else ""}EGP ${String.format("%,.0f", totalReturn)} (${String.format("%.2f", totalReturnPercent)}%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalReturn >= 0) ProfitGreen else LossRed
                            )
                        }
                    }
                }
            }
            
            // Realized Gains Card (from sells + dividends)
            if (stockRealizedGains.isNotEmpty() || totalDividends > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (totalRealizedReturns >= 0) ProfitGreen.copy(alpha = 0.1f) else LossRed.copy(alpha = 0.1f)
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
                            
                            // Realized Gains from Sells
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Sell,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (totalRealizedGains >= 0) ProfitGreen else LossRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Realized Gains (${stockRealizedGains.size} sells)")
                                }
                                Text(
                                    text = String.format("%+,.0f EGP", totalRealizedGains),
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalRealizedGains >= 0) ProfitGreen else LossRed
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Dividends Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = EgyptianGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dividends (${dividends.size} payments)")
                                }
                                Text(
                                    text = String.format("+%,.0f EGP", totalDividends),
                                    fontWeight = FontWeight.Bold,
                                    color = EgyptianGold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Total Realized Returns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total Realized",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%+,.0f EGP", totalRealizedReturns),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalRealizedReturns >= 0) ProfitGreen else LossRed
                                )
                            }
                        }
                    }
                }
            }
            
            // Position details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Position Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DetailRow(label = "Shares", value = "${holding.shares}")
                        DetailRow(label = "Average Cost", value = "EGP ${String.format("%.2f", holding.avgCost)}")
                        DetailRow(label = "Current Price", value = "EGP ${String.format("%.2f", holding.currentPrice)}")
                        DetailRow(label = "Total Cost", value = "EGP ${String.format("%,.0f", holding.totalCost)}")
                    }
                }
            }
            
            // Average Cost History Graph
            if (costHistory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Average Cost History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            AverageCostGraph(
                                history = costHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (costHistory.size >= 2) {
                                val firstCost = costHistory.first().newAvgCost
                                val lastCost = costHistory.last().newAvgCost
                                val change = lastCost - firstCost
                                val changePercent = if (firstCost > 0) (change / firstCost) * 100 else 0.0
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("First Avg", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "EGP ${String.format("%.2f", firstCost)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Change", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "${if (change >= 0) "+" else ""}${String.format("%.2f", changePercent)}%",
                                            fontWeight = FontWeight.Bold,
                                            color = if (change >= 0) LossRed else ProfitGreen
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Current Avg", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "EGP ${String.format("%.2f", lastCost)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Tabs for History
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Cost Changes") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Transactions") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dividends")
                                if (dividends.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge { Text("${dividends.size}") }
                                }
                            }
                        }
                    )
                }
            }
            
            // History content based on tab
            when (selectedTab) {
                0 -> {
                    if (costHistory.isEmpty()) {
                        item {
                            Text(
                                text = "No cost history yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(costHistory.reversed()) { history ->
                            CostHistoryItem(history = history)
                        }
                    }
                }
                1 -> {
                    if (transactions.isEmpty()) {
                        item {
                            Text(
                                text = "No transactions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(transactions) { transaction ->
                            // Find matching realized gain for sell transactions
                            val matchingGain = if (transaction.type == TransactionType.SELL) {
                                stockRealizedGains.find { 
                                    it.sharesSold == transaction.shares && 
                                    kotlin.math.abs(it.sellPrice - transaction.price) < 0.01 
                                }
                            } else null
                            TransactionItem(
                                transaction = transaction,
                                profitLoss = matchingGain?.profitLoss
                            )
                        }
                    }
                }
                2 -> {
                    if (dividends.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No dividends recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showDividendDialog = true }) {
                                    Text("Add Dividend")
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = EgyptianGold.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Dividends Received")
                                    Text(
                                        "EGP ${String.format("%,.0f", totalDividends)}",
                                        fontWeight = FontWeight.Bold,
                                        color = EgyptianGold
                                    )
                                }
                            }
                        }
                        items(dividends) { dividend ->
                            DividendItem(
                                dividend = dividend,
                                onDelete = { viewModel.deleteDividend(dividend) }
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DividendItem(
    dividend: Dividend,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Dividend") },
            text = { Text("Delete this dividend record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LossRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EgyptianGold.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = EgyptianGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dividend Payment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = dateFormat.format(Date(dividend.paymentDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${dividend.totalShares} shares × EGP ${String.format("%.2f", dividend.amountPerShare)}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                if (dividend.notes.isNotEmpty()) {
                    Text(
                        text = dividend.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+EGP ${String.format("%,.0f", dividend.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EgyptianGold
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DividendDialog(
    holding: Holding,
    onDismiss: () -> Unit,
    onConfirm: (amountPerShare: Double, notes: String) -> Unit
) {
    var amountPerShare by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val amountNum = amountPerShare.toDoubleOrNull() ?: 0.0
    val totalDividend = amountNum * holding.shares
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Dividend") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Record a dividend payment for ${holding.stockSymbol}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = amountPerShare,
                    onValueChange = { amountPerShare = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount per Share (EGP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Q3 2024 dividend") }
                )
                
                if (amountNum > 0) {
                    Divider()
                    Text(
                        "Current Shares: ${holding.shares}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Total Dividend: EGP ${String.format("%,.2f", totalDividend)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EgyptianGold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountNum > 0) {
                        onConfirm(amountNum, notes)
                    }
                },
                enabled = amountNum > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EgyptianGold)
            ) {
                Text("Add Dividend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AverageCostGraph(
    history: List<CostHistory>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return
    
    val costs = history.map { it.newAvgCost }
    val minCost = costs.minOrNull() ?: 0.0
    val maxCost = costs.maxOrNull() ?: 1.0
    val range = if (maxCost - minCost == 0.0) 1.0 else maxCost - minCost
    
    val lineColor = if (costs.last() > costs.first()) LossRed else ProfitGreen
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val graphWidth = width - padding * 2
        val graphHeight = height - padding * 2
        
        if (costs.size < 2) return@Canvas
        
        val stepX = graphWidth / (costs.size - 1)
        
        for (i in 0..4) {
            val y = padding + (graphHeight * i / 4)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }
        
        val path = Path()
        costs.forEachIndexed { index, cost ->
            val x = padding + index * stepX
            val y = padding + graphHeight - ((cost - minCost) / range * graphHeight).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        costs.forEachIndexed { index, cost ->
            val x = padding + index * stepX
            val y = padding + graphHeight - ((cost - minCost) / range * graphHeight).toFloat()
            
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun CostHistoryItem(history: CostHistory) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (history.changeType) {
                            CostChangeType.BUY -> Icons.Default.AddCircle
                            CostChangeType.SELL -> Icons.Default.RemoveCircle
                            CostChangeType.ADJUSTMENT -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = when (history.changeType) {
                            CostChangeType.BUY -> ProfitGreen
                            CostChangeType.SELL -> LossRed
                            CostChangeType.ADJUSTMENT -> NileBlue
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = history.changeType.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormat.format(Date(history.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (history.transactionShares > 0) {
                    Text(
                        text = "${history.transactionShares} shares @ EGP ${String.format("%.2f", history.transactionPrice)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Avg: ${String.format("%.2f", history.previousAvgCost)} → ${String.format("%.2f", history.newAvgCost)}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                val change = history.newAvgCost - history.previousAvgCost
                if (history.previousAvgCost > 0) {
                    Text(
                        text = "${if (change >= 0) "↑" else "↓"} ${String.format("%.2f", kotlin.math.abs(change))}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (change >= 0) LossRed else ProfitGreen
                    )
                }
                
                Text(
                    text = "Shares: ${history.previousShares} → ${history.newShares}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    profitLoss: Double? = null
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    // For sell transactions, determine color based on profit/loss
    val isSell = transaction.type == TransactionType.SELL
    val isProfit = profitLoss != null && profitLoss >= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                transaction.type == TransactionType.BUY -> ProfitGreen.copy(alpha = 0.1f)
                isSell && profitLoss != null && isProfit -> ProfitGreen.copy(alpha = 0.1f)
                isSell && profitLoss != null && !isProfit -> LossRed.copy(alpha = 0.1f)
                else -> LossRed.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (transaction.type == TransactionType.BUY) 
                            Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = if (transaction.type == TransactionType.BUY) ProfitGreen else LossRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transaction.type.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == TransactionType.BUY) ProfitGreen else LossRed
                    )
                }
                Text(
                    text = dateFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.notes.isNotEmpty()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${transaction.shares} shares",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@ EGP ${String.format("%.2f", transaction.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Total: EGP ${String.format("%,.0f", transaction.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                // Show profit/loss for sell transactions
                if (isSell && profitLoss != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%+,.0f EGP", profitLoss),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitGreen else LossRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDialog(
    title: String,
    holding: Holding,
    isBuy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (shares: Int, price: Double, notes: String) -> Unit
) {
    var shares by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(holding.currentPrice.toString()) }
    var notes by remember { mutableStateOf("") }
    
    val sharesNum = shares.toIntOrNull() ?: 0
    val priceNum = price.toDoubleOrNull() ?: 0.0
    
    val newTotalShares = if (isBuy) holding.shares + sharesNum else holding.shares - sharesNum
    val newAvgCost = if (isBuy && sharesNum > 0 && priceNum > 0) {
        val totalOldCost = holding.shares * holding.avgCost
        val totalNewCost = sharesNum * priceNum
        (totalOldCost + totalNewCost) / newTotalShares
    } else {
        holding.avgCost
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = shares,
                    onValueChange = { shares = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of Shares") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price per Share (EGP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (sharesNum > 0 && priceNum > 0) {
                    Divider()
                    Text("Preview:", fontWeight = FontWeight.Bold)
                    
                    if (isBuy) {
                        Text("Current Avg: EGP ${String.format("%.2f", holding.avgCost)}")
                        Text(
                            "New Avg: EGP ${String.format("%.2f", newAvgCost)}",
                            color = if (newAvgCost > holding.avgCost) LossRed else ProfitGreen,
                            fontWeight = FontWeight.Bold
                        )
                        val change = newAvgCost - holding.avgCost
                        Text(
                            "${if (change >= 0) "↑ Averaging UP" else "↓ Averaging DOWN"} by ${String.format("%.2f", kotlin.math.abs(change))}",
                            color = if (change >= 0) LossRed else ProfitGreen
                        )
                    } else {
                        val profit = (priceNum - holding.avgCost) * sharesNum
                        Text(
                            "Profit/Loss: ${if (profit >= 0) "+" else ""}EGP ${String.format("%,.0f", profit)}",
                            color = if (profit >= 0) ProfitGreen else LossRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text("Total Shares: ${holding.shares} → $newTotalShares")
                    Text("Transaction Total: EGP ${String.format("%,.0f", sharesNum * priceNum)}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = shares.toIntOrNull() ?: 0
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (s > 0 && p > 0) {
                        if (!isBuy && s > holding.shares) {
                            return@Button
                        }
                        onConfirm(s, p, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) ProfitGreen else LossRed
                )
            ) {
                Text(if (isBuy) "Buy" else "Sell")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
