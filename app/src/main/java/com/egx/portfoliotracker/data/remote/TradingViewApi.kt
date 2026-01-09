package com.egx.portfoliotracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches financial data from TradingView Scanner API
 * Primary source - more accurate when available
 */
@Singleton
class TradingViewApi @Inject constructor() {
    
    companion object {
        private const val API_URL = "https://scanner.tradingview.com/egypt/scan"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT = 15000
    }
    
    data class StockData(
        val symbol: String,
        val price: Double?,
        val eps: Double?,
        val peRatio: Double?,
        val bookValue: Double?,
        val pbRatio: Double?
    )
    
    /**
     * Fetches financial data for multiple stocks in a single API call
     * Returns map of symbol -> StockData
     */
    suspend fun fetchStocksData(symbols: List<String>): Map<String, StockData> {
        return withContext(Dispatchers.IO) {
            try {
                val tickers = symbols.map { "\"EGX:$it\"" }.joinToString(",")
                val requestBody = """
                    {
                        "symbols": {"tickers": [$tickers]},
                        "columns": [
                            "name",
                            "close",
                            "price_earnings_ttm",
                            "price_book_fq",
                            "earnings_per_share_basic_ttm",
                            "book_value_per_share_fq"
                        ]
                    }
                """.trimIndent()
                
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = TIMEOUT
                    readTimeout = TIMEOUT
                    doOutput = true
                }
                
                connection.outputStream.use { os ->
                    os.write(requestBody.toByteArray())
                }
                
                if (connection.responseCode != 200) {
                    return@withContext emptyMap()
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                parseResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }
    
    /**
     * Fetches data for a single stock
     */
    suspend fun fetchStockData(symbol: String): StockData? {
        val results = fetchStocksData(listOf(symbol))
        return results[symbol]
    }
    
    /**
     * Parses the TradingView API response
     * Response format: {"totalCount":N,"data":[{"s":"EGX:SYMBOL","d":["NAME",close,pe,pb,eps,bvps]},...]}
     */
    private fun parseResponse(jsonString: String): Map<String, StockData> {
        val results = mutableMapOf<String, StockData>()
        
        try {
            val json = JSONObject(jsonString)
            val dataArray = json.getJSONArray("data")
            
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val fullSymbol = item.getString("s") // "EGX:COMI"
                val symbol = fullSymbol.removePrefix("EGX:")
                
                val values = item.getJSONArray("d")
                // d = [name, close, pe, pb, eps, bvps]
                
                val stockData = StockData(
                    symbol = symbol,
                    price = getDoubleOrNull(values, 1),
                    peRatio = getDoubleOrNull(values, 2),
                    pbRatio = getDoubleOrNull(values, 3),
                    eps = getDoubleOrNull(values, 4),
                    bookValue = getDoubleOrNull(values, 5)
                )
                
                results[symbol] = stockData
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return results
    }
    
    private fun getDoubleOrNull(array: org.json.JSONArray, index: Int): Double? {
        return try {
            if (array.isNull(index)) null else array.getDouble(index)
        } catch (e: Exception) {
            null
        }
    }
}

