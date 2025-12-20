package com.egx.portfoliotracker.data.remote

import android.util.Log
import com.egx.portfoliotracker.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Service for calculating stock fair value and identifying strong buy zones
 * Uses technical analysis and price data without requiring additional API calls
 */
@javax.inject.Singleton
class StockAnalysisService @javax.inject.Inject constructor() {
    
    /**
     * Calculate fair value and buy zones for a stock
     * Uses current price data and technical indicators
     */
    suspend fun analyzeStock(
        symbol: String,
        currentPrice: Double,
        high: Double?,
        low: Double?,
        open: Double?,
        previousClose: Double?,
        avgCost: Double? = null
    ): StockAnalysis = withContext(Dispatchers.Default) {
        
        // Calculate fair value using multiple methods
        val fairValue = calculateFairValue(
            currentPrice = currentPrice,
            high = high,
            low = low,
            open = open,
            previousClose = previousClose,
            avgCost = avgCost
        )
        
        // Identify strong buy zones
        val buyZones = identifyBuyZones(
            currentPrice = currentPrice,
            high = high,
            low = low,
            previousClose = previousClose,
            avgCost = avgCost
        )
        
        // Generate recommendation
        val recommendation = generateRecommendation(
            currentPrice = currentPrice,
            fairValue = fairValue,
            buyZones = buyZones
        )
        
        StockAnalysis(
            symbol = symbol,
            currentPrice = currentPrice,
            fairValue = fairValue,
            fairValueRange = fairValue?.let { 
                Pair(it * 0.9, it * 1.1) // ±10% range
            },
            strongBuyZones = buyZones,
            recommendation = recommendation
        )
    }
    
    /**
     * Calculate fair value using multiple methods
     */
    private fun calculateFairValue(
        currentPrice: Double,
        high: Double?,
        low: Double?,
        open: Double?,
        previousClose: Double?,
        avgCost: Double?
    ): Double? {
        val methods = mutableListOf<Double>()
        
        // Method 1: Average of high/low (simple fair value)
        if (high != null && low != null) {
            val avgHighLow = (high + low) / 2.0
            methods.add(avgHighLow)
        }
        
        // Method 2: Weighted average (more weight to recent price)
        if (high != null && low != null && open != null) {
            val weighted = (currentPrice * 0.5) + (open * 0.3) + ((high + low) / 2.0 * 0.2)
            methods.add(weighted)
        }
        
        // Method 3: Mean reversion (assume price returns to average of range)
        if (high != null && low != null && previousClose != null) {
            val rangeMid = (high + low) / 2.0
            val meanReversion = (currentPrice * 0.4) + (rangeMid * 0.6)
            methods.add(meanReversion)
        }
        
        // Method 4: If we have average cost, use it as a reference
        if (avgCost != null && avgCost > 0) {
            // Fair value might be around average cost if it's a good entry
            methods.add(avgCost)
        }
        
        // Method 5: Support/Resistance midpoint
        if (high != null && low != null) {
            val support = low * 0.95 // 5% below low
            val resistance = high * 1.05 // 5% above high
            val midpoint = (support + resistance) / 2.0
            methods.add(midpoint)
        }
        
        return if (methods.isNotEmpty()) {
            // Average all methods
            methods.average()
        } else {
            null
        }
    }
    
    /**
     * Identify strong buy zones based on technical analysis
     */
    private fun identifyBuyZones(
        currentPrice: Double,
        high: Double?,
        low: Double?,
        previousClose: Double?,
        avgCost: Double?
    ): List<BuyZone> {
        val zones = mutableListOf<BuyZone>()
        
        // Zone 1: Strong support level (5-10% below current price)
        if (low != null) {
            val supportLevel = low * 0.95 // 5% below recent low
            if (supportLevel < currentPrice) {
                zones.add(
                    BuyZone(
                        priceLevel = supportLevel,
                        strength = ZoneStrength.STRONG,
                        description = "Strong support level (5% below recent low)"
                    )
                )
            }
        }
        
        // Zone 2: Average cost zone (if available)
        if (avgCost != null && avgCost > 0 && avgCost < currentPrice * 1.1) {
            zones.add(
                BuyZone(
                    priceLevel = avgCost,
                    strength = ZoneStrength.MODERATE,
                    description = "Average cost level (good entry point)"
                )
            )
        }
        
        // Zone 3: Mean reversion zone (between low and current)
        if (low != null && currentPrice > low) {
            val meanReversion = (currentPrice + low) / 2.0
            if (meanReversion < currentPrice * 0.95) {
                zones.add(
                    BuyZone(
                        priceLevel = meanReversion,
                        strength = ZoneStrength.MODERATE,
                        description = "Mean reversion zone (midpoint between low and current)"
                    )
                )
            }
        }
        
        // Zone 4: Previous close support
        if (previousClose != null && previousClose < currentPrice) {
            val prevCloseZone = previousClose * 0.98 // 2% below previous close
            if (prevCloseZone < currentPrice) {
                zones.add(
                    BuyZone(
                        priceLevel = prevCloseZone,
                        strength = ZoneStrength.WEAK,
                        description = "Previous close support level"
                    )
                )
            }
        }
        
        // Zone 5: Fibonacci retracement levels (if we have high and low)
        if (high != null && low != null && high > low) {
            val range = high - low
            // 38.2% retracement (strong support)
            val fib382 = high - (range * 0.382)
            if (fib382 < currentPrice && fib382 > low) {
                zones.add(
                    BuyZone(
                        priceLevel = fib382,
                        strength = ZoneStrength.STRONG,
                        description = "Fibonacci 38.2% retracement (strong support)"
                    )
                )
            }
            
            // 61.8% retracement (golden ratio)
            val fib618 = high - (range * 0.618)
            if (fib618 < currentPrice && fib618 > low) {
                zones.add(
                    BuyZone(
                        priceLevel = fib618,
                        strength = ZoneStrength.MODERATE,
                        description = "Fibonacci 61.8% retracement (golden ratio)"
                    )
                )
            }
        }
        
        // Sort by price level (ascending) and remove duplicates
        return zones
            .sortedBy { it.priceLevel }
            .distinctBy { round(it.priceLevel * 100) / 100 } // Round to 2 decimals for comparison
    }
    
    /**
     * Generate buy/sell/hold recommendation
     */
    private fun generateRecommendation(
        currentPrice: Double,
        fairValue: Double?,
        buyZones: List<BuyZone>
    ): Recommendation {
        if (fairValue == null) {
            return Recommendation.HOLD
        }
        
        val priceToFairValueRatio = currentPrice / fairValue
        
        // If price is significantly below fair value
        if (priceToFairValueRatio < 0.85) {
            // Check if we're near a strong buy zone
            val nearStrongZone = buyZones.any { zone ->
                zone.strength == ZoneStrength.STRONG && 
                abs(currentPrice - zone.priceLevel) / currentPrice < 0.05 // Within 5%
            }
            return if (nearStrongZone) Recommendation.STRONG_BUY else Recommendation.BUY
        }
        
        // If price is moderately below fair value
        if (priceToFairValueRatio < 0.95) {
            return Recommendation.BUY
        }
        
        // If price is around fair value
        if (priceToFairValueRatio in 0.95..1.05) {
            return Recommendation.HOLD
        }
        
        // If price is above fair value
        if (priceToFairValueRatio > 1.15) {
            return Recommendation.STRONG_SELL
        }
        
        if (priceToFairValueRatio > 1.05) {
            return Recommendation.SELL
        }
        
        return Recommendation.HOLD
    }
}
