package com.egx.portfoliotracker.data.repository

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.egx.portfoliotracker.data.local.PortfolioDatabase
import com.egx.portfoliotracker.data.model.*
import java.util.UUID

/**
 * Aggressive data recovery from database
 */
class DataRecovery(
    private val database: PortfolioDatabase,
    private val context: Context
) {
    
    fun recoverAllData(): RecoveryResult {
        try {
            Log.e("DataRecovery", "=== STARTING RECOVERY ===")
            
            // FIRST: Try to recover from recovery database files if they exist
            val filesDir = context.getFilesDir()
            val recoveryDbFile = java.io.File(filesDir, "portfolio_database_recovery.db")
            val recoveryWalFile = java.io.File(filesDir, "portfolio_database_wal_recovery")
            
            Log.e("DataRecovery", "Checking for recovery DB at: ${recoveryDbFile.absolutePath}")
            Log.e("DataRecovery", "Recovery DB exists: ${recoveryDbFile.exists()}, size: ${recoveryDbFile.length()}")
            
            // Try recovery database first
            if (recoveryDbFile.exists() && recoveryDbFile.length() > 0) {
                Log.e("DataRecovery", "Found recovery database file: ${recoveryDbFile.length()} bytes")
                try {
                    // Try with WAL file if it exists
                    if (recoveryWalFile.exists() && recoveryWalFile.length() > 0) {
                        Log.e("DataRecovery", "Found recovery WAL file: ${recoveryWalFile.length()} bytes")
                        // Copy WAL to database location temporarily
                        val tempDbPath = java.io.File(filesDir, "temp_recovery.db")
                        val tempWalPath = java.io.File(filesDir, "temp_recovery.db-wal")
                        try {
                            recoveryDbFile.copyTo(tempDbPath, overwrite = true)
                            recoveryWalFile.copyTo(tempWalPath, overwrite = true)
                            Log.e("DataRecovery", "Copied recovery files for WAL recovery")
                            
                            val recoveryDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                                tempDbPath.absolutePath,
                                null,
                                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                            )
                            Log.e("DataRecovery", "Opened recovery database with WAL")
                            val result = recoverFromDatabase(recoveryDb, "RECOVERY_DB_WAL")
                            recoveryDb.close()
                            tempDbPath.delete()
                            tempWalPath.delete()
                            
                            if (result.holdings.isNotEmpty() || result.certificates.isNotEmpty() || result.expenses.isNotEmpty()) {
                                Log.e("DataRecovery", "SUCCESS! Recovered from recovery database with WAL!")
                                return result
                            }
                        } catch (e: Exception) {
                            Log.e("DataRecovery", "Error with WAL recovery: ${e.message}", e)
                            tempDbPath.delete()
                            tempWalPath.delete()
                        }
                    }
                    
                    // Try without WAL
                    val recoveryDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                        recoveryDbFile.absolutePath,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                    )
                    Log.e("DataRecovery", "Opened recovery database successfully")
                    val result = recoverFromDatabase(recoveryDb, "RECOVERY_DB")
                    recoveryDb.close()
                    Log.e("DataRecovery", "Recovery DB result: ${result.holdings.size} holdings, ${result.certificates.size} certs, ${result.expenses.size} expenses")
                    if (result.holdings.isNotEmpty() || result.certificates.isNotEmpty() || result.expenses.isNotEmpty()) {
                        Log.e("DataRecovery", "SUCCESS! Recovered from recovery database!")
                        return result
                    } else {
                        Log.e("DataRecovery", "Recovery DB was empty, trying current DB")
                    }
                } catch (e: Exception) {
                    Log.e("DataRecovery", "Error reading recovery database: ${e.message}", e)
                }
            } else {
                Log.e("DataRecovery", "No recovery database file found, trying current DB")
            }
            
            // Try current database WITH WAL before checkpoint
            val db = database.openHelper.writableDatabase as android.database.sqlite.SQLiteDatabase
            val currentWalFile = java.io.File(context.getDatabasePath("portfolio_database").parent, "portfolio_database-wal")
            if (currentWalFile.exists() && currentWalFile.length() > 0) {
                Log.e("DataRecovery", "Found current WAL file: ${currentWalFile.length()} bytes - reading BEFORE checkpoint")
                val result = recoverFromDatabase(db, "CURRENT_DB_BEFORE_CHECKPOINT")
                if (result.holdings.isNotEmpty() || result.certificates.isNotEmpty() || result.expenses.isNotEmpty()) {
                    Log.e("DataRecovery", "SUCCESS! Found data in current DB before checkpoint!")
                    return result
                }
            }
            
            // Final attempt - current database after checkpoint
            return recoverFromDatabase(db, "CURRENT_DB")
        } catch (e: Exception) {
            Log.e("DataRecovery", "FATAL ERROR in recoverAllData: ${e.message}", e)
            return RecoveryResult(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
    
    private fun recoverFromDatabase(db: android.database.sqlite.SQLiteDatabase, source: String): RecoveryResult {
        Log.e("DataRecovery", "=== RECOVERING FROM $source ===")
        
        // Check if tables exist FIRST
        val tablesCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        val tables = mutableListOf<String>()
        while (tablesCursor.moveToNext()) {
            tables.add(tablesCursor.getString(0))
        }
        tablesCursor.close()
        Log.e("DataRecovery", "$source - Tables found: ${tables.joinToString()}")
        
        // Check row counts BEFORE checkpoint (only for current DB)
        if (source == "CURRENT_DB") {
            try {
                val holdingsCountCursor = db.rawQuery("SELECT COUNT(*) FROM holdings", null)
                if (holdingsCountCursor.moveToFirst()) {
                    val count = holdingsCountCursor.getInt(0)
                    Log.e("DataRecovery", "$source - Holdings count BEFORE recovery: $count")
                }
                holdingsCountCursor.close()
            } catch (e: Exception) {
                Log.e("DataRecovery", "$source - Error checking holdings count: ${e.message}")
            }
            
            // Force checkpoint AFTER checking
            try {
                db.execSQL("PRAGMA wal_checkpoint(FULL)")
                Log.e("DataRecovery", "$source - Checkpoint FULL completed")
            } catch (e: Exception) {
                Log.e("DataRecovery", "$source - Checkpoint error: ${e.message}")
            }
        }
        
        val recoveredHoldings = mutableListOf<Holding>()
        val recoveredCertificates = mutableListOf<Certificate>()
        val recoveredExpenses = mutableListOf<Expense>()
        val recoveredTransactions = mutableListOf<Transaction>()
        val recoveredDividends = mutableListOf<Dividend>()
        
        // Recover holdings
        try {
            val holdingsCursor: Cursor? = db.rawQuery("SELECT * FROM holdings", null)
            if (holdingsCursor != null) {
                Log.e("DataRecovery", "$source - Holdings cursor count: ${holdingsCursor.count}")
                while (holdingsCursor.moveToNext()) {
                    try {
                        val holding = Holding(
                            id = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("id")),
                            stockSymbol = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("stockSymbol")),
                            stockNameEn = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("stockNameEn")),
                            stockNameAr = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("stockNameAr")),
                            shares = holdingsCursor.getInt(holdingsCursor.getColumnIndexOrThrow("shares")),
                            avgCost = holdingsCursor.getDouble(holdingsCursor.getColumnIndexOrThrow("avgCost")),
                            currentPrice = holdingsCursor.getDouble(holdingsCursor.getColumnIndexOrThrow("currentPrice")),
                            role = try {
                                HoldingRole.valueOf(holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("role")))
                            } catch (e: Exception) {
                                HoldingRole.CORE
                            },
                            status = try {
                                HoldingStatus.valueOf(holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("status")))
                            } catch (e: Exception) {
                                HoldingStatus.HOLD
                            },
                            sector = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("sector")),
                            notes = holdingsCursor.getString(holdingsCursor.getColumnIndexOrThrow("notes")),
                            targetPercentage = try {
                                val colIndex = holdingsCursor.getColumnIndex("targetPercentage")
                                if (colIndex >= 0 && !holdingsCursor.isNull(colIndex)) {
                                    holdingsCursor.getDouble(colIndex)
                                } else null
                            } catch (e: Exception) {
                                null
                            },
                            createdAt = holdingsCursor.getLong(holdingsCursor.getColumnIndexOrThrow("createdAt")),
                            updatedAt = holdingsCursor.getLong(holdingsCursor.getColumnIndexOrThrow("updatedAt"))
                        )
                        recoveredHoldings.add(holding)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                holdingsCursor.close()
                Log.e("DataRecovery", "$source - Recovered ${recoveredHoldings.size} holdings")
            }
        } catch (e: Exception) {
            Log.e("DataRecovery", "$source - Error reading holdings: ${e.message}")
        }
        
        // Recover certificates
        try {
            val certCursor: Cursor? = db.rawQuery("SELECT * FROM certificates", null)
            if (certCursor != null) {
                Log.e("DataRecovery", "$source - Certificates cursor count: ${certCursor.count}")
                while (certCursor.moveToNext()) {
                    try {
                        val cert = Certificate(
                            id = certCursor.getString(certCursor.getColumnIndexOrThrow("id")),
                            bankName = certCursor.getString(certCursor.getColumnIndexOrThrow("bankName")),
                            principalAmount = certCursor.getDouble(certCursor.getColumnIndexOrThrow("principalAmount")),
                            durationYears = certCursor.getInt(certCursor.getColumnIndexOrThrow("durationYears")),
                            annualInterestRate = certCursor.getDouble(certCursor.getColumnIndexOrThrow("annualInterestRate")),
                            purchaseDate = certCursor.getLong(certCursor.getColumnIndexOrThrow("purchaseDate")),
                            interestPaymentFrequency = InterestFrequency.valueOf(
                                certCursor.getString(certCursor.getColumnIndexOrThrow("interestPaymentFrequency"))
                            ),
                            status = CertificateStatus.valueOf(
                                certCursor.getString(certCursor.getColumnIndexOrThrow("status"))
                            ),
                            certificateNumber = try {
                                val colIndex = certCursor.getColumnIndex("certificateNumber")
                                if (colIndex >= 0) certCursor.getString(colIndex) else ""
                            } catch (e: Exception) {
                                ""
                            },
                            notes = certCursor.getString(certCursor.getColumnIndexOrThrow("notes")),
                            createdAt = certCursor.getLong(certCursor.getColumnIndexOrThrow("createdAt")),
                            updatedAt = certCursor.getLong(certCursor.getColumnIndexOrThrow("updatedAt"))
                        )
                        recoveredCertificates.add(cert)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                certCursor.close()
                Log.e("DataRecovery", "$source - Recovered ${recoveredCertificates.size} certificates")
            }
        } catch (e: Exception) {
            Log.e("DataRecovery", "$source - Error reading certificates: ${e.message}")
        }
        
        // Recover expenses
        try {
            val expenseCursor: Cursor? = db.rawQuery("SELECT * FROM expenses", null)
            if (expenseCursor != null) {
                Log.e("DataRecovery", "$source - Expenses cursor count: ${expenseCursor.count}")
                while (expenseCursor.moveToNext()) {
                    try {
                        val expense = Expense(
                            id = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("id")),
                            category = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("category")),
                            amount = expenseCursor.getDouble(expenseCursor.getColumnIndexOrThrow("amount")),
                            description = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("description")),
                            date = expenseCursor.getLong(expenseCursor.getColumnIndexOrThrow("date")),
                            createdAt = expenseCursor.getLong(expenseCursor.getColumnIndexOrThrow("createdAt")),
                            updatedAt = expenseCursor.getLong(expenseCursor.getColumnIndexOrThrow("updatedAt"))
                        )
                        recoveredExpenses.add(expense)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                expenseCursor.close()
                Log.e("DataRecovery", "$source - Recovered ${recoveredExpenses.size} expenses")
            }
        } catch (e: Exception) {
            Log.e("DataRecovery", "$source - Error reading expenses: ${e.message}")
        }
        
        Log.e("DataRecovery", "$source - Total: ${recoveredHoldings.size} holdings, ${recoveredCertificates.size} certificates, ${recoveredExpenses.size} expenses")
        
        return RecoveryResult(
            holdings = recoveredHoldings,
            certificates = recoveredCertificates,
            expenses = recoveredExpenses,
            transactions = recoveredTransactions,
            dividends = recoveredDividends
        )
    }
}

data class RecoveryResult(
    val holdings: List<Holding>,
    val certificates: List<Certificate>,
    val expenses: List<Expense>,
    val transactions: List<Transaction>,
    val dividends: List<Dividend>
)

