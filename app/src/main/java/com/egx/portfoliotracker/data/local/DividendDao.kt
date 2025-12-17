package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Dividend
import com.egx.portfoliotracker.data.model.PortfolioSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface DividendDao {
    
    // ============ DIVIDENDS ============
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDividend(dividend: Dividend)
    
    @Update
    suspend fun updateDividend(dividend: Dividend)
    
    @Delete
    suspend fun deleteDividend(dividend: Dividend)
    
    @Query("SELECT * FROM dividends ORDER BY paymentDate DESC")
    fun getAllDividends(): Flow<List<Dividend>>
    
    @Query("SELECT * FROM dividends WHERE holdingId = :holdingId ORDER BY paymentDate DESC")
    fun getDividendsByHolding(holdingId: String): Flow<List<Dividend>>
    
    @Query("SELECT * FROM dividends WHERE stockSymbol = :symbol ORDER BY paymentDate DESC")
    fun getDividendsBySymbol(symbol: String): Flow<List<Dividend>>
    
    @Query("SELECT SUM(totalAmount) FROM dividends WHERE holdingId = :holdingId")
    suspend fun getTotalDividendsForHolding(holdingId: String): Double?
    
    @Query("SELECT SUM(totalAmount) FROM dividends WHERE stockSymbol = :symbol")
    suspend fun getTotalDividendsForSymbol(symbol: String): Double?
    
    @Query("SELECT SUM(totalAmount) FROM dividends")
    suspend fun getTotalDividends(): Double?
    
    @Query("SELECT SUM(totalAmount) FROM dividends WHERE paymentDate >= :startTime")
    suspend fun getDividendsSince(startTime: Long): Double?
    
    @Query("SELECT * FROM dividends WHERE paymentDate >= :startTime AND paymentDate <= :endTime ORDER BY paymentDate DESC")
    fun getDividendsInRange(startTime: Long, endTime: Long): Flow<List<Dividend>>
    
    @Query("DELETE FROM dividends WHERE holdingId = :holdingId")
    suspend fun deleteDividendsByHolding(holdingId: String)
    
    // ============ PORTFOLIO SNAPSHOTS ============
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: PortfolioSnapshot)
    
    @Query("SELECT * FROM portfolio_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): PortfolioSnapshot?
    
    @Query("SELECT * FROM portfolio_snapshots WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getSnapshotsSince(startTime: Long): Flow<List<PortfolioSnapshot>>
    
    @Query("SELECT * FROM portfolio_snapshots WHERE timestamp >= :startTime ORDER BY timestamp ASC LIMIT 1")
    suspend fun getSnapshotAt(startTime: Long): PortfolioSnapshot?
    
    @Query("SELECT * FROM portfolio_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int): Flow<List<PortfolioSnapshot>>
    
    @Query("DELETE FROM portfolio_snapshots WHERE timestamp < :beforeTime")
    suspend fun deleteOldSnapshots(beforeTime: Long)
}
