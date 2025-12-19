package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.UUID

/**
 * Represents a bank saving certificate
 */
@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bankName: String,                          // Bank name
    val certificateNumber: String = "",            // Certificate number/ID (e.g., "235", "236")
    val principalAmount: Double,                    // Initial investment (e.g., 100,000)
    val durationYears: Int,                        // Duration in years (e.g., 3)
    val annualInterestRate: Double,                // Annual interest rate % (e.g., 20%)
    val purchaseDate: Long,                        // Purchase date timestamp
    val interestPaymentFrequency: InterestFrequency, // How often interest is paid
    val status: CertificateStatus = CertificateStatus.ACTIVE,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Auto-calculated properties
    
    /**
     * Calculate maturity date: purchase date + duration years
     */
    val maturityDate: Long
        get() {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = purchaseDate
                add(Calendar.YEAR, durationYears)
            }
            return calendar.timeInMillis
        }
    
    /**
     * Monthly interest payment amount
     * Formula: (Principal × Annual Rate / 100) / 12
     */
    val monthlyInterest: Double
        get() = (principalAmount * annualInterestRate / 100) / 12
    
    /**
     * Total interest that will be earned at maturity
     * Formula: Principal × Rate × Years
     */
    val totalInterestAtMaturity: Double
        get() = principalAmount * (annualInterestRate / 100) * durationYears
    
    /**
     * Days since purchase
     */
    val daysSincePurchase: Long
        get() = maxOf(0, (System.currentTimeMillis() - purchaseDate) / (1000 * 60 * 60 * 24))
    
    /**
     * Accrued interest based on days since purchase
     * Formula: Principal × (Rate / 100) × (Days / 365)
     */
    val accruedInterest: Double
        get() {
            val years = daysSincePurchase / 365.0
            return principalAmount * (annualInterestRate / 100) * years
        }
    
    /**
     * Current value = Principal + Accrued Interest
     */
    val currentValue: Double
        get() = principalAmount + accruedInterest
    
    /**
     * Days until maturity
     */
    val daysUntilMaturity: Long
        get() = maxOf(0, (maturityDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24))
    
    /**
     * Check if certificate has matured
     */
    val isMatured: Boolean
        get() = System.currentTimeMillis() >= maturityDate
    
    /**
     * Get the month number (1-12) when certificate was purchased
     */
    private fun getPurchaseMonth(): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDate
        }
        return calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
    }
    
    /**
     * Get the year when certificate was purchased
     */
    private fun getPurchaseYear(): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDate
        }
        return calendar.get(Calendar.YEAR)
    }
    
    /**
     * Calculate monthly income for a specific month based on payment frequency
     */
    fun getMonthlyIncomeForMonth(year: Int, month: Int): Double {
        if (status != CertificateStatus.ACTIVE) return 0.0
        
        val purchaseYear = getPurchaseYear()
        val purchaseMonth = getPurchaseMonth()
        
        // Check if this month is after purchase date
        if (year < purchaseYear || (year == purchaseYear && month < purchaseMonth)) {
            return 0.0
        }
        
        // Check if certificate has matured before this month
        val calendar = Calendar.getInstance().apply {
            timeInMillis = maturityDate
        }
        val maturityYear = calendar.get(Calendar.YEAR)
        val maturityMonth = calendar.get(Calendar.MONTH) + 1
        
        if (year > maturityYear || (year == maturityYear && month > maturityMonth)) {
            return 0.0
        }
        
        return when (interestPaymentFrequency) {
            InterestFrequency.MONTHLY -> monthlyInterest
            InterestFrequency.QUARTERLY -> {
                // Calculate months since purchase
                val monthsSincePurchase = (year - purchaseYear) * 12 + (month - purchaseMonth)
                if (monthsSincePurchase >= 0 && monthsSincePurchase % 3 == 0) {
                    monthlyInterest * 3 // Quarterly payment
                } else {
                    0.0
                }
            }
            InterestFrequency.ANNUALLY -> {
                if (month == purchaseMonth) {
                    monthlyInterest * 12 // Annual payment
                } else {
                    0.0
                }
            }
            InterestFrequency.AT_MATURITY -> {
                // Only at maturity
                if (year == maturityYear && month == maturityMonth) {
                    totalInterestAtMaturity
                } else {
                    0.0
                }
            }
        }
    }
    
    /**
     * Get the due date for interest payment in a specific month
     * For monthly payments, uses the same day of month as purchase date
     * If that day doesn't exist in the month (e.g., Feb 30), uses last day of month
     */
    fun getInterestDueDateForMonth(year: Int, month: Int): Long? {
        val income = getMonthlyIncomeForMonth(year, month)
        if (income <= 0) return null
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDate
        }
        val purchaseDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        val paymentCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
            // Try to use the same day as purchase, but if it doesn't exist, use last day
            val lastDayOfMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(purchaseDay, lastDayOfMonth))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return paymentCalendar.timeInMillis
    }
    
    /**
     * Get the day of month when interest is paid (for monthly payments)
     */
    fun getPaymentDayOfMonth(): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDate
        }
        return calendar.get(Calendar.DAY_OF_MONTH)
    }
}

/**
 * Interest payment frequency
 */
enum class InterestFrequency(val displayName: String) {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    ANNUALLY("Annually"),
    AT_MATURITY("At Maturity")
}

/**
 * Certificate status
 */
enum class CertificateStatus(val displayName: String) {
    ACTIVE("Active"),
    MATURED("Matured"),
    RENEWED("Renewed"),
    WITHDRAWN("Withdrawn")
}

/**
 * Monthly income from certificates
 */
data class MonthlyCertificateIncome(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val certificates: List<CertificateIncomeDetail>
)

/**
 * Income detail for a specific certificate in a month
 */
data class CertificateIncomeDetail(
    val certificateId: String,
    val certificateNumber: String = "",
    val bankName: String,
    val amount: Double,
    val dueDate: Long
)

/**
 * Egyptian banks list
 */
object EgyptianBanks {
    val banks = listOf(
        "HSBC",
        "Commercial International Bank (CIB)",
        "National Bank of Egypt (NBE)",
        "Banque Misr",
        "Qatar National Bank Alahly (QNB)",
        "Arab African International Bank (AAIB)",
        "Credit Agricole Egypt",
        "Faisal Islamic Bank",
        "Abu Dhabi Islamic Bank (ADIB)",
        "Export Development Bank",
        "Housing & Development Bank",
        "Bank of Alexandria",
        "Suez Canal Bank",
        "Al Baraka Bank",
        "Al Ahli Bank of Kuwait (ABK)",
        "Other"
    )
}


