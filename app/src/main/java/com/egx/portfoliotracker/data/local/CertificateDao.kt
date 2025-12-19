package com.egx.portfoliotracker.data.local

import androidx.room.*
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    
    @Query("SELECT * FROM certificates ORDER BY purchaseDate DESC")
    fun getAllCertificates(): Flow<List<Certificate>>
    
    @Query("SELECT * FROM certificates WHERE id = :id")
    suspend fun getCertificateById(id: String): Certificate?
    
    @Query("SELECT * FROM certificates WHERE status = :status ORDER BY purchaseDate DESC")
    fun getCertificatesByStatus(status: CertificateStatus): Flow<List<Certificate>>
    
    @Query("SELECT * FROM certificates WHERE bankName = :bankName ORDER BY purchaseDate DESC")
    fun getCertificatesByBank(bankName: String): Flow<List<Certificate>>
    
    @Query("SELECT SUM(principalAmount + (principalAmount * annualInterestRate / 100 * (CAST((julianday('now') - julianday(datetime(purchaseDate/1000, 'unixepoch'))) AS REAL) / 365.0))) FROM certificates WHERE status = :status")
    fun getTotalCertificatesValue(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Double?>
    
    @Query("SELECT SUM((principalAmount * annualInterestRate / 100) / 12) FROM certificates WHERE status = :status")
    fun getTotalMonthlyIncome(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Double?>
    
    @Query("SELECT COUNT(*) FROM certificates WHERE status = :status")
    fun getCertificatesCount(status: CertificateStatus = CertificateStatus.ACTIVE): Flow<Int>
    
    @Query("SELECT * FROM certificates WHERE status = :status")
    suspend fun getCertificatesByStatusSync(status: CertificateStatus): List<Certificate>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: Certificate)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificates(certificates: List<Certificate>)
    
    @Update
    suspend fun updateCertificate(certificate: Certificate)
    
    @Delete
    suspend fun deleteCertificate(certificate: Certificate)
    
    @Query("DELETE FROM certificates WHERE id = :id")
    suspend fun deleteCertificateById(id: String)
    
    @Query("DELETE FROM certificates")
    suspend fun deleteAllCertificates()
    
    @Query("UPDATE certificates SET bankName = :newBankName WHERE bankName = :oldBankName")
    suspend fun updateBankName(oldBankName: String, newBankName: String)
}


