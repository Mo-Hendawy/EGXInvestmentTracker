package com.egx.portfoliotracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egx.portfoliotracker.data.model.*
import com.egx.portfoliotracker.data.model.CertificateIncomeDetail
import com.egx.portfoliotracker.data.model.MonthlyCertificateIncome
import com.egx.portfoliotracker.data.remote.StockPriceResult
import com.egx.portfoliotracker.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: PortfolioRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
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
    
    // Certificates
    val certificates: StateFlow<List<Certificate>> = repository.getAllCertificates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val totalCertificatesValue: StateFlow<Double?> = repository.getTotalCertificatesValue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val totalMonthlyIncome: StateFlow<Double?> = repository.getTotalMonthlyIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // Amount blur state
    private val _isAmountsBlurred = MutableStateFlow(false)
    val isAmountsBlurred: StateFlow<Boolean> = _isAmountsBlurred.asStateFlow()
    
    fun toggleAmountsBlur() {
        _isAmountsBlurred.value = !_isAmountsBlurred.value
    }
    
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
    
    // ========== REALIZED GAINS ==========
    private val _realizedGains = MutableStateFlow<List<com.egx.portfoliotracker.data.model.RealizedGain>>(emptyList())
    val realizedGains: StateFlow<List<com.egx.portfoliotracker.data.model.RealizedGain>> = _realizedGains.asStateFlow()
    
    // ========== STOCK ANALYSIS ==========
    private val _stockAnalyses = MutableStateFlow<List<StockAnalysis>>(emptyList())
    val stockAnalyses: StateFlow<List<StockAnalysis>> = _stockAnalyses.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Attempt data recovery first
            repository.attemptDataRecovery()
            repository.initializeStocks()
        }
        
        refreshAllPrices()
        loadPerformanceData()
        refreshRealizedGains()
        refreshStockAnalyses()
    }
    
    fun attemptDataRecovery() {
        viewModelScope.launch {
            val recovered = repository.attemptDataRecovery()
            val counts = repository.getDatabaseCounts()
            _uiState.update { 
                it.copy(
                    lastRefreshMessage = if (recovered) {
                        "Data recovered! Holdings: ${counts["holdings"]}, Certificates: ${counts["certificates"]}, Expenses: ${counts["expenses"]}"
                    } else {
                        "No data found. Counts - Holdings: ${counts["holdings"]}, Certificates: ${counts["certificates"]}, Expenses: ${counts["expenses"]}"
                    }
                )
            }
            if (recovered) {
                // Reload all data after recovery
                loadPerformanceData()
            }
        }
    }
    
    suspend fun getDatabaseCounts(): Map<String, Int> {
        return repository.getDatabaseCounts()
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
    
    // Update target percentage for a holding
    fun updateHoldingTargetPercentage(holdingId: String, targetPercentage: Double?) {
        viewModelScope.launch {
            repository.updateHoldingTargetPercentage(holdingId, targetPercentage)
        }
    }
    
    // ========== WATCHLIST ==========
    
    val watchlistItems: StateFlow<List<Watchlist>> = repository.getAllWatchlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun addWatchlistItem(watchlist: Watchlist) {
        viewModelScope.launch {
            repository.addWatchlistItem(watchlist)
        }
    }
    
    fun updateWatchlistItem(watchlist: Watchlist) {
        viewModelScope.launch {
            repository.updateWatchlistItem(watchlist)
        }
    }
    
    fun deleteWatchlistItem(watchlist: Watchlist) {
        viewModelScope.launch {
            repository.deleteWatchlistItem(watchlist)
        }
    }
    
    // ========== BACKUP/RESTORE ==========
    
    fun exportData(uri: android.net.Uri, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.exportData(uri, callback)
        }
    }
    
    fun importData(uri: android.net.Uri, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.importData(uri, callback)
        }
    }
    
    fun refreshRealizedGains() {
        viewModelScope.launch {
            _realizedGains.value = repository.getRealizedGains()
        }
    }
    
    fun refreshStockAnalyses() {
        viewModelScope.launch {
            try {
                val analyses = repository.getStockAnalyses()
                _stockAnalyses.value = analyses
            } catch (e: Exception) {
                // Handle error - set empty list on failure
                _stockAnalyses.value = emptyList()
            }
        }
    }
    
    // ========== PORTFOLIO SNAPSHOTS ==========
    
    val portfolioSnapshots: StateFlow<List<PortfolioSnapshot>> = repository.getPortfolioSnapshots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // ========== GET ALL STOCKS ==========
    
    fun getAllStocks(): Flow<List<Stock>> = repository.getAllStocks()
    
    // Certificate operations
    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.addCertificate(certificate)
        }
    }
    
    fun updateCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.updateCertificate(certificate)
        }
    }
    
    fun deleteCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.deleteCertificate(certificate)
        }
    }
    
    suspend fun getMonthlyCertificateIncome(year: Int, month: Int): MonthlyCertificateIncome {
        return repository.getMonthlyCertificateIncome(year, month)
    }
    
    fun initializeCertificatesFromNotebook() {
        viewModelScope.launch {
            val certificates = listOf(
                // 2024 Certificates
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "235",
                    principalAmount = 300000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 7, 17),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "236",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 7, 24),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "237",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 8, 4),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "238",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 8, 29),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "239",
                    principalAmount = 80000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 9, 3),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "900",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 9, 26),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "901",
                    principalAmount = 80000.0,
                    durationYears = 3,
                    annualInterestRate = 22.0,
                    purchaseDate = getDateTimestamp(2024, 10, 7),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "902",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2024, 10, 30),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "903",
                    principalAmount = 80000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2024, 11, 5),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "904",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2024, 12, 5),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "905",
                    principalAmount = 120000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2024, 12, 16),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "906",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2024, 12, 26),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                
                // 2025 Certificates
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "907",
                    principalAmount = 100000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 1, 8),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "908",
                    principalAmount = 70000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 1, 27),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "909",
                    principalAmount = 100000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 2, 5),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "910",
                    principalAmount = 60000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 2, 27),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "911",
                    principalAmount = 450000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 3, 9),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "912",
                    principalAmount = 120000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 3, 27),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "913",
                    principalAmount = 210000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 7, 6),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                ),
                Certificate(
                    bankName = "HSBC",
                    certificateNumber = "914",
                    principalAmount = 80000.0,
                    durationYears = 3,
                    annualInterestRate = 20.5,
                    purchaseDate = getDateTimestamp(2025, 9, 15),
                    interestPaymentFrequency = InterestFrequency.MONTHLY,
                    status = CertificateStatus.ACTIVE,
                    notes = ""
                )
            )
            
            // Import all certificates
            certificates.forEach { cert ->
                repository.addCertificate(cert)
            }
        }
    }
    
    /**
     * Initialize holdings from CSV/notebook data
     * Data extracted from transcript:
     * ABUK: 1665 shares @ 47.28 avg, current: 47
     * AMOC: 5898 shares @ 6.87 avg, current: 6.94
     * COMI: 1179 shares @ 101.42 avg, current: 104
     * EFID: 688 shares @ 18.26 avg, current: 26.09
     * EGAL: 735 shares @ 188.83 avg, current: 204.23
     * JUFO: 6313 shares @ 22.13 avg, current: 23.74
     * MFPC: 4431 shares @ 29.15 avg, current: 28.91
     * MICH: 10274 shares @ 33.15 avg, current: 28.72
     * POUL: 1086 shares @ 24.78 avg, current: 26.4
     * SWDY: 1866 shares @ 77.49 avg, current: 76.16
     */
    fun initializeHoldingsFromNotebook() {
        viewModelScope.launch {
            try {
                // CRITICAL: Initialize stocks FIRST to ensure correct names are in database
                repository.initializeStocks()
                
                // Get all stocks from database (now with correct names)
                val stocks = repository.getAllStocks().first()
                val stockMap = stocks.associateBy { it.symbol }
                
                // Portfolio data from CSV/notebook
                val portfolioData = listOf(
                    Pair("ABUK", Triple(1665, 47.28, 47.0)),
                    Pair("AMOC", Triple(5898, 6.87, 6.94)),
                    Pair("COMI", Triple(1179, 101.42, 104.0)),
                    Pair("EFID", Triple(688, 18.26, 26.09)),
                    Pair("EGAL", Triple(735, 188.83, 204.23)),
                    Pair("JUFO", Triple(6313, 22.13, 23.74)),
                    Pair("MFPC", Triple(4431, 29.15, 28.91)),
                    Pair("MICH", Triple(10274, 33.15, 28.72)),
                    Pair("POUL", Triple(1086, 24.78, 26.4)),
                    Pair("SWDY", Triple(1866, 77.49, 76.16))
                )
                
                // Create holdings
                for ((symbol, data) in portfolioData) {
                    val (shares, avgCost, currentPrice) = data
                    val stock = stockMap[symbol]
                    if (stock != null) {
                        val holding = Holding(
                            stockSymbol = stock.symbol,
                            stockNameEn = stock.nameEn,
                            stockNameAr = stock.nameAr,
                            shares = shares,
                            avgCost = avgCost,
                            currentPrice = currentPrice,
                            role = HoldingRole.CORE,
                            status = HoldingStatus.HOLD,
                            sector = stock.sector,
                            notes = "Initialized from notebook"
                        )
                        repository.addHolding(holding)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Helper function to create a date timestamp
     */
    private fun getDateTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month - 1) // Calendar months are 0-based
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

data class DailyCredit(
    val day: Int,
    val totalAmount: Double,
    val certificates: List<CertificateIncomeDetail>
)

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
