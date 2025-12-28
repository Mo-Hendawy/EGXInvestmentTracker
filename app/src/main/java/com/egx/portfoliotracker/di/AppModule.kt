package com.egx.portfoliotracker.di

import android.content.Context
import com.egx.portfoliotracker.data.local.CertificateDao
import com.egx.portfoliotracker.data.local.CostHistoryDao
import com.egx.portfoliotracker.data.local.DividendDao
import com.egx.portfoliotracker.data.local.ExpenseDao
import com.egx.portfoliotracker.data.local.HoldingDao
import com.egx.portfoliotracker.data.local.PortfolioDatabase
import com.egx.portfoliotracker.data.local.StockDao
import com.egx.portfoliotracker.data.local.TransactionDao
import com.egx.portfoliotracker.data.local.WatchlistDao
import com.egx.portfoliotracker.data.remote.StockPriceService
import com.egx.portfoliotracker.data.repository.PortfolioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePortfolioDatabase(@ApplicationContext context: Context): PortfolioDatabase {
        return PortfolioDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideHoldingDao(database: PortfolioDatabase): HoldingDao {
        return database.holdingDao()
    }
    
    @Provides
    @Singleton
    fun provideStockDao(database: PortfolioDatabase): StockDao {
        return database.stockDao()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: PortfolioDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideCostHistoryDao(database: PortfolioDatabase): CostHistoryDao {
        return database.costHistoryDao()
    }
    
    @Provides
    @Singleton
    fun provideDividendDao(database: PortfolioDatabase): DividendDao {
        return database.dividendDao()
    }
    
    @Provides
    @Singleton
    fun provideCertificateDao(database: PortfolioDatabase): CertificateDao {
        return database.certificateDao()
    }
    
    @Provides
    @Singleton
    fun provideExpenseDao(database: PortfolioDatabase): ExpenseDao {
        return database.expenseDao()
    }
    
    @Provides
    @Singleton
    fun provideWatchlistDao(database: PortfolioDatabase): WatchlistDao {
        return database.watchlistDao()
    }
    
    @Provides
    @Singleton
    fun provideStockPriceService(): StockPriceService {
        return StockPriceService()
    }
    
            @Provides
            @Singleton
            fun providePortfolioRepository(
                holdingDao: HoldingDao,
                stockDao: StockDao,
                transactionDao: TransactionDao,
                costHistoryDao: CostHistoryDao,
                dividendDao: DividendDao,
                certificateDao: CertificateDao,
                expenseDao: ExpenseDao,
                watchlistDao: WatchlistDao,
                stockPriceService: StockPriceService,
                database: PortfolioDatabase,
                @ApplicationContext context: Context
            ): PortfolioRepository {
                return PortfolioRepository(
                    holdingDao,
                    stockDao,
                    transactionDao,
                    costHistoryDao,
                    dividendDao,
                    certificateDao,
                    expenseDao,
                    watchlistDao,
                    stockPriceService,
                    database,
                    context
                )
            }
}
