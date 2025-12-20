package com.egx.portfoliotracker.ui.screens.portfolio

import androidx.compose.animation.AnimatedVisibility
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
import com.egx.portfoliotracker.data.model.HoldingRole
import com.egx.portfoliotracker.data.model.HoldingStatus
import com.egx.portfoliotracker.ui.components.HoldingCard
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

enum class SortOption(val displayName: String) {
    VALUE_DESC("Value ↓"),
    VALUE_ASC("Value ↑"),
    PROFIT_DESC("Profit ↓"),
    PROFIT_ASC("Profit ↑"),
    SYMBOL("Symbol"),
    RECENT("Recent")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddStock: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    
    var selectedRole by remember { mutableStateOf<HoldingRole?>(null) }
    var selectedStatus by remember { mutableStateOf<HoldingStatus?>(null) }
    var selectedSector by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.VALUE_DESC) }
    var showFilters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter and sort holdings
    val filteredHoldings = remember(holdings, selectedRole, selectedStatus, selectedSector, sortOption, searchQuery) {
        holdings
            .filter { holding ->
                (selectedRole == null || holding.role == selectedRole) &&
                (selectedStatus == null || holding.status == selectedStatus) &&
                (selectedSector == null || holding.sector == selectedSector) &&
                (searchQuery.isEmpty() || 
                    holding.stockSymbol.contains(searchQuery, ignoreCase = true) ||
                    holding.stockNameEn.contains(searchQuery, ignoreCase = true))
            }
            .let { list ->
                when (sortOption) {
                    SortOption.VALUE_DESC -> list.sortedByDescending { it.marketValue }
                    SortOption.VALUE_ASC -> list.sortedBy { it.marketValue }
                    SortOption.PROFIT_DESC -> list.sortedByDescending { it.profitLossPercent }
                    SortOption.PROFIT_ASC -> list.sortedBy { it.profitLossPercent }
                    SortOption.SYMBOL -> list.sortedBy { it.stockSymbol }
                    SortOption.RECENT -> list.sortedByDescending { it.updatedAt }
                }
            }
    }
    
    val sectors = remember(holdings) {
        holdings.map { it.sector }.distinct().filter { it.isNotEmpty() }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (selectedRole != null || selectedStatus != null || selectedSector != null)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToAddStock) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search stocks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            
            // Filters section
            AnimatedVisibility(visible = showFilters) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Sort options
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SortOption.entries) { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = { sortOption = option },
                                label = { Text(option.displayName) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Role filter
                    Text(
                        text = "Filter by Role",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedRole == null,
                                onClick = { selectedRole = null },
                                label = { Text("All") }
                            )
                        }
                        items(HoldingRole.entries) { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = if (selectedRole == role) null else role },
                                label = { Text(role.displayName) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status filter
                    Text(
                        text = "Filter by Status",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { selectedStatus = null },
                                label = { Text("All") }
                            )
                        }
                        items(HoldingStatus.entries) { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = if (selectedStatus == status) null else status },
                                label = { Text(status.displayName) }
                            )
                        }
                    }
                    
                    // Sector filter
                    if (sectors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Filter by Sector",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedSector == null,
                                    onClick = { selectedSector = null },
                                    label = { Text("All") }
                                )
                            }
                            items(sectors) { sector ->
                                FilterChip(
                                    selected = selectedSector == sector,
                                    onClick = { selectedSector = if (selectedSector == sector) null else sector },
                                    label = { Text(sector) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                }
            }
            
            // Summary row
            summary?.let { portfolioSummary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Showing",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${filteredHoldings.size}/${holdings.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Value",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "EGP ${String.format("%,.0f", filteredHoldings.sumOf { it.marketValue })}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "P/L",
                                style = MaterialTheme.typography.labelSmall
                            )
                            val filteredPL = filteredHoldings.sumOf { it.profitLoss }
                            Text(
                                text = "${if (filteredPL >= 0) "+" else ""}${String.format("%,.0f", filteredPL)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (filteredPL >= 0) 
                                    com.egx.portfoliotracker.ui.theme.ProfitGreen 
                                else com.egx.portfoliotracker.ui.theme.LossRed
                            )
                        }
                    }
                }
            }
            
            // Holdings list
            if (filteredHoldings.isEmpty()) {
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
                            text = "No holdings found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Try adjusting your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHoldings, key = { it.id }) { holding ->
                        HoldingCard(
                            holding = holding,
                            onClick = { onNavigateToStockDetail(holding.id) }
                        )
                    }
                }
            }
        }
    }
}
