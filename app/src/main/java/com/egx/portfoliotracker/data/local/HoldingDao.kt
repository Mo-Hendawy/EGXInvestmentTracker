package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.HoldingRole
import com.egx.portfoliotracker.data.model.HoldingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {
    
    @Query("SELECT * FROM holdings ORDER BY (shares * currentPrice) DESC")
    fun getAllHoldings(): Flow<List<Holding>>
    
    @Query("SELECT * FROM holdings WHERE id = :id")
    suspend fun getHoldingById(id: String): Holding?
    
    @Query("SELECT * FROM holdings WHERE stockSymbol = :symbol")
    suspend fun getHoldingBySymbol(symbol: String): Holding?
    
    @Query("SELECT * FROM holdings WHERE sector = :sector ORDER BY (shares * currentPrice) DESC")
    fun getHoldingsBySector(sector: String): Flow<List<Holding>>
    
    @Query("SELECT * FROM holdings WHERE role = :role ORDER BY (shares * currentPrice) DESC")
    fun getHoldingsByRole(role: HoldingRole): Flow<List<Holding>>
    
    @Query("SELECT * FROM holdings WHERE status = :status ORDER BY (shares * currentPrice) DESC")
    fun getHoldingsByStatus(status: HoldingStatus): Flow<List<Holding>>
    
    @Query("SELECT SUM(shares * currentPrice) FROM holdings")
    fun getTotalPortfolioValue(): Flow<Double?>
    
    @Query("SELECT SUM(shares * avgCost) FROM holdings")
    fun getTotalPortfolioCost(): Flow<Double?>
    
    @Query("SELECT COUNT(*) FROM holdings")
    fun getHoldingsCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM holdings WHERE (currentPrice - avgCost) >= 0")
    fun getProfitableCount(): Flow<Int>
    
    @Query("SELECT * FROM holdings ORDER BY ((currentPrice - avgCost) / avgCost) DESC LIMIT 1")
    fun getTopGainer(): Flow<Holding?>
    
    @Query("SELECT * FROM holdings ORDER BY ((currentPrice - avgCost) / avgCost) ASC LIMIT 1")
    fun getTopLoser(): Flow<Holding?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldings(holdings: List<Holding>)
    
    @Update
    suspend fun updateHolding(holding: Holding)
    
    @Delete
    suspend fun deleteHolding(holding: Holding)
    
    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: String)
    
    @Query("DELETE FROM holdings")
    suspend fun deleteAllHoldings()
    
    @Query("UPDATE holdings SET currentPrice = :price, updatedAt = :timestamp WHERE stockSymbol = :symbol")
    suspend fun updateCurrentPrice(symbol: String, price: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE holdings SET stockNameEn = :nameEn, stockNameAr = :nameAr, sector = :sector, updatedAt = :timestamp WHERE stockSymbol = :symbol")
    suspend fun updateStockName(symbol: String, nameEn: String, nameAr: String, sector: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE holdings SET targetPercentage = :targetPercentage, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTargetPercentage(id: String, targetPercentage: Double?, timestamp: Long = System.currentTimeMillis())
}
