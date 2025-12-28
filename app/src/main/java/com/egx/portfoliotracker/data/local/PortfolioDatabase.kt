package com.egx.portfoliotracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CostHistory
import com.egx.portfoliotracker.data.model.Dividend
import com.egx.portfoliotracker.data.model.Expense
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.PortfolioSnapshot
import com.egx.portfoliotracker.data.model.Stock
import com.egx.portfoliotracker.data.model.Transaction
import com.egx.portfoliotracker.data.model.Watchlist

@Database(
    entities = [
        Holding::class, 
        Stock::class, 
        Transaction::class, 
        CostHistory::class,
        Dividend::class,
        PortfolioSnapshot::class,
        Certificate::class,
        Expense::class,
        Watchlist::class
    ],
    version = 11,
    exportSchema = true
)
abstract class PortfolioDatabase : RoomDatabase() {
    
    /**
     * Force checkpoint to merge WAL file into main database
     * This can recover data that was in the WAL but not yet committed
     */
    fun forceCheckpoint() {
        try {
            val db = openHelper.writableDatabase as android.database.sqlite.SQLiteDatabase
            db.execSQL("PRAGMA wal_checkpoint(FULL)")
        } catch (e: Exception) {
            // Ignore checkpoint errors - database might be in use
            e.printStackTrace()
        }
    }
    
    abstract fun holdingDao(): HoldingDao
    abstract fun stockDao(): StockDao
    abstract fun transactionDao(): TransactionDao
    abstract fun costHistoryDao(): CostHistoryDao
    abstract fun dividendDao(): DividendDao
    abstract fun certificateDao(): CertificateDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun watchlistDao(): WatchlistDao
    
    companion object {
        @Volatile
        private var INSTANCE: PortfolioDatabase? = null
        
        // Migration from version 9 to 10 (add fairValue to holdings)
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add fairValue column to holdings table
                database.execSQL("ALTER TABLE holdings ADD COLUMN fairValue REAL")
            }
        }
        
        // Migration from version 8 to 9 (add watchlist)
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create watchlist table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS watchlist (
                        id TEXT PRIMARY KEY NOT NULL,
                        stockSymbol TEXT NOT NULL,
                        stockNameEn TEXT NOT NULL,
                        stockNameAr TEXT NOT NULL,
                        sector TEXT NOT NULL DEFAULT '',
                        targetPrice REAL,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // Migration from version 7 to 8 (add expenses)
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create expenses table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id TEXT PRIMARY KEY NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        date INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // Migration from version 6 to 7 (add targetPercentage to holdings)
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add targetPercentage column to holdings table
                database.execSQL("ALTER TABLE holdings ADD COLUMN targetPercentage REAL")
            }
        }
        
        // Migration from version 5 to 6 (add certificateNumber field)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add certificateNumber column
                database.execSQL("ALTER TABLE certificates ADD COLUMN certificateNumber TEXT NOT NULL DEFAULT ''")
                
                // Migrate certificate numbers from notes to certificateNumber field
                // Extract "ID: XXX" from notes and put in certificateNumber
                database.execSQL("""
                    UPDATE certificates 
                    SET certificateNumber = CASE 
                        WHEN notes LIKE 'ID: %' THEN SUBSTR(notes, 5)
                        ELSE ''
                    END
                """)
            }
        }
        
        // Migration from version 4 to 5 (add certificates)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create certificates table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS certificates (
                        id TEXT PRIMARY KEY NOT NULL,
                        bankName TEXT NOT NULL,
                        principalAmount REAL NOT NULL,
                        durationYears INTEGER NOT NULL,
                        annualInterestRate REAL NOT NULL,
                        purchaseDate INTEGER NOT NULL,
                        interestPaymentFrequency TEXT NOT NULL,
                        status TEXT NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
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
        
        // Migration from version 10 to 11 (add EPS, growthRate, peRatio to holdings)
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add EPS column
                database.execSQL("ALTER TABLE holdings ADD COLUMN eps REAL DEFAULT NULL")
                // Add growthRate column
                database.execSQL("ALTER TABLE holdings ADD COLUMN growthRate REAL DEFAULT NULL")
                // Add peRatio column
                database.execSQL("ALTER TABLE holdings ADD COLUMN peRatio REAL DEFAULT NULL")
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
                .addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                // Removed fallbackToDestructiveMigration() to prevent data loss
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
