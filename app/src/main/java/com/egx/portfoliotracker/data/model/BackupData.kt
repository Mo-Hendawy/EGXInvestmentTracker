package com.egx.portfoliotracker.data.model

data class BackupData(
    val holdings: List<Holding>,
    val certificates: List<Certificate>,
    val expenses: List<Expense>,
    val transactions: List<Transaction>,
    val dividends: List<Dividend>,
    val costHistory: List<CostHistory>,
    val watchlist: List<Watchlist>,
    val timestamp: Long = System.currentTimeMillis(),
    val version: String = "1.0"
)

