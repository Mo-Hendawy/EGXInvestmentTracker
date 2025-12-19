# Expenses & Bank Certificates Feature - Implementation Plan

## 📋 Overview
Add two new major sections:
1. **Expenses Tracker** - Track daily/monthly spending
2. **Bank Saving Certificates** - Track certificates with monthly interest calculations and income tracking

---

## 🎯 Feature 1: Expenses Tracker

### Core Features:
1. **Add/Edit/Delete Expenses**
   - Date (daily tracking)
   - Category (Food, Transport, Bills, Shopping, Entertainment, etc.)
   - Amount
   - Description/Notes
   - Payment method (Cash, Card, Bank Transfer, etc.)

2. **Expense Categories**
   - Food & Dining
   - Transportation
   - Bills & Utilities
   - Shopping
   - Entertainment
   - Healthcare
   - Education
   - Personal Care
   - Other

3. **Monthly View**
   - Total monthly expenses
   - Expenses by category (pie chart)
   - Daily spending breakdown
   - Average daily spending
   - Comparison with previous months

4. **Analytics**
   - Monthly spending trends
   - Category-wise spending
   - Budget vs actual (if budget set)
   - Spending patterns

---

## 🎯 Feature 2: Bank Saving Certificates (Enhanced)

### Core Features:
1. **Add Certificate**
   - Bank name
   - Principal amount (e.g., 100,000 EGP)
   - Duration (years, e.g., 3 years)
   - Annual interest rate (e.g., 20%)
   - Purchase date
   - **Auto-calculate**: Maturity date, monthly interest, total interest

2. **Automatic Calculations**
   - **Maturity Date**: Purchase date + duration
   - **Monthly Interest**: (Principal × Annual Rate / 100) / 12
   - **Current Accrued Interest**: Based on days since purchase
   - **Total Interest at Maturity**: Principal × Rate × Years
   - **Current Value**: Principal + Accrued Interest

3. **Monthly Income Tracking**
   - Show monthly interest income from each certificate
   - Sum all monthly certificate incomes
   - Calendar view of monthly due dates
   - Track which months have interest payments

4. **Certificate Summary**
   - Total certificates value
   - Total monthly income from all certificates
   - Upcoming interest payment dates
   - Maturity timeline

---

## 🗂️ Data Models

### Expense Entity
```kotlin
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,                       // Expense date
    val category: ExpenseCategory,         // Category
    val amount: Double,                    // Amount spent
    val description: String = "",          // Description/notes
    val paymentMethod: PaymentMethod,      // How paid
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExpenseCategory(val displayName: String, val icon: String) {
    FOOD("Food & Dining", "restaurant"),
    TRANSPORT("Transportation", "directions_car"),
    BILLS("Bills & Utilities", "receipt"),
    SHOPPING("Shopping", "shopping_bag"),
    ENTERTAINMENT("Entertainment", "movie"),
    HEALTHCARE("Healthcare", "local_hospital"),
    EDUCATION("Education", "school"),
    PERSONAL_CARE("Personal Care", "spa"),
    OTHER("Other", "category")
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_WALLET("Mobile Wallet"),
    OTHER("Other")
}
```

### Certificate Entity (Enhanced)
```kotlin
@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bankName: String,                  // Bank name
    val principalAmount: Double,            // e.g., 100,000
    val durationYears: Int,                // e.g., 3 years
    val annualInterestRate: Double,        // e.g., 20%
    val purchaseDate: Long,                 // Purchase date
    val interestPaymentFrequency: InterestFrequency, // Monthly, Quarterly, etc.
    val status: CertificateStatus,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Auto-calculated properties
    val maturityDate: Long
        get() = purchaseDate + (durationYears * 365L * 24 * 60 * 60 * 1000)
    
    val monthlyInterest: Double
        get() = (principalAmount * annualInterestRate / 100) / 12
    
    val totalInterestAtMaturity: Double
        get() = principalAmount * (annualInterestRate / 100) * durationYears
    
    val daysSincePurchase: Long
        get() = (System.currentTimeMillis() - purchaseDate) / (1000 * 60 * 60 * 24)
    
    val accruedInterest: Double
        get() = principalAmount * (annualInterestRate / 100) * (daysSincePurchase / 365.0)
    
    val currentValue: Double
        get() = principalAmount + accruedInterest
    
    val daysUntilMaturity: Long
        get() = maxOf(0, (maturityDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24))
    
    val isMatured: Boolean
        get() = System.currentTimeMillis() >= maturityDate
    
    // Get monthly income for a specific month
    fun getMonthlyIncomeForMonth(year: Int, month: Int): Double {
        // Check if this month should have an interest payment based on frequency
        return when (interestPaymentFrequency) {
            InterestFrequency.MONTHLY -> monthlyInterest
            InterestFrequency.QUARTERLY -> if (month % 3 == 0) monthlyInterest * 3 else 0.0
            InterestFrequency.ANNUALLY -> if (month == getPurchaseMonth()) monthlyInterest * 12 else 0.0
            InterestFrequency.AT_MATURITY -> 0.0 // Only at maturity
        }
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

### Monthly Income Summary
```kotlin
data class MonthlyCertificateIncome(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val certificates: List<CertificateIncomeDetail>
)

data class CertificateIncomeDetail(
    val certificateId: String,
    val bankName: String,
    val amount: Double,
    val dueDate: Long
)
```

---

## 🗄️ Database Changes

### New Tables:
1. **expenses** table
2. **certificates** table

### Migration: Version 4 → 5
- Add both tables
- No data loss

---

## 📁 File Structure

### New Files:
1. `data/model/Expense.kt` - Expense entity
2. `data/model/Certificate.kt` - Certificate entity
3. `data/local/ExpenseDao.kt` - Expense database access
4. `data/local/CertificateDao.kt` - Certificate database access
5. `ui/screens/expenses/ExpensesScreen.kt` - Expenses list
6. `ui/screens/expenses/AddExpenseScreen.kt` - Add expense form
7. `ui/screens/expenses/ExpenseDetailScreen.kt` - Expense detail
8. `ui/screens/certificates/CertificatesScreen.kt` - Certificates list
9. `ui/screens/certificates/AddCertificateScreen.kt` - Add certificate form
10. `ui/screens/certificates/CertificateDetailScreen.kt` - Certificate detail
11. `ui/components/ExpenseCard.kt` - Expense card
12. `ui/components/CertificateCard.kt` - Certificate card
13. `ui/components/MonthlyIncomeCalendar.kt` - Monthly income calendar

### Modified Files:
1. `data/local/PortfolioDatabase.kt` - Add entities, migration
2. `data/repository/PortfolioRepository.kt` - Add CRUD methods
3. `viewmodel/PortfolioViewModel.kt` - Add state flows
4. `data/model/Holding.kt` - Update PortfolioSummary
5. `navigation/Navigation.kt` - Add new routes
6. `ui/screens/dashboard/DashboardScreen.kt` - Add sections
7. `di/AppModule.kt` - Add DAO providers

---

## 🔧 Implementation Steps

### Phase 1: Data Layer (Certificates)
1. Create `Certificate.kt` with auto-calculation logic
2. Create `CertificateDao.kt`
3. Update database (migration 4→5)
4. Add to repository

### Phase 2: Data Layer (Expenses)
1. Create `Expense.kt`
2. Create `ExpenseDao.kt`
3. Update database
4. Add to repository

### Phase 3: Business Logic
1. Certificate calculations (monthly income, maturity dates)
2. Monthly income aggregation
3. Expense aggregation (monthly totals, by category)
4. Update PortfolioSummary

### Phase 4: UI - Certificates
1. Certificates list screen
2. Add certificate form (with auto-calculations)
3. Certificate detail screen
4. Monthly income calendar view

### Phase 5: UI - Expenses
1. Expenses list screen
2. Add expense form
3. Monthly expenses view
4. Category breakdown charts

### Phase 6: Integration
1. Add tabs to main navigation
2. Dashboard integration
3. Portfolio summary updates

---

## 📊 UI Design

### Certificates Tab:
**List View:**
- FAB to add certificate
- Cards showing:
  - Bank name
  - Principal amount
  - Interest rate & duration
  - Current value
  - Monthly income
  - Days until maturity
  - Status badge

**Add Certificate Form:**
- Bank dropdown
- Principal amount input
- Duration (years) input
- Annual interest rate input
- Purchase date picker
- Interest payment frequency dropdown
- **Auto-calculated preview:**
  - Maturity date (auto-filled, editable)
  - Monthly interest (displayed)
  - Total interest at maturity (displayed)

**Monthly Income View:**
- Calendar/List showing each month
- For each month:
  - Total income from all certificates
  - Breakdown by certificate
  - Due dates highlighted
  - Amount per certificate

**Certificate Detail:**
- Key metrics
- Monthly income timeline
- Maturity countdown
- Interest accrual graph

### Expenses Tab:
**Daily View:**
- Date picker
- List of expenses for selected date
- Total for the day
- FAB to add expense

**Monthly View:**
- Month selector
- Total monthly expenses
- Category breakdown (pie chart)
- Daily spending bar chart
- List of all expenses

**Add Expense:**
- Date picker
- Category dropdown
- Amount input
- Description
- Payment method
- Save button

---

## 🧮 Certificate Calculations

### Example: 100,000 EGP for 3 years at 20% yearly

**Input:**
- Principal: 100,000 EGP
- Duration: 3 years
- Rate: 20% annual
- Purchase Date: Jan 1, 2024

**Auto-calculated:**
- Maturity Date: Jan 1, 2027
- Monthly Interest: (100,000 × 20% / 100) / 12 = 1,666.67 EGP/month
- Total Interest at Maturity: 100,000 × 20% × 3 = 60,000 EGP
- Current Value (if 6 months passed): 100,000 + (100,000 × 20% × 0.5) = 110,000 EGP

**Monthly Income Tracking:**
- Jan 2024: 1,666.67 EGP (due date: Jan 31)
- Feb 2024: 1,666.67 EGP (due date: Feb 29)
- Mar 2024: 1,666.67 EGP (due date: Mar 31)
- ... and so on

---

## 📈 Monthly Income Summary

### Data Structure:
```kotlin
data class MonthlyIncomeSummary(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val certificates: List<CertificateIncome>
)

data class CertificateIncome(
    val certificateId: String,
    val bankName: String,
    val certificateName: String,
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false
)
```

### Features:
- Show all months with certificate income
- Highlight months with upcoming payments
- Show total monthly income
- Show breakdown per certificate
- Mark payments as received

---

## 🎨 UI/UX

### Navigation:
- Bottom Navigation or Tabs:
  1. Dashboard
  2. Portfolio (Stocks)
  3. Certificates (NEW)
  4. Expenses (NEW)

### Color Scheme:
- Stocks: Gold/Yellow
- Certificates: Blue/Purple
- Expenses: Red/Orange

### Icons:
- Certificates: AccountBalance, Savings
- Expenses: Receipt, ShoppingCart

---

## 📝 Key Requirements Summary

### Certificates:
✅ Add certificate: 100k, 3 years, 20% yearly
✅ Auto-calculate: monthly interest, end date, current accrued
✅ Sum all certificates
✅ Show monthly certificate incomes
✅ Show monthly due dates for each certificate/month and amount

### Expenses:
✅ Track daily/monthly expenses
✅ Categories
✅ Monthly summaries
✅ Charts and analytics

---

## 🚀 Ready to Start?

**Implementation Order:**
1. Certificates data layer (Phase 1)
2. Certificates UI (Phase 4)
3. Expenses data layer (Phase 2)
4. Expenses UI (Phase 5)
5. Integration (Phase 6)

**Should I start with Certificates first, or do both in parallel?**


