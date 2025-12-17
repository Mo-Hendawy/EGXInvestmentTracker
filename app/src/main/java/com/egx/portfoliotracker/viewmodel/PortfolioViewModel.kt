package com.egx.portfoliotracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egx.portfoliotracker.data.model.*
import com.egx.portfoliotracker.data.remote.StockPriceResult
import com.egx.portfoliotracker.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: PortfolioRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
    // Privacy blur state
    private val _isAmountsBlurred = MutableStateFlow(false)
    val isAmountsBlurred: StateFlow<Boolean> = _isAmountsBlurred.asStateFlow()
    
    fun toggleAmountsBlur() {
        _isAmountsBlurred.value = !_isAmountsBlurred.value
    }
    
    // Holdings
    val holdings: StateFlow<List<Holding>> = repository.getAllHoldings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Portfolio Summary
    val portfolioSummary: StateFlow<PortfolioSummary?> = repository.getPortfolioSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // Stock Allocation (for donut chart)
    val stockAllocation: StateFlow<List<Pair<String, Double>>> = repository.getStockAllocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Sector Performance
    val sectorPerformance: StateFlow<List<SectorPerformance>> = repository.getSectorPerformance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Dividends
    val allDividends: StateFlow<List<Dividend>> = repository.getAllDividends()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Performance breakdown
    private val _performanceBreakdown = MutableStateFlow<List<PerformanceBreakdown>>(emptyList())
    val performanceBreakdown: StateFlow<List<PerformanceBreakdown>> = _performanceBreakdown.asStateFlow()
    
    // Period performance
    private val _periodPerformances = MutableStateFlow<Map<TimePeriod, PeriodPerformance>>(emptyMap())
    val periodPerformances: StateFlow<Map<TimePeriod, PeriodPerformance>> = _periodPerformances.asStateFlow()
    
    // Stocks for selection
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val availableStocks: StateFlow<List<Stock>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllStocks()
            } else {
                repository.searchStocks(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Transactions
    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Price fetching state
    private val _priceState = MutableStateFlow<PriceState>(PriceState.Idle)
    val priceState: StateFlow<PriceState> = _priceState.asStateFlow()
    
    // Current stock price being fetched
    private val _currentStockPrice = MutableStateFlow<StockPriceResult?>(null)
    val currentStockPrice: StateFlow<StockPriceResult?> = _currentStockPrice.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.initializeStocks()
        }
        
        refreshAllPrices()
        loadPerformanceData()
    }
    
    private fun loadPerformanceData() {
        viewModelScope.launch {
            // Load performance breakdown
            _performanceBreakdown.value = repository.getPerformanceBreakdown()
            
            // Load period performances
            val periods = mapOf(
                TimePeriod.WEEK to repository.getPerformanceForPeriod(TimePeriod.WEEK),
                TimePeriod.MONTH to repository.getPerformanceForPeriod(TimePeriod.MONTH),
                TimePeriod.TWO_MONTHS to repository.getPerformanceForPeriod(TimePeriod.TWO_MONTHS),
                TimePeriod.FIFTY_DAYS to repository.getPerformanceForPeriod(TimePeriod.FIFTY_DAYS)
            )
            _periodPerformances.value = periods
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    // ============ PRICE FUNCTIONS ============
    
    fun fetchStockPrice(symbol: String) {
        viewModelScope.launch {
            _currentStockPrice.value = null
            _priceState.value = PriceState.Loading
            
            val result = repository.fetchStockPrice(symbol)
            _currentStockPrice.value = result
            
            _priceState.value = when (result) {
                is StockPriceResult.Success -> PriceState.Success
                is StockPriceResult.Error -> PriceState.Error(result.message)
            }
        }
    }
    
    fun refreshAllPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            try {
                val count = repository.refreshAllPrices()
                _uiState.update { it.copy(
                    isRefreshing = false,
                    lastRefreshMessage = "Updated $count stocks"
                ) }
                
                // Reload performance data after price refresh
                loadPerformanceData()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRefreshing = false,
                    lastRefreshMessage = "Failed to refresh prices"
                ) }
            }
        }
    }
    
    fun refreshPrice(symbol: String) {
        viewModelScope.launch {
            repository.refreshPrice(symbol)
        }
    }
    
    fun clearPriceState() {
        _currentStockPrice.value = null
        _priceState.value = PriceState.Idle
    }
    
    // ============ DIVIDEND FUNCTIONS ============
    
    fun addDividend(
        holdingId: String,
        stockSymbol: String,
        amountPerShare: Double,
        totalShares: Int,
        paymentDate: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val dividend = Dividend(
                holdingId = holdingId,
                stockSymbol = stockSymbol,
                amountPerShare = amountPerShare,
                totalShares = totalShares,
                totalAmount = amountPerShare * totalShares,
                exDividendDate = null,
                paymentDate = paymentDate,
                notes = notes
            )
            repository.addDividend(dividend)
            loadPerformanceData() // Refresh performance data
        }
    }
    
    fun deleteDividend(dividend: Dividend) {
        viewModelScope.launch {
            repository.deleteDividend(dividend)
            loadPerformanceData()
        }
    }
    
    fun getDividendsByHolding(holdingId: String): Flow<List<Dividend>> {
        return repository.getDividendsByHolding(holdingId)
    }
    
    fun getDividendsBySymbol(symbol: String): Flow<List<Dividend>> {
        return repository.getDividendsBySymbol(symbol)
    }
    
    // ============ HOLDING FUNCTIONS ============
    
    fun addHolding(
        stock: Stock,
        shares: Int,
        avgCost: Double,
        currentPrice: Double,
        role: HoldingRole,
        status: HoldingStatus,
        notes: String
    ) {
        viewModelScope.launch {
            val holding = Holding(
                stockSymbol = stock.symbol,
                stockNameEn = stock.nameEn,
                stockNameAr = stock.nameAr,
                shares = shares,
                avgCost = avgCost,
                currentPrice = currentPrice,
                role = role,
                status = status,
                sector = stock.sector,
                notes = notes
            )
            repository.addHolding(holding)
            _uiState.update { it.copy(showAddSuccess = true) }
        }
    }
    
    fun updateHolding(holding: Holding) {
        viewModelScope.launch {
            repository.updateHolding(holding)
        }
    }
    
    fun updateHoldingPrice(symbol: String, newPrice: Double) {
        viewModelScope.launch {
            repository.updateCurrentPrice(symbol, newPrice)
        }
    }
    
    fun deleteHolding(holding: Holding) {
        viewModelScope.launch {
            repository.deleteHolding(holding)
        }
    }
    
    fun dismissAddSuccess() {
        _uiState.update { it.copy(showAddSuccess = false) }
    }
    
    fun clearRefreshMessage() {
        _uiState.update { it.copy(lastRefreshMessage = null) }
    }
    
    // Cost History functions
    fun getCostHistory(holdingId: String): Flow<List<CostHistory>> {
        return repository.getCostHistory(holdingId)
    }
    
    fun getCostHistoryAsc(holdingId: String): Flow<List<CostHistory>> {
        return repository.getCostHistoryAsc(holdingId)
    }
    
    fun getTransactionsByHolding(holdingId: String): Flow<List<Transaction>> {
        return repository.getTransactionsByHolding(holdingId)
    }
    
    // Buy more shares
    fun buyMoreShares(
        holding: Holding,
        shares: Int,
        price: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addSharesToHolding(holding, shares, price, notes)
        }
    }
    
    // Sell shares
    fun sellShares(
        holding: Holding,
        shares: Int,
        price: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.sellSharesFromHolding(holding, shares, price, notes)
        }
    }
    
    // Adjust average cost manually
    fun adjustAverageCost(
        holding: Holding,
        newAvgCost: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.adjustAverageCost(holding, newAvgCost, notes)
        }
    }
    
    // Analytics helpers
    fun getHoldingsByRole(role: HoldingRole): Flow<List<Holding>> {
        return repository.getHoldingsByRole(role)
    }
    
    fun getHoldingsByStatus(status: HoldingStatus): Flow<List<Holding>> {
        return repository.getHoldingsByStatus(status)
    }
    
    fun getHoldingsBySector(sector: String): Flow<List<Holding>> {
        return repository.getHoldingsBySector(sector)
    }
}

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val showAddSuccess: Boolean = false,
    val lastRefreshMessage: String? = null,
    val error: String? = null
)

sealed class PriceState {
    object Idle : PriceState()
    object Loading : PriceState()
    object Success : PriceState()
    data class Error(val message: String) : PriceState()
}
