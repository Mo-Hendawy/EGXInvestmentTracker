package com.egx.portfoliotracker.data.model

data class RealizedGain(
    val stockSymbol: String,
    val sharesSold: Int,
    val sellPrice: Double,
    val avgCost: Double,
    val profitLoss: Double,
    val profitLossPercent: Double,
    val sellDate: Long
)

