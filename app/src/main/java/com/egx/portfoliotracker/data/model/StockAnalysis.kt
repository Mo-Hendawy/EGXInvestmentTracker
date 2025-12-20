package com.egx.portfoliotracker.data.model

/**
 * Stock analysis data including fair value and buy zones
 */
data class StockAnalysis(
    val symbol: String,
    val currentPrice: Double,
    val fairValue: Double?,
    val fairValueRange: Pair<Double, Double>?, // Lower and upper bound
    val strongBuyZones: List<BuyZone>,
    val recommendation: Recommendation,
    val analysisDate: Long = System.currentTimeMillis()
)

data class BuyZone(
    val priceLevel: Double,
    val strength: ZoneStrength,
    val description: String
)

enum class ZoneStrength {
    STRONG,    // Very attractive buying opportunity
    MODERATE,  // Good buying opportunity
    WEAK       // Minor buying opportunity
}

enum class Recommendation {
    STRONG_BUY,
    BUY,
    HOLD,
    SELL,
    STRONG_SELL
}
