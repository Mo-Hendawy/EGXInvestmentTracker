package com.egx.portfoliotracker.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching real-time EGX stock prices
 * Primary: TradingView API
 * Fallback: CNBC API
 */
@Singleton
class StockPriceService @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * Fetch current price for a single stock
     * Tries TradingView first, falls back to CNBC
     */
    suspend fun getPrice(symbol: String): StockPriceResult {
        return withContext(Dispatchers.IO) {
            // Try TradingView first
            val tvResult = fetchFromTradingView(symbol)
            if (tvResult is StockPriceResult.Success) {
                Log.d("StockPriceService", "Got price from TradingView for $symbol: ${tvResult.price}")
                return@withContext tvResult
            }
            
            // Fallback to CNBC
            Log.d("StockPriceService", "TradingView failed for $symbol, trying CNBC...")
            val cnbcResult = fetchFromCNBC(symbol)
            if (cnbcResult is StockPriceResult.Success) {
                Log.d("StockPriceService", "Got price from CNBC for $symbol: ${cnbcResult.price}")
                return@withContext cnbcResult
            }
            
            // Both failed
            Log.e("StockPriceService", "Both APIs failed for $symbol")
            StockPriceResult.Error(symbol, "Unable to fetch price from any source")
        }
    }
    
    /**
     * Fetch from TradingView Scanner API
     */
    private fun fetchFromTradingView(symbol: String): StockPriceResult {
        return try {
            val jsonBody = """
                {
                    "symbols": {
                        "tickers": ["EGX:$symbol"],
                        "query": {"types": []}
                    },
                    "columns": ["close", "change", "volume", "open", "high", "low", "Recommend.All"]
                }
            """.trimIndent()
            
            val request = Request.Builder()
                .url("https://scanner.tradingview.com/egypt/scan")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return StockPriceResult.Error(symbol, "TradingView HTTP ${response.code}")
            }
            
            val body = response.body?.string() ?: return StockPriceResult.Error(symbol, "Empty response")
            val json = JSONObject(body)
            
            val dataArray = json.optJSONArray("data")
            if (dataArray == null || dataArray.length() == 0) {
                return StockPriceResult.Error(symbol, "No data for $symbol")
            }
            
            val stockData = dataArray.getJSONObject(0)
            val values = stockData.getJSONArray("d")
            
            val price = values.optDouble(0, Double.NaN)
            if (price.isNaN()) {
                return StockPriceResult.Error(symbol, "Invalid price data")
            }
            
            val change = values.optDouble(1, 0.0)
            val volume = values.optLong(2, 0)
            val open = values.optDouble(3, Double.NaN)
            val high = values.optDouble(4, Double.NaN)
            val low = values.optDouble(5, Double.NaN)
            
            // Calculate previous close from price and change percent
            val previousClose = if (change != 0.0) price / (1 + change / 100) else price
            
            StockPriceResult.Success(
                symbol = symbol,
                price = price,
                previousClose = previousClose,
                change = price - previousClose,
                changePercent = change,
                open = if (open.isNaN()) null else open,
                high = if (high.isNaN()) null else high,
                low = if (low.isNaN()) null else low,
                volume = volume,
                source = "TradingView"
            )
        } catch (e: Exception) {
            Log.e("StockPriceService", "TradingView error for $symbol", e)
            StockPriceResult.Error(symbol, e.message ?: "TradingView error")
        }
    }
    
    /**
     * Fetch from CNBC Quote API
     */
    private fun fetchFromCNBC(symbol: String): StockPriceResult {
        return try {
            val url = "https://quote.cnbc.com/quote-html-webservice/restQuote/symbolType/symbol" +
                    "?symbols=$symbol-EG&requestMethod=itv&noCache=1&partnerId=2&fund=1&exthrs=1&output=json"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return StockPriceResult.Error(symbol, "CNBC HTTP ${response.code}")
            }
            
            val body = response.body?.string() ?: return StockPriceResult.Error(symbol, "Empty response")
            val json = JSONObject(body)
            
            val quoteResult = json.optJSONObject("FormattedQuoteResult")
                ?: return StockPriceResult.Error(symbol, "Invalid CNBC response")
            
            val quotes = quoteResult.optJSONArray("FormattedQuote")
            if (quotes == null || quotes.length() == 0) {
                return StockPriceResult.Error(symbol, "No quote data")
            }
            
            val quote = quotes.getJSONObject(0)
            
            val priceStr = quote.optString("last", "")
            val price = priceStr.replace(",", "").toDoubleOrNull()
                ?: return StockPriceResult.Error(symbol, "Invalid price")
            
            val changeStr = quote.optString("change", "0").replace(",", "")
            val change = changeStr.toDoubleOrNull() ?: 0.0
            
            val changePercentStr = quote.optString("change_pct", "0%")
                .replace("%", "").replace("+", "").trim()
            val changePercent = changePercentStr.toDoubleOrNull() ?: 0.0
            
            val previousClose = quote.optString("previous_day_closing", "")
                .replace(",", "").toDoubleOrNull()
            
            val open = quote.optString("open", "").replace(",", "").toDoubleOrNull()
            val high = quote.optString("high", "").replace(",", "").toDoubleOrNull()
            val low = quote.optString("low", "").replace(",", "").toDoubleOrNull()
            val volume = quote.optString("volume", "0").replace(",", "").toLongOrNull() ?: 0
            
            StockPriceResult.Success(
                symbol = symbol,
                price = price,
                previousClose = previousClose,
                change = change,
                changePercent = changePercent,
                open = open,
                high = high,
                low = low,
                volume = volume,
                source = "CNBC"
            )
        } catch (e: Exception) {
            Log.e("StockPriceService", "CNBC error for $symbol", e)
            StockPriceResult.Error(symbol, e.message ?: "CNBC error")
        }
    }
    
    /**
     * Fetch prices for multiple stocks
     */
    suspend fun getPrices(symbols: List<String>): Map<String, StockPriceResult> {
        return withContext(Dispatchers.IO) {
            symbols.associateWith { symbol ->
                getPrice(symbol)
            }
        }
    }
}

sealed class StockPriceResult {
    data class Success(
        val symbol: String,
        val price: Double,
        val previousClose: Double?,
        val change: Double?,
        val changePercent: Double?,
        val open: Double? = null,
        val high: Double? = null,
        val low: Double? = null,
        val volume: Long = 0,
        val source: String = ""
    ) : StockPriceResult()
    
    data class Error(
        val symbol: String,
        val message: String
    ) : StockPriceResult()
}
