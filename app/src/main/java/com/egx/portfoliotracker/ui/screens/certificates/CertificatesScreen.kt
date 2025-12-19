package com.egx.portfoliotracker.ui.screens.certificates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateStatus
import com.egx.portfoliotracker.data.model.MonthlyCertificateIncome
import com.egx.portfoliotracker.ui.components.BlurredAmountNoDecimals
import com.egx.portfoliotracker.ui.components.CertificateCard
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CertificateSortOption(val displayName: String) {
    PAYMENT_DAY("Payment Day"),
    MATURITY_DATE("Maturity Date ↑"),
    MATURITY_DATE_DESC("Maturity Date ↓"),
    PURCHASE_DATE("Purchase Date ↑"),
    PURCHASE_DATE_DESC("Purchase Date ↓"),
    VALUE("Value ↑"),
    VALUE_DESC("Value ↓"),
    PRINCIPAL("Principal ↑"),
    PRINCIPAL_DESC("Principal ↓"),
    RATE("Rate ↑"),
    RATE_DESC("Rate ↓")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCertificate: () -> Unit,
    onNavigateToCertificateDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val certificates by viewModel.certificates.collectAsState()
    val totalValue by viewModel.totalCertificatesValue.collectAsState()
    val totalMonthlyIncome by viewModel.totalMonthlyIncome.collectAsState()
    val isAmountsBlurred by viewModel.isAmountsBlurred.collectAsState()
    
    var selectedStatus by remember { mutableStateOf<CertificateStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showMonthlyIncome by remember { mutableStateOf(false) }
    var showDailySchedule by remember { mutableStateOf(false) }
    var monthlyIncomes by remember { mutableStateOf<List<MonthlyCertificateIncome>>(emptyList()) }
    var sortOption by remember { mutableStateOf(CertificateSortOption.PAYMENT_DAY) }
    val scope = rememberCoroutineScope()
    
    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    
    // Filter and sort certificates
    val filteredCertificates = remember(certificates, selectedStatus, searchQuery, sortOption) {
        certificates
            .filter { cert ->
                (selectedStatus == null || cert.status == selectedStatus) &&
                (searchQuery.isEmpty() || 
                    cert.bankName.contains(searchQuery, ignoreCase = true) ||
                    cert.certificateNumber.contains(searchQuery, ignoreCase = true))
            }
            .let { list ->
                when (sortOption) {
                    CertificateSortOption.PAYMENT_DAY -> list.sortedBy { it.getPaymentDayOfMonth() }
                    CertificateSortOption.MATURITY_DATE -> list.sortedBy { it.maturityDate }
                    CertificateSortOption.MATURITY_DATE_DESC -> list.sortedByDescending { it.maturityDate }
                    CertificateSortOption.PURCHASE_DATE -> list.sortedBy { it.purchaseDate }
                    CertificateSortOption.PURCHASE_DATE_DESC -> list.sortedByDescending { it.purchaseDate }
                    CertificateSortOption.VALUE -> list.sortedBy { it.currentValue }
                    CertificateSortOption.VALUE_DESC -> list.sortedByDescending { it.currentValue }
                    CertificateSortOption.PRINCIPAL -> list.sortedBy { it.principalAmount }
                    CertificateSortOption.PRINCIPAL_DESC -> list.sortedByDescending { it.principalAmount }
                    CertificateSortOption.RATE -> list.sortedBy { it.annualInterestRate }
                    CertificateSortOption.RATE_DESC -> list.sortedByDescending { it.annualInterestRate }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Certificates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Import certificates button (only show if no certificates exist)
                    if (certificates.isEmpty()) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    viewModel.initializeCertificatesFromNotebook()
                                }
                            }
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Import Certificates")
                        }
                    }
                    // Monthly income toggle
                    IconButton(
                        onClick = {
                            showMonthlyIncome = !showMonthlyIncome
                            if (showMonthlyIncome) {
                                scope.launch {
                                    // Get only current month
                                    val currentMonthIncome = viewModel.getMonthlyCertificateIncome(currentYear, currentMonth)
                                    monthlyIncomes = listOf(currentMonthIncome)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (showMonthlyIncome) Icons.Default.CalendarMonth else Icons.Default.CalendarToday,
                            contentDescription = "Monthly Income",
                            tint = if (showMonthlyIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Privacy/Blur toggle
                    IconButton(
                        onClick = { viewModel.toggleAmountsBlur() }
                    ) {
                        Icon(
                            imageVector = if (isAmountsBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isAmountsBlurred) "Show amounts" else "Hide amounts",
                            tint = if (isAmountsBlurred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToAddCertificate) {
                        Icon(Icons.Default.Add, contentDescription = "Add Certificate")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddCertificate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Certificate")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary card
            if (certificates.isNotEmpty()) {
                item {
                    val totalPrincipal = remember(certificates) {
                        certificates.sumOf { it.principalAmount }
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total Principal",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    BlurredAmountNoDecimals(
                                        amount = totalPrincipal,
                                        currency = "EGP",
                                        isBlurred = isAmountsBlurred,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total Value",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    BlurredAmountNoDecimals(
                                        amount = totalValue ?: 0.0,
                                        currency = "EGP",
                                        isBlurred = isAmountsBlurred,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Monthly Income",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    BlurredAmountNoDecimals(
                                        amount = totalMonthlyIncome ?: 0.0,
                                        currency = "EGP",
                                        isBlurred = isAmountsBlurred,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Certificates",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "${certificates.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Calendar View Section
            if (showDailySchedule) {
                item {
                    var selectedYear by remember { mutableStateOf(currentYear) }
                    var selectedMonth by remember { mutableStateOf(currentMonth) }
                    
                    com.egx.portfoliotracker.ui.components.CertificateCalendarView(
                        certificates = certificates,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        onMonthChange = { year, month ->
                            selectedYear = year
                            selectedMonth = month
                        },
                        isBlurred = isAmountsBlurred
                    )
                }
            }
            
            // Monthly Income Section
            if (showMonthlyIncome && monthlyIncomes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Monthly Certificate Income",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            monthlyIncomes.forEach { monthlyIncome ->
                                if (monthlyIncome.totalIncome > 0) {
                                    MonthlyIncomeItem(
                                        monthlyIncome = monthlyIncome,
                                        isBlurred = isAmountsBlurred
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // Filter chips and sort
            if (certificates.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { selectedStatus = null },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = selectedStatus == CertificateStatus.ACTIVE,
                                onClick = { selectedStatus = CertificateStatus.ACTIVE },
                                label = { Text("Active") }
                            )
                            FilterChip(
                                selected = selectedStatus == CertificateStatus.MATURED,
                                onClick = { selectedStatus = CertificateStatus.MATURED },
                                label = { Text("Matured") }
                            )
                        }
                        
                        // Sort dropdown
                        var showSortDropdown by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = showSortDropdown,
                            onExpandedChange = { showSortDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = sortOption.displayName,
                                onValueChange = {},
                                label = { Text("Sort by") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSortDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showSortDropdown,
                                onDismissRequest = { showSortDropdown = false }
                            ) {
                                CertificateSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName) },
                                        onClick = {
                                            sortOption = option
                                            showSortDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Certificates list
            if (filteredCertificates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (certificates.isEmpty()) "No certificates yet" else "No certificates found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (certificates.isEmpty()) "Add your first certificate" else "Try adjusting your filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredCertificates, key = { it.id }) { certificate ->
                    CertificateCard(
                        certificate = certificate,
                        onClick = { onNavigateToCertificateDetail(certificate.id) },
                        isBlurred = isAmountsBlurred
                    )
                }
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DailyCreditItem(
    dailyCredit: com.egx.portfoliotracker.viewmodel.DailyCredit,
    isBlurred: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Day ${dailyCredit.day}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${dailyCredit.certificates.size} certificate${if (dailyCredit.certificates.size > 1) "s" else ""})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BlurredAmountNoDecimals(
                    amount = dailyCredit.totalAmount,
                    currency = "EGP",
                    isBlurred = isBlurred,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (dailyCredit.certificates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                dailyCredit.certificates.forEach { certIncome ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = certIncome.bankName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isBlurred) "••••" else "EGP ${String.format("%,.2f", certIncome.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyIncomeItem(
    monthlyIncome: MonthlyCertificateIncome,
    isBlurred: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance().apply {
        set(Calendar.YEAR, monthlyIncome.year)
        set(Calendar.MONTH, monthlyIncome.month - 1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    } }
    val monthName = dateFormat.format(calendar.time)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                BlurredAmountNoDecimals(
                    amount = monthlyIncome.totalIncome,
                    currency = "EGP",
                    isBlurred = isBlurred,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (monthlyIncome.certificates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                // Sort by payment day (dueDate)
                val sortedCertificates = monthlyIncome.certificates.sortedBy { it.dueDate }
                val currentTime = System.currentTimeMillis()
                
                sortedCertificates.forEach { certIncome ->
                    val isPast = certIncome.dueDate < currentTime
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = certIncome.bankName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (certIncome.certificateNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "#${certIncome.certificateNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBlurred) "••••" else "EGP ${String.format("%,.2f", certIncome.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            if (certIncome.dueDate > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Due: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(certIncome.dueDate))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPast) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    } else {
                                        com.egx.portfoliotracker.ui.theme.ProfitGreen
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


