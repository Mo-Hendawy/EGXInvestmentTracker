package com.egx.portfoliotracker.ui.screens.stockanalysis

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.Recommendation
import com.egx.portfoliotracker.ui.theme.ProfitGreen
import com.egx.portfoliotracker.ui.theme.LossRed
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val isRefreshing by viewModel.isRefreshingFinancials.collectAsState()
    val refreshResult by viewModel.financialsRefreshResult.collectAsState()
    
    var editingHolding by remember { mutableStateOf<Holding?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(refreshResult) {
        refreshResult?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFinancialsResult()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshFinancialData() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Fetch Data")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card explaining the model
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "EGX 3-Tier Fair Value Model",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "• Base FV = Normalized EPS × Low P/E (6)\n" +
                            "• Growth FV = Forward EPS × Justified P/E\n" +
                            "• Justified P/E = 8 + (Growth% ÷ 2)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Stock cards
            items(holdings) { holding ->
                StockAnalysisCard(
                    holding = holding,
                    onEditClick = { editingHolding = holding }
                )
            }
        }
    }
    
    // Edit dialog
    editingHolding?.let { holding ->
        EditFinancialsDialog(
            holding = holding,
            onDismiss = { editingHolding = null },
            onSave = { updatedHolding ->
                viewModel.updateHolding(updatedHolding)
                viewModel.refreshStockAnalyses()
                editingHolding = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAnalysisCard(
    holding: Holding,
    onEditClick: () -> Unit
) {
    // Determine recommendation based on growth fair value
    val recommendation = when {
        holding.growthFairValue == null && holding.baseFairValue == null -> Recommendation.NO_DATA
        else -> {
            val fv = holding.effectiveFairValue ?: return@StockAnalysisCard
            val upside = ((fv / holding.currentPrice) - 1) * 100
            when {
                upside >= 50 -> Recommendation.STRONG_BUY
                upside >= 20 -> Recommendation.BUY
                upside >= -10 -> Recommendation.HOLD
                upside >= -30 -> Recommendation.SELL
                else -> Recommendation.STRONG_SELL
            }
        }
    }
    
    val recommendationColor = when (recommendation) {
        Recommendation.STRONG_BUY -> ProfitGreen
        Recommendation.BUY -> Color(0xFF4CAF50)
        Recommendation.HOLD -> Color(0xFFFF9800)
        Recommendation.SELL -> LossRed
        Recommendation.STRONG_SELL -> Color(0xFFD32F2F)
        Recommendation.NO_DATA -> Color(0xFF9E9E9E)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = holding.stockSymbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to edit inputs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Badge(
                    containerColor = recommendationColor.copy(alpha = 0.2f),
                    contentColor = recommendationColor
                ) {
                    Text(
                        if (recommendation == Recommendation.NO_DATA) "SET DATA"
                        else recommendation.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // Current Price
            Text(
                "Current Price: ${String.format("%.2f", holding.currentPrice)} EGP",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            // Raw Data Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 Market Data", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DataItem("EPS", holding.eps?.let { String.format("%.2f", it) } ?: "-")
                        DataItem("P/E", holding.peRatio?.let { String.format("%.1f", it) } ?: "-")
                        DataItem("Book", holding.bookValue?.let { String.format("%.2f", it) } ?: "-")
                    }
                }
            }
            
            // User Inputs Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("✏️ Your Inputs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DataItem("Norm EPS", holding.normalizedEps?.let { String.format("%.2f", it) } ?: "-")
                        DataItem("Fwd EPS", holding.forwardEps?.let { String.format("%.2f", it) } ?: "-")
                        DataItem("Growth%", holding.growthRate?.let { String.format("%.0f%%", it) } ?: "-")
                        DataItem("Low P/E", holding.lowCyclePE?.let { String.format("%.0f", it) } ?: "-")
                    }
                }
            }
            
            Divider()
            
            // 3 Fair Values
            Text("💰 Fair Values", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            
            // Base FV
            FairValueRow(
                label = "Base FV",
                formula = "Norm EPS × Low P/E",
                value = holding.baseFairValue,
                currentPrice = holding.currentPrice
            )
            
            // Growth FV
            FairValueRow(
                label = "Growth FV",
                formula = "Fwd EPS × Current P/E (${holding.peRatio?.let { String.format("%.1f", it) } ?: "?"})",
                value = holding.growthFairValue,
                currentPrice = holding.currentPrice,
                isHighlighted = true
            )
            
            // P/B FV
            FairValueRow(
                label = "P/B FV",
                formula = "Book × 2",
                value = holding.pbFairValue,
                currentPrice = holding.currentPrice
            )
            
            // Manual Override if set
            if (holding.fairValue != null) {
                FairValueRow(
                    label = "Override",
                    formula = "Manual",
                    value = holding.fairValue,
                    currentPrice = holding.currentPrice,
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
fun DataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FairValueRow(
    label: String,
    formula: String,
    value: Double?,
    currentPrice: Double,
    isHighlighted: Boolean = false
) {
    val upside = value?.let { ((it / currentPrice) - 1) * 100 }
    val upsideColor = when {
        upside == null -> Color.Gray
        upside >= 20 -> ProfitGreen
        upside >= 0 -> Color(0xFF4CAF50)
        upside >= -20 -> Color(0xFFFF9800)
        else -> LossRed
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) Modifier.padding(vertical = 4.dp)
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                label,
                style = if (isHighlighted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                formula,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value?.let { String.format("%.2f", it) } ?: "-",
                style = if (isHighlighted) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                upside?.let { String.format("(%+.0f%%)", it) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = upsideColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFinancialsDialog(
    holding: Holding,
    onDismiss: () -> Unit,
    onSave: (Holding) -> Unit
) {
    // Market data (from scraping)
    var epsText by remember { mutableStateOf(holding.eps?.toString() ?: "") }
    var peText by remember { mutableStateOf(holding.peRatio?.toString() ?: "") }
    var bookValueText by remember { mutableStateOf(holding.bookValue?.toString() ?: "") }
    
    // User inputs for valuation
    var normalizedEpsText by remember { mutableStateOf(holding.normalizedEps?.toString() ?: "") }
    var forwardEpsText by remember { mutableStateOf(holding.forwardEps?.toString() ?: "") }
    var growthRateText by remember { mutableStateOf(holding.growthRate?.toString() ?: "") }
    var lowCyclePEText by remember { mutableStateOf(holding.lowCyclePE?.toString() ?: "") }
    var fairValueOverrideText by remember { mutableStateOf(holding.fairValue?.toString() ?: "") }
    
    // Calculate preview values - NO DEFAULTS, USE REAL VALUES ONLY
    val normEps = normalizedEpsText.toDoubleOrNull() ?: epsText.toDoubleOrNull()
    val lowPE = lowCyclePEText.toDoubleOrNull()  // REQUIRED - no default
    val baseFV = if (normEps != null && normEps > 0 && lowPE != null && lowPE > 0) normEps * lowPE else null
    
    val growthRate = growthRateText.toDoubleOrNull()
    val currentPE = peText.toDoubleOrNull()  // Use REAL P/E ratio, not formula
    val justifiedPE = currentPE?.takeIf { it > 0 }  // Use actual P/E ratio
    val fwdEps = forwardEpsText.toDoubleOrNull() ?: run {
        val currEps = epsText.toDoubleOrNull() ?: return@run null
        growthRate?.let { currEps * (1 + it / 100.0) }
    }
    val growthFV = if (fwdEps != null && fwdEps > 0 && justifiedPE != null) fwdEps * justifiedPE else null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${holding.stockSymbol}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Current Price: ${String.format("%.2f", holding.currentPrice)} EGP",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Divider()
                Text("📊 Market Data (from scraping)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = epsText,
                        onValueChange = { epsText = it },
                        label = { Text("EPS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = peText,
                        onValueChange = { peText = it },
                        label = { Text("P/E") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                OutlinedTextField(
                    value = bookValueText,
                    onValueChange = { bookValueText = it },
                    label = { Text("Book Value Per Share") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Divider()
                Text("✏️ Your Valuation Inputs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = normalizedEpsText,
                        onValueChange = { normalizedEpsText = it },
                        label = { Text("Norm EPS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text("Avg EPS") }
                    )
                    OutlinedTextField(
                        value = lowCyclePEText,
                        onValueChange = { lowCyclePEText = it },
                        label = { Text("Low P/E") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text("Default 6") }
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = forwardEpsText,
                        onValueChange = { forwardEpsText = it },
                        label = { Text("Fwd EPS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text("Next year") }
                    )
                    OutlinedTextField(
                        value = growthRateText,
                        onValueChange = { growthRateText = it },
                        label = { Text("Growth %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text("Annual") }
                    )
                }
                
                Divider()
                
                // Preview Fair Values
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💰 Fair Value Preview", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base FV:")
                            Text(
                                baseFV?.let { String.format("%.2f EGP", it) } ?: "-",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (justifiedPE != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Justified P/E:")
                                Text(String.format("%.1f", justifiedPE), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Growth FV:")
                            Text(
                                growthFV?.let { 
                                    val upside = ((it / holding.currentPrice) - 1) * 100
                                    String.format("%.2f EGP (%+.0f%%)", it, upside) 
                                } ?: "-",
                                fontWeight = FontWeight.Bold,
                                color = growthFV?.let { 
                                    if (it > holding.currentPrice) ProfitGreen else LossRed 
                                } ?: Color.Gray
                            )
                        }
                    }
                }
                
                Divider()
                
                OutlinedTextField(
                    value = fairValueOverrideText,
                    onValueChange = { fairValueOverrideText = it },
                    label = { Text("Manual Fair Value Override") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Leave empty to use calculated values") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedHolding = holding.copy(
                        eps = epsText.toDoubleOrNull(),
                        peRatio = peText.toDoubleOrNull(),
                        bookValue = bookValueText.toDoubleOrNull(),
                        normalizedEps = normalizedEpsText.toDoubleOrNull(),
                        forwardEps = forwardEpsText.toDoubleOrNull(),
                        growthRate = growthRateText.toDoubleOrNull(),
                        lowCyclePE = lowCyclePEText.toDoubleOrNull(),
                        fairValue = fairValueOverrideText.toDoubleOrNull(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updatedHolding)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
