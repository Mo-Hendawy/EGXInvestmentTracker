package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val id: String,
    val category: String, // e.g., "Supermarket", "Cafe", "Eating Out", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other"
    val amount: Double,
    val description: String = "",
    val date: Long, // Timestamp
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Default categories
        val DEFAULT_CATEGORIES = listOf(
            "Supermarket",
            "Cafe",
            "Eating Out",
            "Transport",
            "Shopping",
            "Bills",
            "Entertainment",
            "Health",
            "Education",
            "Travel",
            "Other"
        )
    }
}
