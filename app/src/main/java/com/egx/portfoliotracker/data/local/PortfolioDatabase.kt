package com.egx.portfoliotracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.egx.portfoliotracker.data.model.CostHistory
import com.egx.portfoliotracker.data.model.Dividend
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.PortfolioSnapshot
import com.egx.portfoliotracker.data.model.Stock
import com.egx.portfoliotracker.data.model.Transaction

@Database(
    entities = [
        Holding::class, 
        Stock::class, 
        Transaction::class, 
        CostHistory::class,
        Dividend::class,
        PortfolioSnapshot::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PortfolioDatabase : RoomDatabase() {
    
    abstract fun holdingDao(): HoldingDao
    abstract fun stockDao(): StockDao
    abstract fun transactionDao(): TransactionDao
    abstract fun costHistoryDao(): CostHistoryDao
    abstract fun dividendDao(): DividendDao
    
    companion object {
        @Volatile
        private var INSTANCE: PortfolioDatabase? = null
        
        // Migration from version 3 to 4 (add dividends and snapshots)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create dividends table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS dividends (
                        id TEXT PRIMARY KEY NOT NULL,
                        holdingId TEXT NOT NULL,
                        stockSymbol TEXT NOT NULL,
                        amountPerShare REAL NOT NULL,
                        totalShares INTEGER NOT NULL,
                        totalAmount REAL NOT NULL,
                        exDividendDate INTEGER,
                        paymentDate INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                // Create portfolio_snapshots table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS portfolio_snapshots (
                        id TEXT PRIMARY KEY NOT NULL,
                        totalValue REAL NOT NULL,
                        totalCost REAL NOT NULL,
                        profitLoss REAL NOT NULL,
                        profitLossPercent REAL NOT NULL,
                        totalDividends REAL NOT NULL,
                        holdingsCount INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // Migration from version 2 to 3 (preserves data)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes, just version bump to ensure stability
            }
        }
        
        // Migration from version 1 to 3 (for users who still have v1)
        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create cost_history table if it doesn't exist
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cost_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        holdingId TEXT NOT NULL,
                        stockSymbol TEXT NOT NULL,
                        previousAvgCost REAL NOT NULL,
                        newAvgCost REAL NOT NULL,
                        previousShares INTEGER NOT NULL,
                        newShares INTEGER NOT NULL,
                        changeType TEXT NOT NULL,
                        transactionPrice REAL NOT NULL,
                        transactionShares INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
        
        fun getDatabase(context: Context): PortfolioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortfolioDatabase::class.java,
                    "portfolio_database"
                )
                .addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration() // Only as last resort
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
