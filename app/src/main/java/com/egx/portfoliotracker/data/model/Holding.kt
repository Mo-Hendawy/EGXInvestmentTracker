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
    val eps: Double? = null,  // Current EPS (from TradingView/Mubasher)
    val growthRate: Double? = null,  // Expected annual EPS growth rate (%)
    val peRatio: Double? = null,  // Current P/E ratio (from TradingView/Mubasher)
    val bookValue: Double? = null,  // Book Value Per Share
    val normalizedEps: Double? = null,  // Normalized/Average EPS (user input)
    val forwardEps: Double? = null,  // Forward EPS estimate (user input)
    val lowCyclePE: Double? = null,  // Low-cycle P/E for base valuation (REQUIRED - no default)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Calculated properties
    val marketValue: Double get() = shares * currentPrice
    val totalCost: Double get() = shares * avgCost
    val profitLoss: Double get() = marketValue - totalCost
    val profitLossPercent: Double get() = if (totalCost > 0) (profitLoss / totalCost) * 100 else 0.0
    val isProfit: Boolean get() = profitLoss >= 0
    
    // ============================================
    // EGX 3-TIER FAIR VALUE MODEL
    // ============================================
    
    // 1. BASE FAIR VALUE (Conservative)
    // Formula: Normalized EPS × Low-cycle P/E
    // REQUIRES: normalizedEps (or eps) AND lowCyclePE - NO DEFAULTS
    val baseFairValue: Double? get() {
        val normEps = normalizedEps ?: eps ?: return null
        val lowPE = lowCyclePE ?: return null  // REQUIRED - no default
        if (normEps <= 0 || lowPE <= 0) return null
        return normEps * lowPE
    }
    
    // 2. GROWTH FAIR VALUE (Market-based)
    // Formula: Forward EPS × Justified P/E
    // Justified P/E = Current P/E ratio (REAL VALUE) - NO FORMULA, USE ACTUAL P/E
    val justifiedPE: Double? get() {
        // Use REAL current P/E ratio, not a formula
        return peRatio?.takeIf { it > 0 }
    }
    
    val growthFairValue: Double? get() {
        // REQUIRES: forwardEps (or eps + growthRate) AND peRatio - NO DEFAULTS
        val fwdEps = forwardEps ?: run {
            val currEps = eps ?: return null
            val growth = growthRate ?: return null
            if (currEps <= 0) return null
            currEps * (1 + growth / 100.0)
        }
        val justPE = peRatio ?: return null  // Use REAL P/E ratio, not formula
        if (fwdEps <= 0 || justPE <= 0) return null
        return fwdEps * justPE
    }
    
    // 3. SIMPLE FAIR VALUE (P/B based fallback)
    // Formula: Book Value × 2
    val pbFairValue: Double? get() {
        val bvps = bookValue ?: return null
        if (bvps <= 0) return null
        return bvps * 2.0
    }
    
    // Use user-defined fair value override if set, otherwise use Growth FV, then Base FV
    val effectiveFairValue: Double? get() = fairValue ?: growthFairValue ?: baseFairValue ?: pbFairValue
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
    val avgCostAtSale: Double? = null,  // For SELL transactions: avg cost at time of sale
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    BUY, SELL, DIVIDEND
}
