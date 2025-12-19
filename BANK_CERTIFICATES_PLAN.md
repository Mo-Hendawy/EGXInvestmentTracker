# Bank Saving Certificates Feature - Implementation Plan

## 📋 Overview
Add a new section to track Egyptian bank saving certificates (شهادات ادخار) alongside stock holdings. This will allow users to track their fixed deposits, certificates of deposit, and other bank savings instruments.

---

## 🎯 Features to Implement

### Core Features:
1. **Add/Edit/Delete Certificates**
   - Bank name (dropdown or searchable list)
   - Certificate type/name
   - Principal amount (initial investment)
   - Interest rate (annual %)
   - Purchase date
   - Maturity date
   - Interest payment frequency (monthly, quarterly, annually, at maturity)
   - Status (Active, Matured, Renewed, Withdrawn)

2. **Automatic Calculations**
   - Current value (principal + accrued interest)
   - Accrued interest (calculated based on days since purchase)
   - Remaining days until maturity
   - Total interest earned (if matured or withdrawn)
   - Effective yield

3. **Portfolio Integration**
   - Include certificates in total portfolio value
   - Show certificates in portfolio summary
   - Separate section/tab for certificates
   - Combined view (stocks + certificates)

4. **Visualization**
   - List view of all certificates
   - Detail view for each certificate
   - Maturity timeline/calendar
   - Interest accrual graph
   - Bank allocation chart

---

## 🗂️ Data Model Design

### Certificate Entity
```kotlin
@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bankName: String,                    // e.g., "CIB", "NBE", "QNB"
    val certificateName: String,              // e.g., "3-Year Certificate"
    val principalAmount: Double,              // Initial investment
    val interestRate: Double,                 // Annual interest rate %
    val purchaseDate: Long,                   // When purchased
    val maturityDate: Long,                   // When it matures
    val interestPaymentFrequency: InterestFrequency, // How often interest is paid
    val status: CertificateStatus,            // Active, Matured, etc.
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Calculated properties
    val daysUntilMaturity: Long
        get() = maxOf(0, (maturityDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24))
    
    val isMatured: Boolean
        get() = System.currentTimeMillis() >= maturityDate
    
    val accruedInterest: Double
        get() = calculateAccruedInterest()
    
    val currentValue: Double
        get() = principalAmount + accruedInterest
    
    val totalInterestEarned: Double
        get() = if (isMatured) calculateTotalInterest() else accruedInterest
    
    private fun calculateAccruedInterest(): Double {
        // Calculate based on days since purchase and interest rate
        val daysSincePurchase = (System.currentTimeMillis() - purchaseDate) / (1000 * 60 * 60 * 24)
        val years = daysSincePurchase / 365.0
        return principalAmount * (interestRate / 100) * years
    }
    
    private fun calculateTotalInterest(): Double {
        val daysHeld = (maturityDate - purchaseDate) / (1000 * 60 * 60 * 24)
        val years = daysHeld / 365.0
        return principalAmount * (interestRate / 100) * years
    }
}

enum class InterestFrequency(val displayName: String) {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    ANNUALLY("Annually"),
    AT_MATURITY("At Maturity")
}

enum class CertificateStatus(val displayName: String) {
    ACTIVE("Active"),
    MATURED("Matured"),
    RENEWED("Renewed"),
    WITHDRAWN("Withdrawn")
}
```

### Egyptian Banks List
```kotlin
object EgyptianBanks {
    val banks = listOf(
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
```

---

## 🗄️ Database Changes

### New Table: `certificates`
```sql
CREATE TABLE certificates (
    id TEXT PRIMARY KEY NOT NULL,
    bankName TEXT NOT NULL,
    certificateName TEXT NOT NULL,
    principalAmount REAL NOT NULL,
    interestRate REAL NOT NULL,
    purchaseDate INTEGER NOT NULL,
    maturityDate INTEGER NOT NULL,
    interestPaymentFrequency TEXT NOT NULL,
    status TEXT NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
)
```

### Database Migration (Version 4 → 5)
- Add `certificates` table
- Update `PortfolioSummary` to include certificates
- No data loss (additive change)

---

## 📁 File Structure

### New Files:
1. `data/model/Certificate.kt` - Certificate entity and enums
2. `data/local/CertificateDao.kt` - Database access for certificates
3. `ui/screens/certificates/CertificatesScreen.kt` - List view
4. `ui/screens/certificates/AddCertificateScreen.kt` - Add/Edit form
5. `ui/screens/certificates/CertificateDetailScreen.kt` - Detail view
6. `ui/components/CertificateCard.kt` - Certificate card component
7. `ui/components/CertificateChart.kt` - Interest accrual chart

### Modified Files:
1. `data/local/PortfolioDatabase.kt` - Add Certificate entity, migration
2. `data/repository/PortfolioRepository.kt` - Add certificate CRUD methods
3. `viewmodel/PortfolioViewModel.kt` - Add certificate state flows
4. `data/model/Holding.kt` - Update PortfolioSummary to include certificates
5. `ui/screens/dashboard/DashboardScreen.kt` - Add certificates section
6. `ui/screens/portfolio/PortfolioScreen.kt` - Add certificates tab
7. `di/AppModule.kt` - Add CertificateDao provider
8. `navigation/Navigation.kt` - Add certificate routes

---

## 🔧 Implementation Steps

### Phase 1: Data Layer
1. ✅ Create `Certificate.kt` data model
2. ✅ Create `CertificateDao.kt` with CRUD operations
3. ✅ Update `PortfolioDatabase.kt`:
   - Add Certificate to entities
   - Increment version to 5
   - Create MIGRATION_4_5
4. ✅ Update `AppModule.kt` to provide CertificateDao
5. ✅ Add certificate methods to `PortfolioRepository.kt`

### Phase 2: Business Logic
1. ✅ Update `PortfolioSummary` to include:
   - `totalCertificatesValue: Double`
   - `totalCertificatesCount: Int`
   - `certificatesInterestEarned: Double`
2. ✅ Update portfolio summary calculation to include certificates
3. ✅ Add certificate state flows to `PortfolioViewModel.kt`
4. ✅ Implement interest calculation logic

### Phase 3: UI Components
1. ✅ Create `CertificateCard.kt` component
2. ✅ Create `CertificatesScreen.kt` (list view)
3. ✅ Create `AddCertificateScreen.kt` (form)
4. ✅ Create `CertificateDetailScreen.kt` (detail view)
5. ✅ Add certificates section to Dashboard
6. ✅ Add certificates tab to Portfolio screen

### Phase 4: Integration
1. ✅ Update navigation to include certificate routes
2. ✅ Integrate certificates into portfolio summary
3. ✅ Update portfolio allocation charts
4. ✅ Add maturity notifications/alerts (future)

---

## 📊 UI Design

### Certificates List Screen:
- Floating Action Button to add new certificate
- Filter by: Bank, Status, Maturity date
- Sort by: Value, Maturity date, Interest rate
- Search by bank name or certificate name
- Cards showing:
  - Bank name
  - Certificate name
  - Principal amount
  - Current value
  - Interest rate
  - Days until maturity
  - Status badge

### Add/Edit Certificate Screen:
- Bank dropdown/search
- Certificate name field
- Principal amount field
- Interest rate field
- Purchase date picker
- Maturity date picker
- Interest payment frequency dropdown
- Status dropdown
- Notes field
- Preview of calculated values

### Certificate Detail Screen:
- Header with bank and certificate name
- Key metrics card:
  - Principal amount
  - Current value
  - Accrued interest
  - Interest rate
  - Days until maturity
- Timeline showing purchase → maturity
- Interest accrual graph
- History of interest payments (if tracked)
- Actions: Edit, Renew, Withdraw, Delete

### Dashboard Integration:
- New section: "Bank Certificates"
- Summary card showing:
  - Total certificates value
  - Number of active certificates
  - Total interest earned
  - Upcoming maturities (next 30 days)

---

## 🧮 Interest Calculation Logic

### Simple Interest (Most Common):
```
Accrued Interest = Principal × (Interest Rate / 100) × (Days Since Purchase / 365)
Current Value = Principal + Accrued Interest
```

### Compound Interest (if applicable):
```
Current Value = Principal × (1 + (Interest Rate / 100) / n)^(n × years)
where n = compounding frequency per year
```

### Interest Payment Frequency:
- **Monthly**: Interest paid monthly, principal remains
- **Quarterly**: Interest paid every 3 months
- **Annually**: Interest paid once per year
- **At Maturity**: All interest paid when certificate matures

---

## 📈 Portfolio Summary Updates

### Updated PortfolioSummary:
```kotlin
data class PortfolioSummary(
    // Existing fields...
    val totalValue: Double,              // Stocks + Certificates
    val totalCost: Double,               // Stocks cost + Certificates principal
    val totalProfitLoss: Double,          // Stocks P/L + Certificates interest
    val totalProfitLossPercent: Double,
    val holdingsCount: Int,
    val certificatesCount: Int,          // NEW
    val totalCertificatesValue: Double,   // NEW
    val totalCertificatesInterest: Double, // NEW
    // ... rest
)
```

---

## 🎨 UI/UX Considerations

1. **Color Scheme**: Use a different color (e.g., blue/purple) to distinguish from stocks (gold)
2. **Icons**: Use bank/money icons for certificates
3. **Maturity Alerts**: Visual indicators for certificates maturing soon
4. **Status Badges**: Color-coded status indicators
5. **Charts**: Interest accrual over time, maturity timeline

---

## 🔄 Future Enhancements (Post-MVP)

1. **Interest Payment Tracking**: Record when interest is paid
2. **Renewal Tracking**: Track certificate renewals
3. **Maturity Notifications**: Push notifications for upcoming maturities
4. **Bank Comparison**: Compare rates across banks
5. **Certificate Templates**: Save common certificate types
6. **Export/Import**: Export certificate data
7. **Tax Calculation**: Calculate tax on interest (if applicable)

---

## ⚠️ Important Notes

1. **Interest Calculation**: Confirm if Egyptian banks use simple or compound interest
2. **Tax Considerations**: May need to account for tax on interest
3. **Currency**: All amounts in EGP
4. **Date Handling**: Use proper date handling for maturity calculations
5. **Data Migration**: Ensure existing users' data is preserved

---

## 📝 Testing Checklist

- [ ] Add new certificate
- [ ] Edit existing certificate
- [ ] Delete certificate
- [ ] Interest calculation accuracy
- [ ] Maturity date calculations
- [ ] Portfolio summary includes certificates
- [ ] Certificates appear in dashboard
- [ ] Filter and sort certificates
- [ ] Search certificates
- [ ] Certificate detail view
- [ ] Status changes (Active → Matured)
- [ ] Database migration (v4 → v5)

---

## 🚀 Implementation Order

1. **Start with Data Layer** (Phase 1)
2. **Test with simple UI** (basic list view)
3. **Add Business Logic** (Phase 2)
4. **Build Full UI** (Phase 3)
5. **Integrate Everything** (Phase 4)
6. **Test & Refine**

---

**Ready to start implementation? Let me know and I'll begin with Phase 1!**


