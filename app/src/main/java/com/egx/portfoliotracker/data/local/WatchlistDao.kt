package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Watchlist
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY createdAt DESC")
    fun getAllWatchlistItems(): Flow<List<Watchlist>>
    
    @Query("SELECT * FROM watchlist WHERE id = :id")
    suspend fun getWatchlistItemById(id: String): Watchlist?
    
    @Query("SELECT * FROM watchlist WHERE stockSymbol = :symbol")
    suspend fun getWatchlistItemBySymbol(symbol: String): Watchlist?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(watchlist: Watchlist)
    
    @Update
    suspend fun updateWatchlistItem(watchlist: Watchlist)
    
    @Delete
    suspend fun deleteWatchlistItem(watchlist: Watchlist)
    
    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteWatchlistItemById(id: String)
}

