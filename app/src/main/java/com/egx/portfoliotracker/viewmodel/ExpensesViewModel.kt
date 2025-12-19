package com.egx.portfoliotracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egx.portfoliotracker.data.model.Expense
import com.egx.portfoliotracker.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: PortfolioRepository
) : ViewModel() {
    
    val allExpenses: StateFlow<List<Expense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allCategories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _selectedPeriod = MutableStateFlow(ExpensePeriod.MONTHLY)
    val selectedPeriod: StateFlow<ExpensePeriod> = _selectedPeriod.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // Custom date filtering
    private val _selectedMonth = MutableStateFlow<Pair<Int, Int>?>(null) // Year, Month (1-12)
    val selectedMonth: StateFlow<Pair<Int, Int>?> = _selectedMonth.asStateFlow()
    
    private val _selectedDay = MutableStateFlow<Long?>(null) // Timestamp for the day
    val selectedDay: StateFlow<Long?> = _selectedDay.asStateFlow()
    
    fun setPeriod(period: ExpensePeriod) {
        _selectedPeriod.value = period
        // Clear custom date filters when changing period
        if (period != ExpensePeriod.CUSTOM_MONTH && period != ExpensePeriod.CUSTOM_DAY) {
            _selectedMonth.value = null
            _selectedDay.value = null
        }
    }
    
    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun setSelectedMonth(year: Int, month: Int) {
        _selectedMonth.value = Pair(year, month)
        _selectedPeriod.value = ExpensePeriod.CUSTOM_MONTH
    }
    
    fun setSelectedDay(timestamp: Long) {
        _selectedDay.value = timestamp
        _selectedPeriod.value = ExpensePeriod.CUSTOM_DAY
    }
    
    fun clearCustomDateFilter() {
        _selectedMonth.value = null
        _selectedDay.value = null
        _selectedPeriod.value = ExpensePeriod.MONTHLY
    }
    
    // Get date range for current period
    fun getDateRangeForPeriod(period: ExpensePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        when (period) {
            ExpensePeriod.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ExpensePeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ExpensePeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ExpensePeriod.YEARLY -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ExpensePeriod.CUSTOM_MONTH -> {
                val month = _selectedMonth.value
                if (month != null) {
                    calendar.set(Calendar.YEAR, month.first)
                    calendar.set(Calendar.MONTH, month.second - 1) // Month is 0-based
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startDate = calendar.timeInMillis
                    // End of month
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    return Pair(startDate, calendar.timeInMillis)
                }
            }
            ExpensePeriod.CUSTOM_DAY -> {
                val day = _selectedDay.value
                if (day != null) {
                    calendar.timeInMillis = day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startDate = calendar.timeInMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    return Pair(startDate, calendar.timeInMillis)
                }
            }
            ExpensePeriod.ALL_TIME -> {
                return Pair(0L, endDate)
            }
        }
        
        val startDate = calendar.timeInMillis
        return Pair(startDate, endDate)
    }
    
    // Get filtered expenses
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        selectedPeriod,
        selectedCategory,
        selectedMonth,
        selectedDay
    ) { expenses, period, category, month, day ->
        val (startDate, endDate) = getDateRangeForPeriod(period)
        expenses.filter { expense ->
            expense.date >= startDate && expense.date <= endDate &&
            (category == null || expense.category == category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Get total expenses for current period
    val totalExpenses: StateFlow<Double> = filteredExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // Get category totals for current period
    val categoryTotals: StateFlow<List<CategoryTotal>> = combine(
        filteredExpenses,
        selectedPeriod
    ) { expenses, _ ->
        expenses.groupBy { it.category }
            .map { (category, expenseList) ->
                CategoryTotal(category, expenseList.sumOf { it.amount })
            }
            .sortedByDescending { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    suspend fun getCategoryTotalsForPeriod(period: ExpensePeriod): List<com.egx.portfoliotracker.data.local.CategoryTotal> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return repository.getCategoryTotalsByDateRange(startDate, endDate)
    }
    
    suspend fun getDailyExpensesForPeriod(period: ExpensePeriod): List<com.egx.portfoliotracker.data.local.DailyExpense> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return repository.getDailyExpensesByDateRange(startDate, endDate)
    }
    
    suspend fun getMonthlyExpensesForPeriod(period: ExpensePeriod): List<com.egx.portfoliotracker.data.local.MonthlyExpense> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return repository.getMonthlyExpensesByDateRange(startDate, endDate)
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }
    
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
    
    suspend fun getExpenseById(id: String): Expense? {
        return repository.getExpenseById(id)
    }
}

enum class ExpensePeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CUSTOM_MONTH("Select Month"),
    CUSTOM_DAY("Select Day"),
    ALL_TIME("All Time")
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
