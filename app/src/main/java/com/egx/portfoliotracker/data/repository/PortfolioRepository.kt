package com.egx.portfoliotracker.data.repository

import com.egx.portfoliotracker.data.local.CertificateDao
import com.egx.portfoliotracker.data.local.CostHistoryDao
import com.egx.portfoliotracker.data.local.DividendDao
import com.egx.portfoliotracker.data.local.ExpenseDao
import com.egx.portfoliotracker.data.local.HoldingDao
import com.egx.portfoliotracker.data.local.StockDao
import com.egx.portfoliotracker.data.local.TransactionDao
import com.egx.portfoliotracker.data.model.*
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateStatus
import com.egx.portfoliotracker.data.model.CertificateIncomeDetail
import com.egx.portfoliotracker.data.model.MonthlyCertificateIncome
import com.egx.portfoliotracker.data.remote.StockPriceResult
import com.egx.portfoliotracker.data.remote.StockPriceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepository @Inject constructor(
    private val holdingDao: HoldingDao,
    private val stockDao: StockDao,
    private val transactionDao: TransactionDao,
    private val costHistoryDao: CostHistoryDao,
    private val dividendDao: DividendDao,
    private val certificateDao: CertificateDao,
    private val expenseDao: ExpenseDao,
    private val stockPriceService: StockPriceService
) {
    // Holdings
    fun getAllHoldings(): Flow<List<Holding>> = holdingDao.getAllHoldings()
    
    suspend fun getHoldingById(id: String): Holding? = holdingDao.getHoldingById(id)
    
    suspend fun getHoldingBySymbol(symbol: String): Holding? = holdingDao.getHoldingBySymbol(symbol)
    
    fun getHoldingsBySector(sector: String): Flow<List<Holding>> = holdingDao.getHoldingsBySector(sector)
    
    fun getHoldingsByRole(role: HoldingRole): Flow<List<Holding>> = holdingDao.getHoldingsByRole(role)
    
    fun getHoldingsByStatus(status: HoldingStatus): Flow<List<Holding>> = holdingDao.getHoldingsByStatus(status)
    
    suspend fun addHolding(holding: Holding) {
        holdingDao.insertHolding(holding)
        // Record the buy transaction
        transactionDao.insertTransaction(
            Transaction(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                type = TransactionType.BUY,
                shares = holding.shares,
                price = holding.avgCost,
                total = holding.totalCost,
                notes = "Initial purchase"
            )
        )
        // Record initial cost history
        costHistoryDao.insertHistory(
            CostHistory(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                previousAvgCost = 0.0,
                newAvgCost = holding.avgCost,
                previousShares = 0,
                newShares = holding.shares,
                changeType = CostChangeType.BUY,
                transactionPrice = holding.avgCost,
                transactionShares = holding.shares,
                notes = "Initial purchase"
            )
        )
    }
    
    suspend fun updateHolding(holding: Holding) {
        holdingDao.updateHolding(holding.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun updateHoldingTargetPercentage(holdingId: String, targetPercentage: Double?) {
        holdingDao.updateTargetPercentage(holdingId, targetPercentage, System.currentTimeMillis())
    }
    
    suspend fun deleteHolding(holding: Holding) {
        holdingDao.deleteHolding(holding)
        transactionDao.deleteTransactionsByHolding(holding.id)
        costHistoryDao.deleteHistoryByHolding(holding.id)
        dividendDao.deleteDividendsByHolding(holding.id)
    }
    
    suspend fun updateCurrentPrice(symbol: String, price: Double) {
        holdingDao.updateCurrentPrice(symbol, price)
    }
    
    // ============ PRICE API FUNCTIONS ============
    
    suspend fun fetchStockPrice(symbol: String): StockPriceResult {
        return stockPriceService.getPrice(symbol)
    }
    
    suspend fun refreshAllPrices(): Int {
        val holdings = holdingDao.getAllHoldings().first()
        var updatedCount = 0
        
        for (holding in holdings) {
            val result = stockPriceService.getPrice(holding.stockSymbol)
            if (result is StockPriceResult.Success) {
                holdingDao.updateCurrentPrice(holding.stockSymbol, result.price)
                updatedCount++
            }
        }
        
        // Take a snapshot after refreshing prices
        savePortfolioSnapshot()
        
        return updatedCount
    }
    
    suspend fun refreshPrice(symbol: String): StockPriceResult {
        val result = stockPriceService.getPrice(symbol)
        if (result is StockPriceResult.Success) {
            holdingDao.updateCurrentPrice(symbol, result.price)
        }
        return result
    }
    
    // ============ DIVIDEND FUNCTIONS ============
    
    suspend fun addDividend(dividend: Dividend) {
        dividendDao.insertDividend(dividend)
    }
    
    suspend fun updateDividend(dividend: Dividend) {
        dividendDao.updateDividend(dividend)
    }
    
    suspend fun deleteDividend(dividend: Dividend) {
        dividendDao.deleteDividend(dividend)
    }
    
    fun getAllDividends(): Flow<List<Dividend>> = dividendDao.getAllDividends()
    
    fun getDividendsByHolding(holdingId: String): Flow<List<Dividend>> = 
        dividendDao.getDividendsByHolding(holdingId)
    
    fun getDividendsBySymbol(symbol: String): Flow<List<Dividend>> = 
        dividendDao.getDividendsBySymbol(symbol)
    
    suspend fun getTotalDividends(): Double = dividendDao.getTotalDividends() ?: 0.0
    
    suspend fun getTotalDividendsForHolding(holdingId: String): Double = 
        dividendDao.getTotalDividendsForHolding(holdingId) ?: 0.0
    
    suspend fun getDividendsSince(startTime: Long): Double = 
        dividendDao.getDividendsSince(startTime) ?: 0.0
    
    // ============ PORTFOLIO SNAPSHOT FUNCTIONS ============
    
    suspend fun savePortfolioSnapshot() {
        val holdings = holdingDao.getAllHoldings().first()
        if (holdings.isEmpty()) return
        
        val totalValue = holdings.sumOf { it.marketValue }
        val totalCost = holdings.sumOf { it.totalCost }
        val totalDividends = getTotalDividends()
        
        val snapshot = PortfolioSnapshot(
            totalValue = totalValue,
            totalCost = totalCost,
            profitLoss = totalValue - totalCost,
            profitLossPercent = if (totalCost > 0) ((totalValue - totalCost) / totalCost) * 100 else 0.0,
            totalDividends = totalDividends,
            holdingsCount = holdings.size
        )
        
        dividendDao.insertSnapshot(snapshot)
    }
    
    fun getRecentSnapshots(limit: Int = 100): Flow<List<PortfolioSnapshot>> = 
        dividendDao.getRecentSnapshots(limit)
    
    fun getSnapshotsSince(startTime: Long): Flow<List<PortfolioSnapshot>> = 
        dividendDao.getSnapshotsSince(startTime)
    
    suspend fun getSnapshotAt(time: Long): PortfolioSnapshot? = 
        dividendDao.getSnapshotAt(time)
    
    // ============ PERFORMANCE ANALYSIS ============
    
    suspend fun getPerformanceForPeriod(period: TimePeriod): PeriodPerformance {
        val now = System.currentTimeMillis()
        val startTime = now - (period.days.toLong() * 24 * 60 * 60 * 1000)
        
        val startSnapshot = dividendDao.getSnapshotAt(startTime)
        val currentHoldings = holdingDao.getAllHoldings().first()
        
        val currentValue = currentHoldings.sumOf { it.marketValue }
        val currentCost = currentHoldings.sumOf { it.totalCost }
        
        val startValue = startSnapshot?.totalValue ?: currentCost
        val dividendsReceived = getDividendsSince(startTime)
        
        val valueChange = currentValue - startValue
        val valueChangePercent = if (startValue > 0) (valueChange / startValue) * 100 else 0.0
        
        val totalReturn = valueChange + dividendsReceived
        val totalReturnPercent = if (startValue > 0) (totalReturn / startValue) * 100 else 0.0
        
        return PeriodPerformance(
            period = period,
            startValue = startValue,
            endValue = currentValue,
            valueChange = valueChange,
            valueChangePercent = valueChangePercent,
            dividendsReceived = dividendsReceived,
            totalReturn = totalReturn,
            totalReturnPercent = totalReturnPercent
        )
    }
    
    suspend fun getPerformanceBreakdown(): List<PerformanceBreakdown> {
        val holdings = holdingDao.getAllHoldings().first()
        
        return holdings.map { holding ->
            val dividends = getTotalDividendsForHolding(holding.id)
            val priceGain = holding.profitLoss
            val priceGainPercent = holding.profitLossPercent
            val totalReturn = priceGain + dividends
            val totalReturnPercent = if (holding.totalCost > 0) {
                (totalReturn / holding.totalCost) * 100
            } else 0.0
            val dividendYield = if (holding.totalCost > 0) {
                (dividends / holding.totalCost) * 100
            } else 0.0
            
            PerformanceBreakdown(
                stockSymbol = holding.stockSymbol,
                stockName = holding.stockNameEn,
                priceGain = priceGain,
                priceGainPercent = priceGainPercent,
                dividendGain = dividends,
                dividendYield = dividendYield,
                totalReturn = totalReturn,
                totalReturnPercent = totalReturnPercent,
                totalCost = holding.totalCost,
                currentValue = holding.marketValue
            )
        }.sortedByDescending { it.totalReturn }
    }
    
    // ============ TRANSACTION FUNCTIONS ============
    
    suspend fun addSharesToHolding(
        holding: Holding,
        newShares: Int,
        purchasePrice: Double,
        notes: String = ""
    ) {
        val previousAvgCost = holding.avgCost
        val previousShares = holding.shares
        
        val totalOldCost = previousShares * previousAvgCost
        val totalNewCost = newShares * purchasePrice
        val totalShares = previousShares + newShares
        val newAvgCost = (totalOldCost + totalNewCost) / totalShares
        
        val updatedHolding = holding.copy(
            shares = totalShares,
            avgCost = newAvgCost,
            updatedAt = System.currentTimeMillis()
        )
        holdingDao.updateHolding(updatedHolding)
        
        transactionDao.insertTransaction(
            Transaction(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                type = TransactionType.BUY,
                shares = newShares,
                price = purchasePrice,
                total = newShares * purchasePrice,
                notes = notes
            )
        )
        
        costHistoryDao.insertHistory(
            CostHistory(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                previousAvgCost = previousAvgCost,
                newAvgCost = newAvgCost,
                previousShares = previousShares,
                newShares = totalShares,
                changeType = CostChangeType.BUY,
                transactionPrice = purchasePrice,
                transactionShares = newShares,
                notes = notes
            )
        )
    }
    
    suspend fun sellSharesFromHolding(
        holding: Holding,
        sharesToSell: Int,
        sellPrice: Double,
        notes: String = ""
    ) {
        val previousShares = holding.shares
        val newShares = previousShares - sharesToSell
        
        if (newShares <= 0) {
            deleteHolding(holding)
            return
        }
        
        val updatedHolding = holding.copy(
            shares = newShares,
            updatedAt = System.currentTimeMillis()
        )
        holdingDao.updateHolding(updatedHolding)
        
        transactionDao.insertTransaction(
            Transaction(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                type = TransactionType.SELL,
                shares = sharesToSell,
                price = sellPrice,
                total = sharesToSell * sellPrice,
                notes = notes
            )
        )
        
        costHistoryDao.insertHistory(
            CostHistory(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                previousAvgCost = holding.avgCost,
                newAvgCost = holding.avgCost,
                previousShares = previousShares,
                newShares = newShares,
                changeType = CostChangeType.SELL,
                transactionPrice = sellPrice,
                transactionShares = sharesToSell,
                notes = notes
            )
        )
    }
    
    suspend fun adjustAverageCost(
        holding: Holding,
        newAvgCost: Double,
        notes: String = "Manual adjustment"
    ) {
        val previousAvgCost = holding.avgCost
        
        val updatedHolding = holding.copy(
            avgCost = newAvgCost,
            updatedAt = System.currentTimeMillis()
        )
        holdingDao.updateHolding(updatedHolding)
        
        costHistoryDao.insertHistory(
            CostHistory(
                holdingId = holding.id,
                stockSymbol = holding.stockSymbol,
                previousAvgCost = previousAvgCost,
                newAvgCost = newAvgCost,
                previousShares = holding.shares,
                newShares = holding.shares,
                changeType = CostChangeType.ADJUSTMENT,
                transactionPrice = newAvgCost,
                transactionShares = 0,
                notes = notes
            )
        )
    }
    
    // Cost History
    fun getCostHistory(holdingId: String): Flow<List<CostHistory>> = 
        costHistoryDao.getHistoryByHolding(holdingId)
    
    fun getCostHistoryAsc(holdingId: String): Flow<List<CostHistory>> = 
        costHistoryDao.getHistoryByHoldingAsc(holdingId)
    
    fun getRecentCostHistory(limit: Int = 50): Flow<List<CostHistory>> = 
        costHistoryDao.getRecentHistory(limit)
    
    // Stocks
    fun getAllStocks(): Flow<List<Stock>> = stockDao.getAllActiveStocks()
    
    fun searchStocks(query: String): Flow<List<Stock>> = stockDao.searchStocks(query)
    
    fun getStocksBySector(sector: String): Flow<List<Stock>> = stockDao.getStocksBySector(sector)
    
    fun getAllSectors(): Flow<List<String>> = stockDao.getAllSectors()
    
    suspend fun initializeStocks() {
        stockDao.insertStocks(EGXStocks.stocks)
        // Sync stock names in existing holdings
        syncStockNames()
    }
    
    /**
     * Update stock names in existing holdings to match current Stock table
     */
    suspend fun syncStockNames() {
        val holdings = holdingDao.getAllHoldings().first()
        val stocks = stockDao.getAllStocks().first()
        val stockMap = stocks.associateBy { it.symbol }
        
        for (holding in holdings) {
            val stock = stockMap[holding.stockSymbol]
            if (stock != null && 
                (holding.stockNameEn != stock.nameEn || 
                 holding.stockNameAr != stock.nameAr || 
                 holding.sector != stock.sector)) {
                holdingDao.updateStockName(
                    symbol = holding.stockSymbol,
                    nameEn = stock.nameEn,
                    nameAr = stock.nameAr,
                    sector = stock.sector
                )
            }
        }
    }
    
    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsByHolding(holdingId: String): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByHolding(holdingId)
    
    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    // Portfolio Analytics
    fun getPortfolioSummary(): Flow<PortfolioSummary> {
        return holdingDao.getAllHoldings().map { holdings ->
            if (holdings.isEmpty()) {
                PortfolioSummary(
                    totalValue = 0.0,
                    totalCost = 0.0,
                    totalProfitLoss = 0.0,
                    totalProfitLossPercent = 0.0,
                    holdingsCount = 0,
                    profitableCount = 0,
                    losingCount = 0,
                    topGainer = null,
                    topLoser = null,
                    sectorAllocation = emptyMap(),
                    roleAllocation = emptyMap()
                )
            } else {
                val totalValue = holdings.sumOf { it.marketValue }
                val totalCost = holdings.sumOf { it.totalCost }
                val totalProfitLoss = totalValue - totalCost
                val totalProfitLossPercent = if (totalCost > 0) (totalProfitLoss / totalCost) * 100 else 0.0
                
                val profitableHoldings = holdings.filter { it.isProfit }
                val losingHoldings = holdings.filter { !it.isProfit }
                
                val topGainer = holdings.maxByOrNull { it.profitLossPercent }
                val topLoser = holdings.minByOrNull { it.profitLossPercent }
                
                val sectorAllocation = holdings
                    .groupBy { it.sector.ifEmpty { "Other" } }
                    .mapValues { (_, sectorHoldings) ->
                        val sectorValue = sectorHoldings.sumOf { it.marketValue }
                        if (totalValue > 0) (sectorValue / totalValue) * 100 else 0.0
                    }
                
                val roleAllocation = holdings
                    .groupBy { it.role }
                    .mapValues { (_, roleHoldings) ->
                        val roleValue = roleHoldings.sumOf { it.marketValue }
                        if (totalValue > 0) (roleValue / totalValue) * 100 else 0.0
                    }
                
                PortfolioSummary(
                    totalValue = totalValue,
                    totalCost = totalCost,
                    totalProfitLoss = totalProfitLoss,
                    totalProfitLossPercent = totalProfitLossPercent,
                    holdingsCount = holdings.size,
                    profitableCount = profitableHoldings.size,
                    losingCount = losingHoldings.size,
                    topGainer = topGainer,
                    topLoser = topLoser,
                    sectorAllocation = sectorAllocation,
                    roleAllocation = roleAllocation
                )
            }
        }
    }
    
    // Stock allocation for donut chart
    fun getStockAllocation(): Flow<List<Pair<String, Double>>> {
        return holdingDao.getAllHoldings().map { holdings ->
            val totalValue = holdings.sumOf { it.marketValue }
            holdings.map { holding ->
                val percentage = if (totalValue > 0) (holding.marketValue / totalValue) * 100 else 0.0
                Pair(holding.stockSymbol, percentage)
            }.sortedByDescending { it.second }
        }
    }
    
    fun getSectorPerformance(): Flow<List<SectorPerformance>> {
        return holdingDao.getAllHoldings().map { holdings ->
            val totalPortfolioValue = holdings.sumOf { it.marketValue }
            
            holdings
                .groupBy { it.sector.ifEmpty { "Other" } }
                .map { (sector, sectorHoldings) ->
                    val sectorValue = sectorHoldings.sumOf { it.marketValue }
                    val sectorCost = sectorHoldings.sumOf { it.totalCost }
                    val sectorProfitLoss = sectorValue - sectorCost
                    val sectorProfitLossPercent = if (sectorCost > 0) (sectorProfitLoss / sectorCost) * 100 else 0.0
                    val weight = if (totalPortfolioValue > 0) (sectorValue / totalPortfolioValue) * 100 else 0.0
                    
                    SectorPerformance(
                        sector = sector,
                        totalValue = sectorValue,
                        totalCost = sectorCost,
                        profitLoss = sectorProfitLoss,
                        profitLossPercent = sectorProfitLossPercent,
                        weight = weight,
                        holdingsCount = sectorHoldings.size
                    )
                }
                .sortedByDescending { it.weight }
        }
    }
    
    // ============ CERTIFICATES ============
    
    fun getAllCertificates(): Flow<List<Certificate>> = certificateDao.getAllCertificates()
    
    suspend fun getCertificateById(id: String): Certificate? = certificateDao.getCertificateById(id)
    
    fun getCertificatesByStatus(status: CertificateStatus): Flow<List<Certificate>> = 
        certificateDao.getCertificatesByStatus(status)
    
    fun getCertificatesByBank(bankName: String): Flow<List<Certificate>> = 
        certificateDao.getCertificatesByBank(bankName)
    
    suspend fun addCertificate(certificate: Certificate) {
        certificateDao.insertCertificate(certificate)
    }
    
    suspend fun updateCertificate(certificate: Certificate) {
        certificateDao.updateCertificate(certificate)
    }
    
    suspend fun deleteCertificate(certificate: Certificate) {
        certificateDao.deleteCertificate(certificate)
    }
    
    suspend fun deleteCertificateById(id: String) {
        certificateDao.deleteCertificateById(id)
    }
    
    fun getTotalCertificatesValue(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Double?> = 
        certificateDao.getTotalCertificatesValue(status)
    
    fun getTotalMonthlyIncome(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Double?> = 
        certificateDao.getTotalMonthlyIncome(status)
    
    fun getCertificatesCount(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Int> = 
        certificateDao.getCertificatesCount(status)
    
    suspend fun getUpcomingMaturities(limit: Int = 10): List<Certificate> {
        val thirtyDaysFromNow = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        val certificates = certificateDao.getCertificatesByStatusSync(CertificateStatus.ACTIVE)
        return certificates
            .filter { it.maturityDate <= thirtyDaysFromNow }
            .sortedBy { it.maturityDate }
            .take(limit)
    }
    
    /**
     * Get monthly income summary for all certificates
     */
    suspend fun getMonthlyCertificateIncome(year: Int, month: Int): MonthlyCertificateIncome {
        val certificates = certificateDao.getAllCertificates().first()
        val certificateDetails = certificates
            .mapNotNull { cert ->
                val income = cert.getMonthlyIncomeForMonth(year, month)
                if (income > 0) {
                    val dueDate = cert.getInterestDueDateForMonth(year, month)
                    CertificateIncomeDetail(
                        certificateId = cert.id,
                        certificateNumber = cert.certificateNumber,
                        bankName = cert.bankName,
                        amount = income,
                        dueDate = dueDate ?: 0L
                    )
                } else null
            }
        
        return MonthlyCertificateIncome(
            year = year,
            month = month,
            totalIncome = certificateDetails.sumOf { it.amount },
            certificates = certificateDetails
        )
    }
    
    /**
     * Get monthly income for a range of months
     */
    suspend fun getMonthlyCertificateIncomeRange(startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): List<MonthlyCertificateIncome> {
        val certificates = certificateDao.getAllCertificates().first()
        val months = mutableListOf<MonthlyCertificateIncome>()
        
        var currentYear = startYear
        var currentMonth = startMonth
        
        while (currentYear < endYear || (currentYear == endYear && currentMonth <= endMonth)) {
            val certificateDetails = certificates
                .mapNotNull { cert ->
                    val income = cert.getMonthlyIncomeForMonth(currentYear, currentMonth)
                    if (income > 0) {
                        val dueDate = cert.getInterestDueDateForMonth(currentYear, currentMonth)
                        CertificateIncomeDetail(
                            certificateId = cert.id,
                            bankName = cert.bankName,
                            amount = income,
                            dueDate = dueDate ?: 0L
                        )
                    } else null
                }
            
            months.add(
                MonthlyCertificateIncome(
                    year = currentYear,
                    month = currentMonth,
                    totalIncome = certificateDetails.sumOf { it.amount },
                    certificates = certificateDetails
                )
            )
            
            // Move to next month
            currentMonth++
            if (currentMonth > 12) {
                currentMonth = 1
                currentYear++
            }
        }
        
        return months
    }
    
    /**
     * Update bank name for all certificates
     */
    suspend fun updateAllCertificatesBankName(oldBankName: String, newBankName: String) {
        certificateDao.updateBankName(oldBankName, newBankName)
    }
    
    // ========== EXPENSES ==========
    
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    suspend fun getExpenseById(id: String): Expense? = expenseDao.getExpenseById(id)
    
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> = 
        expenseDao.getExpensesByDateRange(startDate, endDate)
    
    fun getExpensesByCategory(category: String): Flow<List<Expense>> = 
        expenseDao.getExpensesByCategory(category)
    
    fun getExpensesByCategoryAndDateRange(category: String, startDate: Long, endDate: Long): Flow<List<Expense>> = 
        expenseDao.getExpensesByCategoryAndDateRange(category, startDate, endDate)
    
    fun getAllCategories(): Flow<List<String>> = expenseDao.getAllCategories()
    
    suspend fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Double = 
        expenseDao.getTotalExpensesByDateRange(startDate, endDate) ?: 0.0
    
    suspend fun getCategoryTotalsByDateRange(startDate: Long, endDate: Long): List<com.egx.portfoliotracker.data.local.CategoryTotal> = 
        expenseDao.getCategoryTotalsByDateRange(startDate, endDate)
    
    suspend fun getDailyExpensesByDateRange(startDate: Long, endDate: Long): List<com.egx.portfoliotracker.data.local.DailyExpense> = 
        expenseDao.getDailyExpensesByDateRange(startDate, endDate)
    
    suspend fun getMonthlyExpensesByDateRange(startDate: Long, endDate: Long): List<com.egx.portfoliotracker.data.local.MonthlyExpense> = 
        expenseDao.getMonthlyExpensesByDateRange(startDate, endDate)
    
    suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
    
    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }
    
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
    
    suspend fun deleteExpenseById(id: String) {
        expenseDao.deleteExpenseById(id)
    }
}
