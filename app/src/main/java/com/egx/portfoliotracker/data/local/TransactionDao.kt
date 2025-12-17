package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Transaction
import com.egx.portfoliotracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE holdingId = :holdingId ORDER BY timestamp DESC")
    fun getTransactionsByHolding(holdingId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE stockSymbol = :symbol ORDER BY timestamp DESC")
    fun getTransactionsBySymbol(symbol: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<Transaction>>
    
    @Query("SELECT SUM(total) FROM transactions WHERE type = :type")
    fun getTotalByType(type: TransactionType): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE holdingId = :holdingId")
    suspend fun deleteTransactionsByHolding(holdingId: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
