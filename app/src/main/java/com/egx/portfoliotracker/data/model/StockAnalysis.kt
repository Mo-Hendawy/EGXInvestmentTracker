package com.egx.portfoliotracker.data.model

/**
 * Stock Analysis using Benjamin Graham's Value Investing principles
 * 
 * Fair Value Calculation (Graham's Formula):
 * Fair Value = EPS × (8.5 + 2g)
 * Where:
 * - EPS = Earnings Per Share
 * - g = Expected annual growth rate (%)
 * - 8.5 = Base P/E for a zero-growth company
 * - 2 = Multiplier for growth
 */
data class StockAnalysis(
    val stockSymbol: String,
    val currentPrice: Double,
    val fairValue: Double?, // Calculated or user-defined fair value
    val upsidePercent: Double?, // Potential upside: ((fairValue / currentPrice) - 1) * 100
    val marginOfSafety: Double?, // How much below fair value: ((fairValue - currentPrice) / fairValue) * 100
    val recommendation: Recommendation,
    val eps: Double? = null, // Earnings Per Share
    val growthRate: Double? = null, // Expected growth rate (%)
    val peRatio: Double? = null, // Current P/E ratio
    val hasFairValue: Boolean = fairValue != null,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class Recommendation {
    STRONG_BUY,  // Price is 30%+ below fair value (great margin of safety)
    BUY,         // Price is 10-30% below fair value
    HOLD,        // Price is within 10% of fair value
    SELL,        // Price is 10-30% above fair value
    STRONG_SELL, // Price is 30%+ above fair value
    NO_DATA      // No EPS data to calculate fair value
}

