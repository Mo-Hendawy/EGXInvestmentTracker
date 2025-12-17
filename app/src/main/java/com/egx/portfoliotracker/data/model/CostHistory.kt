package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tracks the history of average cost changes for a holding
 */
@Entity(tableName = "cost_history")
data class CostHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val holdingId: String,
    val stockSymbol: String,
    val previousAvgCost: Double,
    val newAvgCost: Double,
    val previousShares: Int,
    val newShares: Int,
    val changeType: CostChangeType,
    val transactionPrice: Double, // Price at which shares were bought/sold
    val transactionShares: Int,   // Number of shares in this transaction
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val costChange: Double get() = newAvgCost - previousAvgCost
    val costChangePercent: Double get() = if (previousAvgCost > 0) (costChange / previousAvgCost) * 100 else 0.0
    val isIncreased: Boolean get() = newAvgCost > previousAvgCost
}

enum class CostChangeType(val displayName: String) {
    BUY("Buy"),
    SELL("Sell"),
    ADJUSTMENT("Manual Adjustment")
}
