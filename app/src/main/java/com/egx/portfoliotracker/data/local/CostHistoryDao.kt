package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.CostHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CostHistoryDao {
    
    @Query("SELECT * FROM cost_history WHERE holdingId = :holdingId ORDER BY timestamp DESC")
    fun getHistoryByHolding(holdingId: String): Flow<List<CostHistory>>
    
    @Query("SELECT * FROM cost_history WHERE stockSymbol = :symbol ORDER BY timestamp DESC")
    fun getHistoryBySymbol(symbol: String): Flow<List<CostHistory>>
    
    @Query("SELECT * FROM cost_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<CostHistory>>
    
    @Query("SELECT * FROM cost_history WHERE holdingId = :holdingId ORDER BY timestamp ASC")
    fun getHistoryByHoldingAsc(holdingId: String): Flow<List<CostHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CostHistory)
    
    @Query("DELETE FROM cost_history WHERE holdingId = :holdingId")
    suspend fun deleteHistoryByHolding(holdingId: String)
    
    @Query("DELETE FROM cost_history")
    suspend fun deleteAllHistory()
}
