package com.egx.portfoliotracker.ui.screens.addstock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.egx.portfoliotracker.data.model.HoldingRole
import com.egx.portfoliotracker.data.model.HoldingStatus
import com.egx.portfoliotracker.data.model.Stock
import com.egx.portfoliotracker.data.remote.StockPriceResult
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import com.egx.portfoliotracker.viewmodel.PriceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    onNavigateBack: () -> Unit,
    onStockAdded: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val availableStocks by viewModel.availableStocks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val priceState by viewModel.priceState.collectAsState()
    val currentStockPrice by viewModel.currentStockPrice.collectAsState()
    
    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    var showStockPicker by remember { mutableStateOf(true) }
    var shares by remember { mutableStateOf("") }
    var avgCost by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(HoldingRole.CORE) }
    var selectedStatus by remember { mutableStateOf(HoldingStatus.HOLD) }
    var notes by remember { mutableStateOf("") }
    
    var roleExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    
    // Get the live price from API
    val livePrice = (currentStockPrice as? StockPriceResult.Success)?.price
    
    // Handle success
    LaunchedEffect(uiState.showAddSuccess) {
        if (uiState.showAddSuccess) {
            viewModel.dismissAddSuccess()
            viewModel.clearPriceState()
            onStockAdded()
        }
    }
    
    // Fetch price when stock is selected
    LaunchedEffect(selectedStock) {
        selectedStock?.let {
            viewModel.fetchStockPrice(it.symbol)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Stock") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearPriceState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showStockPicker && selectedStock == null) {
                // Stock Selection Screen
                StockPickerContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    stocks = availableStocks,
                    onStockSelected = { stock ->
                        selectedStock = stock
                        showStockPicker = false
                    }
                )
            } else {
                // Stock Details Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selected Stock Card with Live Price
                    selectedStock?.let { stock ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stock.symbol,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stock.nameEn,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = stock.sector,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(onClick = {
                                        selectedStock = null
                                        showStockPicker = true
                                        viewModel.clearPriceState()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Change")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Live Price Display
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Live Price",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    
                                    when (priceState) {
                                        is PriceState.Loading -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Fetching...")
                                            }
                                        }
                                        is PriceState.Success -> {
                                            livePrice?.let { price ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = ProfitGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "EGP ${String.format("%.2f", price)}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ProfitGreen
                                                    )
                                                }
                                            }
                                        }
                                        is PriceState.Error -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = LossRed,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Failed to fetch",
                                                    color = LossRed
                                                )
                                                TextButton(onClick = {
                                                    viewModel.fetchStockPrice(stock.symbol)
                                                }) {
                                                    Text("Retry")
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                                
                                // Show additional price info if available
                                (currentStockPrice as? StockPriceResult.Success)?.let { result ->
                                    result.changePercent?.let { changePercent ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Today: ${if (changePercent >= 0) "+" else ""}${String.format("%.2f", changePercent)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (changePercent >= 0) ProfitGreen else LossRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Shares input
                    OutlinedTextField(
                        value = shares,
                        onValueChange = { shares = it.filter { c -> c.isDigit() } },
                        label = { Text("Number of Shares") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        singleLine = true
                    )
                    
                    // Average Cost input
                    OutlinedTextField(
                        value = avgCost,
                        onValueChange = { avgCost = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Your Average Cost (EGP)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        supportingText = { Text("The price you bought at") },
                        singleLine = true
                    )
                    
                    // Preview calculations
                    val sharesNum = shares.toIntOrNull() ?: 0
                    val avgCostNum = avgCost.toDoubleOrNull() ?: 0.0
                    val currentPriceNum = livePrice ?: 0.0
                    
                    if (sharesNum > 0 && avgCostNum > 0 && currentPriceNum > 0) {
                        val totalCost = sharesNum * avgCostNum
                        val marketValue = sharesNum * currentPriceNum
                        val profitLoss = marketValue - totalCost
                        val profitLossPercent = (profitLoss / totalCost) * 100
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (profitLoss >= 0) 
                                    ProfitGreen.copy(alpha = 0.1f)
                                else LossRed.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Position Preview",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Cost", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "EGP ${String.format("%,.0f", totalCost)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("Market Value", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "EGP ${String.format("%,.0f", marketValue)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("P/L", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "${if (profitLoss >= 0) "+" else ""}${String.format("%.2f", profitLossPercent)}%",
                                            fontWeight = FontWeight.Bold,
                                            color = if (profitLoss >= 0) ProfitGreen else LossRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Role Dropdown
                    ExposedDropdownMenuBox(
                        expanded = roleExpanded,
                        onExpandedChange = { roleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRole.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            HoldingRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(role.displayName, fontWeight = FontWeight.Bold)
                                            Text(
                                                role.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedRole = role
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            HoldingStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = {
                                        selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add Button
                    Button(
                        onClick = {
                            selectedStock?.let { stock ->
                                viewModel.addHolding(
                                    stock = stock,
                                    shares = shares.toIntOrNull() ?: 0,
                                    avgCost = avgCost.toDoubleOrNull() ?: 0.0,
                                    currentPrice = livePrice ?: avgCost.toDoubleOrNull() ?: 0.0,
                                    role = selectedRole,
                                    status = selectedStatus,
                                    notes = notes
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedStock != null && 
                            shares.toIntOrNull() != null && shares.toInt() > 0 &&
                            avgCost.toDoubleOrNull() != null && avgCost.toDouble() > 0 &&
                            priceState is PriceState.Success
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Portfolio")
                    }
                    
                    // Show message if price not loaded
                    if (priceState !is PriceState.Success && selectedStock != null) {
                        Text(
                            text = "Waiting for live price...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockPickerContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    stocks: List<Stock>,
    onStockSelected: (Stock) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search by symbol or name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Text(
            text = "Select an EGX Stock",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No stocks found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Try a different search term",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Group stocks by sector
            val groupedStocks = stocks.groupBy { it.sector }
            
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedStocks.forEach { (sector, sectorStocks) ->
                    item {
                        Text(
                            text = sector,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(sectorStocks) { stock ->
                        StockItem(
                            stock = stock,
                            onClick = { onStockSelected(stock) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockItem(
    stock: Stock,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stock.nameAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
