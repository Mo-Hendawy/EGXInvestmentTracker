/**
 * Certificate Initialization Code - EXTRACTED FROM NOTEBOOK
 * 
 * Total: 20 certificates
 * All certificates: 3 years duration, Monthly interest payment
 * 
 * IMPORTANT: Replace "Commercial International Bank (CIB)" with the actual bank name
 * or specify different banks for each certificate if they differ.
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
 * Initialize all 20 certificates from notebook
 * TODO: Replace "Commercial International Bank (CIB)" with actual bank name(s)
 */
suspend fun initializeCertificatesFromNotebook(viewModel: PortfolioViewModel) {
    val certificates = listOf(
        // 2024 Certificates
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 300000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 7, 17),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 235"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 7, 24),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 236"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 8, 4),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 237"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 8, 29),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 238"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 80000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 9, 3),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 239"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 9, 26),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 900"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 80000.0,
            durationYears = 3,
            annualInterestRate = 22.0,
            purchaseDate = getDateTimestamp(2024, 10, 7),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 901"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2024, 10, 30),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 902"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 80000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2024, 11, 5),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 903"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2024, 12, 5),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 904"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 120000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2024, 12, 16),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 905"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2024, 12, 26),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 906"
        ),
        
        // 2025 Certificates
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 100000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 1, 8),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 907"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 70000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 1, 27),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 908"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 100000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 2, 5),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 909"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 60000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 2, 27),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 910"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 450000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 3, 9),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 911"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 120000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 3, 27),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 912"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 210000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 7, 6),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 913"
        ),
        Certificate(
            bankName = "Commercial International Bank (CIB)", // TODO: Change if different
            principalAmount = 80000.0,
            durationYears = 3,
            annualInterestRate = 20.5,
            purchaseDate = getDateTimestamp(2025, 9, 15),
            interestPaymentFrequency = InterestFrequency.MONTHLY,
            status = CertificateStatus.ACTIVE,
            notes = "ID: 914"
        )
    )
    
    viewModel.importCertificates(certificates)
}


