package com.egx.portfoliotracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egx.portfoliotracker.data.model.*
import com.egx.portfoliotracker.data.remote.StockAnalysisService
import com.egx.portfoliotracker.data.remote.StockPriceResult
import com.egx.portfoliotracker.data.remote.StockPriceService
import com.egx.portfoliotracker.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: PortfolioRepository,
    private val stockAnalysisService: StockAnalysisService,
    private val stockPriceService: StockPriceService
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
    
    // Certificates
    val certificates: StateFlow<List<Certificate>> = repository.getAllCertificates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val totalCertificatesValue: StateFlow<Double?> = repository.getTotalCertificatesValue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val totalMonthlyIncome: StateFlow<Double?> = repository.getTotalMonthlyIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
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
            // Update any CIB certificates to HSBC
            repository.updateAllCertificatesBankName("Commercial International Bank (CIB)", "HSBC")
            // Always sync stock names FIRST to fix any wrong names
            repository.syncStockNames()
            // Restore portfolio data
            restorePortfolioData()
            // Sync again after restore to ensure correct names
            repository.syncStockNames()
        }
        
        refreshAllPrices()
        loadPerformanceData()
    }
    
    /**
     * Restore portfolio data from backup
     */
    private suspend fun restorePortfolioData() {
        // Check if holdings already exist
        val existingHoldings = repository.getAllHoldings().first()
        if (existingHoldings.isNotEmpty()) {
            return // Data already exists, don't restore
        }
        
        // Wait for stocks to be initialized
        repository.initializeStocks()
        
        // Get all stocks to match names
        val stocks = repository.getAllStocks().first()
        val stockMap = stocks.associateBy { it.symbol }
        
        // Portfolio data from backup
        val portfolioData = listOf(
            "ABUK" to Pair(1665, 47.28),
            "AMOC" to Pair(5898, 6.87),
            "BONY" to Pair(5592, 4.25),
            "COMI" to Pair(1345, 101.62),
            "EFID" to Pair(688, 18.26),
            "EGAL" to Pair(735, 188.83),
            "JUFO" to Pair(6313, 22.13),
            "MFPC" to Pair(4431, 29.15),
            "MICH" to Pair(10274, 33.15),
            "POUL" to Pair(566, 24.78),
            "SWDY" to Pair(1866, 77.49)
        )
        
        // Create holdings
        for ((symbol, data) in portfolioData) {
            val shares = data.first
            val avgCost = data.second
            val stock = stockMap[symbol]
            
            if (stock != null) {
                val holding = Holding(
                    id = java.util.UUID.randomUUID().toString(),
                    stockSymbol = symbol,
                    stockNameEn = stock.nameEn,
                    stockNameAr = stock.nameAr,
                    shares = shares,
                    avgCost = avgCost,
                    currentPrice = avgCost, // Will be updated by price refresh
                    sector = stock.sector,
                    notes = "",
                    targetPercentage = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.addHolding(holding)
            }
        }
        
        // Sync stock names after adding holdings to ensure correct names
        repository.syncStockNames()
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
    
    fun updateHoldingTargetPercentage(holdingId: String, targetPercentage: Double?) {
        viewModelScope.launch {
            repository.updateHoldingTargetPercentage(holdingId, targetPercentage)
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
    
    /**
     * Get stock analysis (fair value and buy zones) for a holding
     * Uses existing price data, no extra API calls
     */
    suspend fun getStockAnalysis(holding: Holding): com.egx.portfoliotracker.data.model.StockAnalysis? {
        return try {
            // Fetch current price data (we already have this cached)
            val priceResult = stockPriceService.getPrice(holding.stockSymbol)
            
            if (priceResult is StockPriceResult.Success) {
                stockAnalysisService.analyzeStock(
                    symbol = holding.stockSymbol,
                    currentPrice = priceResult.price,
                    high = priceResult.high,
                    low = priceResult.low,
                    open = priceResult.open,
                    previousClose = priceResult.previousClose,
                    avgCost = holding.avgCost
                )
            } else {
                // If price fetch fails, still try to analyze with available data
                stockAnalysisService.analyzeStock(
                    symbol = holding.stockSymbol,
                    currentPrice = holding.currentPrice,
                    high = null,
                    low = null,
                    open = null,
                    previousClose = null,
                    avgCost = holding.avgCost
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("PortfolioViewModel", "Error analyzing stock ${holding.stockSymbol}", e)
            null
        }
    }
    
    // ============ CERTIFICATES ============
    
    suspend fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.addCertificate(certificate)
        }
    }
    
    suspend fun updateCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.updateCertificate(certificate)
        }
    }
    
    suspend fun deleteCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.deleteCertificate(certificate)
        }
    }
    
    suspend fun getMonthlyCertificateIncome(year: Int, month: Int): MonthlyCertificateIncome {
        return repository.getMonthlyCertificateIncome(year, month)
    }
    
    suspend fun getMonthlyCertificateIncomeRange(startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): List<MonthlyCertificateIncome> {
        return repository.getMonthlyCertificateIncomeRange(startYear, startMonth, endYear, endMonth)
    }
    
    suspend fun getUpcomingMaturities(limit: Int = 10): List<Certificate> {
        return repository.getUpcomingMaturities(limit)
    }
    
    /**
     * Get daily credit schedule for current month
     * Shows which days have credits and how much
     */
    suspend fun getDailyCreditScheduleForCurrentMonth(): List<DailyCredit> {
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        
        val allCertificates = repository.getAllCertificates().first()
        val dailyCredits = mutableMapOf<Int, MutableList<CertificateIncomeDetail>>()
        
        allCertificates.forEach { cert ->
            val income = cert.getMonthlyIncomeForMonth(currentYear, currentMonth)
            if (income > 0 && cert.interestPaymentFrequency == InterestFrequency.MONTHLY) {
                val paymentDay = cert.getPaymentDayOfMonth()
                val dueDate = cert.getInterestDueDateForMonth(currentYear, currentMonth)
                
                val detail = CertificateIncomeDetail(
                    certificateId = cert.id,
                    certificateNumber = cert.certificateNumber,
                    bankName = cert.bankName,
                    amount = income,
                    dueDate = dueDate ?: 0L
                )
                
                dailyCredits.getOrPut(paymentDay) { mutableListOf() }.add(detail)
            }
        }
        
        return dailyCredits.map { (day, details) ->
            DailyCredit(
                day = day,
                totalAmount = details.sumOf { it.amount },
                certificates = details
            )
        }.sortedBy { it.day }
    }
    
    /**
     * Bulk import certificates - useful for importing from statements
     */
    suspend fun importCertificates(certificates: List<Certificate>) {
        viewModelScope.launch {
            certificates.forEach { cert ->
                repository.addCertificate(cert)
            }
        }
    }
    
    /**
     * Initialize all 20 certificates from notebook
     * All certificates: 3 years duration, Monthly interest payment
     * All certificates are from HSBC
     */
    suspend fun initializeCertificatesFromNotebook() {
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
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 7, 24),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = "236",
                notes = ""
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 8, 4),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 237"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 8, 29),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 238"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 80000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 9, 3),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 239"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 9, 26),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 900"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 80000.0,
                durationYears = 3,
                annualInterestRate = 22.0,
                purchaseDate = getDateTimestamp(2024, 10, 7),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 901"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2024, 10, 30),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 902"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 80000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2024, 11, 5),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 903"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2024, 12, 5),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 904"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 120000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2024, 12, 16),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 905"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2024, 12, 26),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 906"
            ),
            
            // 2025 Certificates
            Certificate(
                bankName = "HSBC",
                principalAmount = 100000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 1, 8),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 907"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 70000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 1, 27),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 908"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 100000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 2, 5),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 909"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 60000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 2, 27),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 910"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 450000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 3, 9),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 911"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 120000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 3, 27),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 912"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 210000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 7, 6),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 913"
            ),
            Certificate(
                bankName = "HSBC",
                principalAmount = 80000.0,
                durationYears = 3,
                annualInterestRate = 20.5,
                purchaseDate = getDateTimestamp(2025, 9, 15),
                interestPaymentFrequency = InterestFrequency.MONTHLY,
                status = CertificateStatus.ACTIVE,
                certificateNumber = " 914"
            )
        )
        
        importCertificates(certificates)
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

/**
 * Daily credit information for a specific day in the month
 */
data class DailyCredit(
    val day: Int,
    val totalAmount: Double,
    val certificates: List<CertificateIncomeDetail>
)
