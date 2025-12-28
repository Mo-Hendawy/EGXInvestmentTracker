package com.egx.portfoliotracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.HoldingRole
import com.egx.portfoliotracker.data.model.HoldingStatus
import com.egx.portfoliotracker.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HoldingCard(
    holding: Holding,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    recommendation: com.egx.portfoliotracker.data.model.Recommendation? = null,
    fairValue: Double? = null,
    modifier: Modifier = Modifier
) {
    val profitColor by animateColorAsState(
        targetValue = if (holding.isProfit) ProfitGreen else LossRed,
        label = "profit_color"
    )
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ar", "EG")) }
    val percentFormat = remember { NumberFormat.getPercentInstance().apply { maximumFractionDigits = 2 } }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row: Symbol, Name, Recommendation, P/L indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = holding.stockSymbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (recommendation != null) {
                            RecommendationChip(recommendation = recommendation)
                        }
                    }
                    Text(
                        text = holding.stockNameEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // P/L Badge
                Surface(
                    color = profitColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (holding.isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = profitColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (holding.isProfit) "+" else ""}${String.format("%.2f", holding.profitLossPercent)}%",
                            color = profitColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Shares",
                    value = "${holding.shares}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Avg Cost",
                    value = String.format("%.2f", holding.avgCost),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Current",
                    value = String.format("%.2f", holding.currentPrice),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Value",
                    value = String.format("%.0f", holding.marketValue),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Fair Value row (always show if available from analysis or holding)
            val displayFairValue = fairValue ?: holding.fairValue
            if (displayFairValue != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Fair Value",
                        value = String.format("%.2f", displayFairValue),
                        modifier = Modifier.weight(1f)
                    )
                    val fairValuePercent = ((displayFairValue / holding.currentPrice) - 1) * 100
                    StatItem(
                        label = "Fair Value %",
                        value = String.format("%+.2f%%", fairValuePercent),
                        modifier = Modifier.weight(1f),
                        valueColor = if (fairValuePercent > 0) ProfitGreen else LossRed
                    )
                    Spacer(modifier = Modifier.weight(2f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tags row: Role and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleChip(role = holding.role)
                StatusChip(status = holding.status)
                
                if (holding.sector.isNotEmpty()) {
                    SectorChip(sector = holding.sector)
                }
            }
            
            // Notes if present
            if (holding.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = holding.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RoleChip(role: HoldingRole, modifier: Modifier = Modifier) {
    val color = when (role) {
        HoldingRole.CORE -> RoleCore
        HoldingRole.INCOME -> RoleIncome
        HoldingRole.GROWTH -> RoleGrowth
        HoldingRole.SWING -> RoleSwing
        HoldingRole.SPECULATIVE -> RoleSpeculative
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = role.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusChip(status: HoldingStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        HoldingStatus.HOLD -> StatusHold
        HoldingStatus.ADD -> StatusAdd
        HoldingStatus.REDUCE -> StatusReduce
        HoldingStatus.EXIT -> StatusExit
        HoldingStatus.REVIEW -> StatusReview
        HoldingStatus.WATCH -> StatusWatch
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectorChip(sector: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = sector,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun RecommendationChip(recommendation: com.egx.portfoliotracker.data.model.Recommendation, modifier: Modifier = Modifier) {
    val (color, text) = when (recommendation) {
        com.egx.portfoliotracker.data.model.Recommendation.STRONG_BUY -> ProfitGreen to "STRONG BUY"
        com.egx.portfoliotracker.data.model.Recommendation.BUY -> ProfitGreen.copy(alpha = 0.8f) to "BUY"
        com.egx.portfoliotracker.data.model.Recommendation.HOLD -> MaterialTheme.colorScheme.onSurfaceVariant to "HOLD"
        com.egx.portfoliotracker.data.model.Recommendation.SELL -> LossRed.copy(alpha = 0.8f) to "SELL"
        com.egx.portfoliotracker.data.model.Recommendation.STRONG_SELL -> LossRed to "STRONG SELL"
        com.egx.portfoliotracker.data.model.Recommendation.NO_DATA -> MaterialTheme.colorScheme.outline to "NO DATA"
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
