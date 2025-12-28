package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a stock holding in the user's portfolio
 */
@Entity(tableName = "holdings")
data class Holding(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val stockSymbol: String,
    val stockNameEn: String,
    val stockNameAr: String,
    val shares: Int,
    val avgCost: Double,
    val currentPrice: Double,
    val role: HoldingRole = HoldingRole.CORE,
    val status: HoldingStatus = HoldingStatus.HOLD,
    val sector: String = "",
    val notes: String = "",
    val targetPercentage: Double? = null,  // Target allocation percentage (0-100)
    val fairValue: Double? = null,  // User-defined fair value override (optional)
    val eps: Double? = null,  // Earnings Per Share (from company financials)
    val growthRate: Double? = null,  // Expected annual EPS growth rate (%)
    val peRatio: Double? = null,  // Current P/E ratio
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Calculated properties
    val marketValue: Double get() = shares * currentPrice
    val totalCost: Double get() = shares * avgCost
    val profitLoss: Double get() = marketValue - totalCost
    val profitLossPercent: Double get() = if (totalCost > 0) (profitLoss / totalCost) * 100 else 0.0
    val isProfit: Boolean get() = profitLoss >= 0
    
    // Calculate fair value using Benjamin Graham's formula: EPS × (8.5 + 2g)
    // Where g is the expected growth rate
    val calculatedFairValue: Double? get() {
        val epsVal = eps ?: return null
        val growth = growthRate ?: 5.0  // Default 5% growth if not specified
        return epsVal * (8.5 + 2 * growth)
    }
    
    // Use user-defined fair value if set, otherwise use calculated
    val effectiveFairValue: Double? get() = fairValue ?: calculatedFairValue
}

enum class HoldingRole(val displayName: String, val description: String) {
    CORE("Core", "Long-term strategic holdings"),
    INCOME("Income", "Dividend-focused investments"),
    GROWTH("Growth", "High growth potential stocks"),
    SWING("Swing", "Short to medium term trades"),
    SPECULATIVE("Speculative", "Higher risk/reward positions")
}

enum class HoldingStatus(val displayName: String, val actionColor: String) {
    HOLD("Hold", "#4CAF50"),
    ADD("Add on Dips", "#2196F3"),
    REDUCE("Reduce", "#FF9800"),
    EXIT("Exit", "#F44336"),
    REVIEW("Review", "#9C27B0"),
    WATCH("Watch", "#607D8B")
}

/**
 * Portfolio summary data
 */
data class PortfolioSummary(
    val totalValue: Double,
    val totalCost: Double,
    val totalProfitLoss: Double,
    val totalProfitLossPercent: Double,
    val holdingsCount: Int,
    val profitableCount: Int,
    val losingCount: Int,
    val topGainer: Holding?,
    val topLoser: Holding?,
    val sectorAllocation: Map<String, Double>,
    val roleAllocation: Map<HoldingRole, Double>
)

/**
 * Sector performance data
 */
data class SectorPerformance(
    val sector: String,
    val totalValue: Double,
    val totalCost: Double,
    val profitLoss: Double,
    val profitLossPercent: Double,
    val weight: Double,
    val holdingsCount: Int
)

/**
 * Transaction record for history tracking
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val holdingId: String,
    val stockSymbol: String,
    val type: TransactionType,
    val shares: Int,
    val price: Double,
    val total: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    BUY, SELL, DIVIDEND
}
