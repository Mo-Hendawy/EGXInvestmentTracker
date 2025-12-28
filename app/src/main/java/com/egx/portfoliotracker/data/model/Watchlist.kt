package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "watchlist")
data class Watchlist(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val stockSymbol: String,
    val stockNameEn: String,
    val stockNameAr: String,
    val sector: String = "",
    val targetPrice: Double? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

