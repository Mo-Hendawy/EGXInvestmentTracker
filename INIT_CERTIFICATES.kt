/**
 * Certificate Initialization Code
 * 
 * INSTRUCTIONS:
 * 1. Open the PDF "statements (1).pdf" and manually extract certificate details
 * 2. Fill in the certificate data below
 * 3. Copy this code into PortfolioViewModel or create a one-time initialization function
 * 4. Run the app once to import, then remove this code
 * 
 * Format for each certificate:
 * - Bank Name (from EgyptianBanks list)
 * - Principal Amount (e.g., 100000.0)
 * - Duration in Years (e.g., 3)
 * - Annual Interest Rate % (e.g., 20.0)
 * - Purchase Date (year, month, day)
 */

package com.egx.portfoliotracker.viewmodel

import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateStatus
import com.egx.portfoliotracker.data.model.InterestFrequency
import java.util.Calendar

/**
 * Helper function to create a date timestamp
 */
fun getDateTimestamp(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Calendar months are 0-based
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

/**
 * Initialize certificates from statement
 * Replace the example data below with your actual certificate information
 */
suspend fun initializeCertificatesFromStatement(viewModel: PortfolioViewModel) {
    val certificates = listOf(
        // TODO: Replace these examples with your actual certificate data from the PDF
        
        // Example Certificate 1 - REPLACE WITH YOUR DATA
        Certificate(
            bankName = "Commercial International Bank (CIB)", // Change this
            principalAmount = 100000.0, // Change this
            durationYears = 3, // Change this
            annualInterestRate = 20.0, // Change this
            purchaseDate = getDateTimestamp(2024, 1, 15), // Change this (year, month, day)
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "Imported from statement"
        ),
        
        // Example Certificate 2 - ADD MORE AS NEEDED
        /*
        Certificate(
            bankName = "National Bank of Egypt (NBE)",
            principalAmount = 50000.0,
            durationYears = 2,
            annualInterestRate = 18.5,
            purchaseDate = getDateTimestamp(2024, 3, 10),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "Imported from statement"
        ),
        */
        
        // Add more certificates here...
    )
    
    viewModel.importCertificates(certificates)
}


