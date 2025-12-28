package com.egx.portfoliotracker.ui.screens.stockanalysis

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
import com.egx.portfoliotracker.data.model.Recommendation
import com.egx.portfoliotracker.data.model.StockAnalysis
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
    val stockAnalyses by viewModel.stockAnalyses.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (stockAnalyses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No analysis available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stockAnalyses) { analysis ->
                    StockAnalysisCard(analysis = analysis)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAnalysisCard(analysis: StockAnalysis) {
    val recommendationColor = when (analysis.recommendation) {
        Recommendation.STRONG_BUY -> ProfitGreen
        Recommendation.BUY -> Color(0xFF4CAF50)
        Recommendation.HOLD -> Color(0xFFFF9800)
        Recommendation.SELL -> LossRed
        Recommendation.STRONG_SELL -> Color(0xFFD32F2F)
        Recommendation.NO_DATA -> Color(0xFF9E9E9E)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = analysis.stockSymbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = recommendationColor.copy(alpha = 0.2f),
                    contentColor = recommendationColor
                ) {
                    Text(
                        if (analysis.recommendation == Recommendation.NO_DATA) "SET FAIR VALUE"
                        else analysis.recommendation.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Divider()
            
            // Current Price vs Fair Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Current Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        String.format("%.2f EGP", analysis.currentPrice),
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
                        analysis.fairValue?.let { String.format("%.2f EGP", it) } ?: "Not Set",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.hasFairValue) recommendationColor else Color(0xFF9E9E9E)
                    )
                }
            }
            
            if (analysis.hasFairValue && analysis.upsidePercent != null && analysis.marginOfSafety != null) {
                // Upside/Downside Potential
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Upside Potential",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        String.format("%+.1f%%", analysis.upsidePercent),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.upsidePercent >= 0) ProfitGreen else LossRed
                    )
                }
                
                // Margin of Safety
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Margin of Safety",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        String.format("%+.1f%%", analysis.marginOfSafety),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.marginOfSafety >= 0) ProfitGreen else LossRed
                    )
                }
                
                Divider()
                
                // Price Targets based on fair value
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Price Targets",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Strong Buy (<70%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                String.format("%.2f EGP", analysis.fairValue!! * 0.7),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ProfitGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Buy (<90%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                String.format("%.2f EGP", analysis.fairValue!! * 0.9),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Fair Value",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                String.format("%.2f EGP", analysis.fairValue!!),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            } else {
                // No fair value set message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Set a fair value in Edit Stock to get buy/sell recommendations",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }
        }
    }
}

