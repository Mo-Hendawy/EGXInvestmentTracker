package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Stock
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    
    @Query("SELECT * FROM stocks WHERE isActive = 1 ORDER BY symbol ASC")
    fun getAllActiveStocks(): Flow<List<Stock>>
    
    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<Stock>>
    
    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): Stock?
    
    @Query("SELECT * FROM stocks WHERE sector = :sector AND isActive = 1 ORDER BY symbol ASC")
    fun getStocksBySector(sector: String): Flow<List<Stock>>
    
    @Query("SELECT DISTINCT sector FROM stocks WHERE isActive = 1 ORDER BY sector ASC")
    fun getAllSectors(): Flow<List<String>>
    
    @Query("SELECT * FROM stocks WHERE (symbol LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' OR nameAr LIKE '%' || :query || '%') AND isActive = 1 ORDER BY symbol ASC")
    fun searchStocks(query: String): Flow<List<Stock>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<Stock>)
    
    @Update
    suspend fun updateStock(stock: Stock)
    
    @Delete
    suspend fun deleteStock(stock: Stock)
    
    @Query("DELETE FROM stocks")
    suspend fun deleteAllStocks()
}
