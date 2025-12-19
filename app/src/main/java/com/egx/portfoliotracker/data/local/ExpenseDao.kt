package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): Expense?
    
    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE category = :category AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesByCategoryAndDateRange(category: String, startDate: Long, endDate: Long): Flow<List<Expense>>
    
    @Query("SELECT DISTINCT category FROM expenses ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY category")
    suspend fun getCategoryTotalsByDateRange(startDate: Long, endDate: Long): List<CategoryTotal>
    
    @Query("SELECT date, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date")
    suspend fun getDailyExpensesByDateRange(startDate: Long, endDate: Long): List<DailyExpense>
    
    @Query("SELECT strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as month, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY month ORDER BY month")
    suspend fun getMonthlyExpensesByDateRange(startDate: Long, endDate: Long): List<MonthlyExpense>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    
    @Update
    suspend fun updateExpense(expense: Expense)
    
    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class DailyExpense(
    val date: Long,
    val total: Double
)

data class MonthlyExpense(
    val month: String, // Format: "YYYY-MM"
    val total: Double
)
