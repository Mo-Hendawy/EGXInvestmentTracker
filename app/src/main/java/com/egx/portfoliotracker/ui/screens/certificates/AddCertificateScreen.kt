package com.egx.portfoliotracker.ui.screens.certificates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateStatus
import com.egx.portfoliotracker.data.model.EgyptianBanks
import com.egx.portfoliotracker.data.model.InterestFrequency
import com.egx.portfoliotracker.ui.components.BlurredAmount
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCertificateScreen(
    onNavigateBack: () -> Unit,
    onCertificateAdded: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    var bankName by remember { mutableStateOf("") }
    var certificateNumber by remember { mutableStateOf("") }
    var principalAmount by remember { mutableStateOf("") }
    var durationYears by remember { mutableStateOf("") }
    var annualInterestRate by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var interestFrequency by remember { mutableStateOf(InterestFrequency.MONTHLY) }
    var notes by remember { mutableStateOf("") }
    
    var showBankDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Auto-calculated values
    val principalNum = principalAmount.toDoubleOrNull() ?: 0.0
    val durationNum = durationYears.toIntOrNull() ?: 0
    val rateNum = annualInterestRate.toDoubleOrNull() ?: 0.0
    
    val maturityDate = remember(purchaseDate, durationNum) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDate
            add(Calendar.YEAR, durationNum)
        }
        calendar.timeInMillis
    }
    
    val monthlyInterest = remember(principalNum, rateNum) {
        if (principalNum > 0 && rateNum > 0) {
            (principalNum * rateNum / 100) / 12
        } else 0.0
    }
    
    val totalInterestAtMaturity = remember(principalNum, rateNum, durationNum) {
        if (principalNum > 0 && rateNum > 0 && durationNum > 0) {
            principalNum * (rateNum / 100) * durationNum
        } else 0.0
    }
    
    val canSave = bankName.isNotBlank() && 
                 principalNum > 0 && 
                 durationNum > 0 && 
                 rateNum > 0
    
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Certificate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Certificate Number
            OutlinedTextField(
                value = certificateNumber,
                onValueChange = { certificateNumber = it },
                label = { Text("Certificate Number (optional)") },
                placeholder = { Text("e.g., 235") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) }
            )
            
            // Bank selection
            ExposedDropdownMenuBox(
                expanded = showBankDropdown,
                onExpandedChange = { showBankDropdown = !showBankDropdown }
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBankDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showBankDropdown,
                    onDismissRequest = { showBankDropdown = false }
                ) {
                    EgyptianBanks.banks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank) },
                            onClick = {
                                bankName = bank
                                showBankDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Principal amount
            OutlinedTextField(
                value = principalAmount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) principalAmount = it },
                label = { Text("Principal Amount (EGP) *") },
                placeholder = { Text("e.g., 100000") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
            )
            
            // Duration
            OutlinedTextField(
                value = durationYears,
                onValueChange = { if (it.all { char -> char.isDigit() }) durationYears = it },
                label = { Text("Duration (Years) *") },
                placeholder = { Text("e.g., 3") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
            
            // Annual interest rate
            OutlinedTextField(
                value = annualInterestRate,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) annualInterestRate = it },
                label = { Text("Annual Interest Rate (%) *") },
                placeholder = { Text("e.g., 20") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) }
            )
            
            // Purchase date
            OutlinedTextField(
                value = dateFormat.format(java.util.Date(purchaseDate)),
                onValueChange = {},
                label = { Text("Purchase Date *") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = purchaseDate
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    purchaseDate = it
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Interest payment frequency
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                var showFrequencyDropdown by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = interestFrequency.displayName,
                    onValueChange = {},
                    label = { Text("Interest Payment Frequency") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .clickable { showFrequencyDropdown = true }
                )
                ExposedDropdownMenu(
                    expanded = showFrequencyDropdown,
                    onDismissRequest = { showFrequencyDropdown = false }
                ) {
                    InterestFrequency.values().forEach { frequency ->
                        DropdownMenuItem(
                            text = { Text(frequency.displayName) },
                            onClick = {
                                interestFrequency = frequency
                                showFrequencyDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            Divider()
            
            // Auto-calculated preview
            if (canSave) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Auto-Calculated Values",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Maturity Date:")
                            Text(
                                text = dateFormat.format(java.util.Date(maturityDate)),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Interest:")
                            Text(
                                text = "EGP ${String.format("%,.2f", monthlyInterest)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Interest at Maturity:")
                            Text(
                                text = "EGP ${String.format("%,.0f", totalInterestAtMaturity)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    val certificate = Certificate(
                        bankName = bankName,
                        certificateNumber = certificateNumber.trim(),
                        principalAmount = principalNum,
                        durationYears = durationNum,
                        annualInterestRate = rateNum,
                        purchaseDate = purchaseDate,
                        interestPaymentFrequency = interestFrequency,
                        status = CertificateStatus.ACTIVE,
                        notes = notes
                    )
                    viewModel.viewModelScope.launch {
                        viewModel.addCertificate(certificate)
                    }
                    onCertificateAdded()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Add Certificate", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}


