package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a dividend payment received for a stock holding
 */
@Entity(tableName = "dividends")
data class Dividend(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val holdingId: String,
    val stockSymbol: String,
    val amountPerShare: Double,      // Dividend per share
    val totalShares: Int,            // Shares owned at time of dividend
    val totalAmount: Double,         // Total dividend received
    val exDividendDate: Long?,       // Ex-dividend date (optional)
    val paymentDate: Long,           // When dividend was paid/received
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val dividendYield: Double
        get() = if (totalAmount > 0 && totalShares > 0) {
            (amountPerShare / totalAmount) * 100
        } else 0.0
}

/**
 * Summary of dividends for a holding
 */
data class DividendSummary(
    val stockSymbol: String,
    val totalDividends: Double,
    val dividendCount: Int,
    val lastDividendDate: Long?,
    val averageDividendPerShare: Double
)

/**
 * Portfolio snapshot for tracking historical values
 */
@Entity(tableName = "portfolio_snapshots")
data class PortfolioSnapshot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val totalValue: Double,
    val totalCost: Double,
    val profitLoss: Double,
    val profitLossPercent: Double,
    val totalDividends: Double,
    val holdingsCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Time period for analysis
 */
enum class TimePeriod(val displayName: String, val days: Int) {
    WEEK("1 Week", 7),
    MONTH("1 Month", 30),
    TWO_MONTHS("2 Months", 60),
    FIFTY_DAYS("50 Days", 50),
    THREE_MONTHS("3 Months", 90),
    SIX_MONTHS("6 Months", 180),
    YEAR("1 Year", 365)
}

/**
 * Performance breakdown showing price gain vs dividends
 */
data class PerformanceBreakdown(
    val stockSymbol: String,
    val stockName: String,
    val priceGain: Double,           // Gain from price appreciation
    val priceGainPercent: Double,
    val dividendGain: Double,        // Total dividends received
    val dividendYield: Double,       // Dividend yield %
    val totalReturn: Double,         // Price gain + dividends
    val totalReturnPercent: Double,
    val totalCost: Double,
    val currentValue: Double
)

/**
 * Portfolio performance for a time period
 */
data class PeriodPerformance(
    val period: TimePeriod,
    val startValue: Double,
    val endValue: Double,
    val valueChange: Double,
    val valueChangePercent: Double,
    val dividendsReceived: Double,
    val totalReturn: Double,
    val totalReturnPercent: Double
)
