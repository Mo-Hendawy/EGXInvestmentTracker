package com.egx.portfoliotracker.ui.screens.targets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.ui.components.BlurredAmount
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStockDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val isAmountsBlurred by viewModel.isAmountsBlurred.collectAsState()
    
    val totalPortfolioValue = summary?.totalValue ?: 0.0
    
    // Filter holdings with target percentages and calculate current percentages
    val holdingsWithTargets = remember(holdings, totalPortfolioValue) {
        holdings
            .filter { it.targetPercentage != null && totalPortfolioValue > 0 }
            .map { holding ->
                val currentPercentage = (holding.marketValue / totalPortfolioValue) * 100
                val difference = currentPercentage - (holding.targetPercentage ?: 0.0)
                TargetHolding(
                    holding = holding,
                    currentPercentage = currentPercentage,
                    targetPercentage = holding.targetPercentage ?: 0.0,
                    difference = difference,
                    isBelowTarget = difference < -0.5,
                    isAboveTarget = difference > 0.5
                )
            }
            .sortedByDescending { it.difference } // Above target first, then below target
    }
    
    val belowTarget = holdingsWithTargets.filter { it.isBelowTarget }
    val aboveTarget = holdingsWithTargets.filter { it.isAboveTarget }
    val atTarget = holdingsWithTargets.filter { !it.isBelowTarget && !it.isAboveTarget }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Target Allocations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAmountsBlur() }) {
                        Icon(
                            imageVector = if (isAmountsBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isAmountsBlurred) "Show amounts" else "Hide amounts"
                        )
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
            if (holdingsWithTargets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.TrackChanges,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Target Allocations",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Set target percentages in stock detail screens",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Above Target (Need to Reduce)
                if (aboveTarget.isNotEmpty()) {
                    item {
                        Text(
                            text = "Above Target (Reduce)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800) // Orange
                        )
                    }
                    items(aboveTarget, key = { it.holding.id }) { targetHolding ->
                        TargetItem(
                            targetHolding = targetHolding,
                            onClick = { onNavigateToStockDetail(targetHolding.holding.id) },
                            isBlurred = isAmountsBlurred
                        )
                    }
                }
                
                // Below Target (Need to Add)
                if (belowTarget.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Below Target (Add)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.egx.portfoliotracker.ui.theme.ProfitGreen
                        )
                    }
                    items(belowTarget, key = { it.holding.id }) { targetHolding ->
                        TargetItem(
                            targetHolding = targetHolding,
                            onClick = { onNavigateToStockDetail(targetHolding.holding.id) },
                            isBlurred = isAmountsBlurred
                        )
                    }
                }
                
                // At Target
                if (atTarget.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "At Target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(atTarget, key = { it.holding.id }) { targetHolding ->
                        TargetItem(
                            targetHolding = targetHolding,
                            onClick = { onNavigateToStockDetail(targetHolding.holding.id) },
                            isBlurred = isAmountsBlurred
                        )
                    }
                }
            }
        }
    }
}

data class TargetHolding(
    val holding: Holding,
    val currentPercentage: Double,
    val targetPercentage: Double,
    val difference: Double,
    val isBelowTarget: Boolean,
    val isAboveTarget: Boolean
)

@Composable
private fun TargetItem(
    targetHolding: TargetHolding,
    onClick: () -> Unit,
    isBlurred: Boolean
) {
    val holding = targetHolding.holding
    val backgroundColor = when {
        targetHolding.isBelowTarget -> com.egx.portfoliotracker.ui.theme.ProfitGreen.copy(alpha = 0.1f)
        targetHolding.isAboveTarget -> Color(0xFFFF9800).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val iconColor = when {
        targetHolding.isBelowTarget -> com.egx.portfoliotracker.ui.theme.ProfitGreen
        targetHolding.isAboveTarget -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when {
        targetHolding.isBelowTarget -> Icons.Default.ArrowUpward
        targetHolding.isAboveTarget -> Icons.Default.ArrowDownward
        else -> Icons.Default.CheckCircle
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = holding.stockSymbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = holding.stockNameEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBlurred) "••%" else "${String.format("%.1f", targetHolding.currentPercentage)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.0f", targetHolding.targetPercentage)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                if (targetHolding.isBelowTarget || targetHolding.isAboveTarget) {
                    Text(
                        text = if (targetHolding.isBelowTarget) {
                            "Need +${String.format("%.1f", -targetHolding.difference)}%"
                        } else {
                            "Need -${String.format("%.1f", targetHolding.difference)}%"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor
                    )
                }
            }
        }
    }
}


