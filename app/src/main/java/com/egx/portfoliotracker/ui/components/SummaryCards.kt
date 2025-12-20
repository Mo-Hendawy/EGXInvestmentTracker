package com.egx.portfoliotracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.egx.portfoliotracker.data.model.PortfolioSummary
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.ui.components.BlurredAmountNoDecimals
import com.egx.portfoliotracker.ui.components.BlurredPercentage

@Composable
fun PortfolioValueCard(
    summary: PortfolioSummary,
    isBlurred: Boolean = false,
    modifier: Modifier = Modifier
) {
    val profitColor = if (summary.totalProfitLoss >= 0) ProfitGreen else LossRed
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Total Portfolio Value",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BlurredAmountNoDecimals(
                amount = summary.totalValue,
                currency = "EGP",
                isBlurred = isBlurred,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    BlurredAmountNoDecimals(
                        amount = summary.totalCost,
                        currency = "EGP",
                        isBlurred = isBlurred,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Profit/Loss",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (summary.totalProfitLoss >= 0) 
                                Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (summary.totalProfitLoss >= 0) 
                                ProfitGreenLight else LossRedLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BlurredAmountNoDecimals(
                                amount = summary.totalProfitLoss,
                                currency = if (summary.totalProfitLoss >= 0) "+EGP" else "EGP",
                                isBlurred = isBlurred,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.totalProfitLoss >= 0) 
                                    ProfitGreenLight else LossRedLight
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            BlurredPercentage(
                                percentage = summary.totalProfitLossPercent,
                                isBlurred = isBlurred,
                                showSign = false,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.totalProfitLoss >= 0) 
                                    ProfitGreenLight else LossRedLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(
    summary: PortfolioSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "Holdings",
            value = "${summary.holdingsCount}",
            icon = Icons.Default.AccountBalance,
            color = NileBlue,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Profitable",
            value = "${summary.profitableCount}",
            icon = Icons.Default.ThumbUp,
            color = ProfitGreen,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Losing",
            value = "${summary.losingCount}",
            icon = Icons.Default.ThumbDown,
            color = LossRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TopPerformerCard(
    title: String,
    symbol: String,
    name: String,
    profitLossPercent: Double,
    isGainer: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isGainer) ProfitGreen else LossRed
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isGainer) Icons.Default.EmojiEvents else Icons.Default.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${if (profitLossPercent >= 0) "+" else ""}${String.format("%.2f", profitLossPercent)}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ActionInsightCard(
    title: String,
    description: String,
    actionText: String,
    icon: ImageVector,
    color: Color,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            TextButton(onClick = onAction) {
                Text(text = actionText, color = color)
            }
        }
    }
}
