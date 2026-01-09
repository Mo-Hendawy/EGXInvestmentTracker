package com.egx.portfoliotracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scrapes financial data (EPS, P/E ratio, Book Value) from English Mubasher
 * Used ONLY for stock analysis and fair value calculations
 */
@Singleton
class MubasherScraper @Inject constructor() {
    
    companion object {
        private const val BASE_URL = "https://english.mubasher.info/markets/EGX/stocks/"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT = 15000
    }
    
    data class StockFinancials(
        val symbol: String,
        val eps: Double?,           // Earnings Per Share
        val peRatio: Double?,       // Price to Earnings ratio
        val bookValue: Double?,     // Book Value Per Share
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    /**
     * Fetches EPS, P/E ratio, and Book Value for a given stock symbol
     * Returns null if scraping fails
     */
    suspend fun fetchStockFinancials(symbol: String): StockFinancials? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL$symbol")
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                    connectTimeout = TIMEOUT
                    readTimeout = TIMEOUT
                }
                
                if (connection.responseCode != 200) {
                    return@withContext null
                }
                
                val html = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                val eps = extractValue(html, "EPS")
                val peRatio = extractValue(html, "P/E Ratio")
                val bookValue = extractValue(html, "Book Value \\(BVPS\\)")
                
                StockFinancials(
                    symbol = symbol,
                    eps = eps,
                    peRatio = peRatio,
                    bookValue = bookValue
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Fetches financials for multiple stocks
     */
    suspend fun fetchMultipleStockFinancials(symbols: List<String>): Map<String, StockFinancials> {
        val results = mutableMapOf<String, StockFinancials>()
        for (symbol in symbols) {
            fetchStockFinancials(symbol)?.let { financials ->
                results[symbol] = financials
            }
        }
        return results
    }
    
    /**
     * Extracts a numeric value from HTML based on the label
     * Pattern: <span>Label</span> ... <span class="number number--aligned">VALUE</span>
     */
    private fun extractValue(html: String, label: String): Double? {
        return try {
            // Pattern to match: Label</span> ... <span class="number number--aligned">VALUE</span>
            val pattern = Regex(
                """$label</span>\s*<span class="stock-overview__value">\s*<span class="number number--aligned">([0-9,.-]+)</span>""",
                RegexOption.IGNORE_CASE
            )
            val match = pattern.find(html)
            match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

