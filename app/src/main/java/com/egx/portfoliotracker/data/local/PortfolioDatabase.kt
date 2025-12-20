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

@Database(
    entities = [
        Holding::class, 
        Stock::class, 
        Transaction::class, 
        CostHistory::class,
        Dividend::class,
        PortfolioSnapshot::class,
        Certificate::class,
        Expense::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PortfolioDatabase : RoomDatabase() {
    
    abstract fun holdingDao(): HoldingDao
    abstract fun stockDao(): StockDao
    abstract fun transactionDao(): TransactionDao
    abstract fun costHistoryDao(): CostHistoryDao
    abstract fun dividendDao(): DividendDao
    abstract fun certificateDao(): CertificateDao
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        @Volatile
        private var INSTANCE: PortfolioDatabase? = null
        
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
                // Check if targetPercentage column exists
                val cursor = database.query("PRAGMA table_info(holdings)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "targetPercentage") {
                        columnExists = true
                        break
                    }
                }
                cursor.close()
                
                // Add targetPercentage column only if it doesn't exist
                if (!columnExists) {
                    database.execSQL("ALTER TABLE holdings ADD COLUMN targetPercentage REAL")
                }
            }
        }
        
        // Migration from version 5 to 6 (add certificateNumber field)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if certificateNumber column exists
                val cursor = database.query("PRAGMA table_info(certificates)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "certificateNumber") {
                        columnExists = true
                        break
                    }
                }
                cursor.close()
                
                // Add certificateNumber column only if it doesn't exist
                if (!columnExists) {
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
                .addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
