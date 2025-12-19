package com.egx.portfoliotracker.ui.screens.certificates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.egx.portfoliotracker.ui.components.BlurredAmountNoDecimals
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateDetailScreen(
    certificateId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val certificates by viewModel.certificates.collectAsState()
    val certificate = certificates.find { it.id == certificateId }
    val isAmountsBlurred by viewModel.isAmountsBlurred.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (certificate == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(certificate.bankName)
                        Text(
                            text = "${certificate.durationYears} years @ ${String.format("%.2f", certificate.annualInterestRate)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main value card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Value",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BlurredAmountNoDecimals(
                            amount = certificate.currentValue,
                            currency = "EGP",
                            isBlurred = isAmountsBlurred,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Key metrics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Certificate Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (certificate.certificateNumber.isNotEmpty()) {
                            DetailRow(label = "Certificate Number", value = certificate.certificateNumber)
                        }
                        DetailRow(label = "Principal Amount", value = if (isAmountsBlurred) "••••••" else "EGP ${String.format("%,.0f", certificate.principalAmount)}")
                        DetailRow(label = "Interest Rate", value = "${String.format("%.2f", certificate.annualInterestRate)}%")
                        DetailRow(label = "Duration", value = "${certificate.durationYears} years")
                        DetailRow(label = "Monthly Income", value = if (isAmountsBlurred) "••••" else "EGP ${String.format("%,.2f", certificate.monthlyInterest)}")
                        DetailRow(label = "Purchase Date", value = dateFormat.format(Date(certificate.purchaseDate)))
                        DetailRow(label = "Maturity Date", value = dateFormat.format(Date(certificate.maturityDate)))
                        DetailRow(label = "Days Until Maturity", value = "${certificate.daysUntilMaturity} days")
                        DetailRow(label = "Accrued Interest", value = if (isAmountsBlurred) "••••••" else "EGP ${String.format("%,.2f", certificate.accruedInterest)}")
                        DetailRow(label = "Total Interest at Maturity", value = if (isAmountsBlurred) "••••••" else "EGP ${String.format("%,.0f", certificate.totalInterestAtMaturity)}")
                        DetailRow(label = "Payment Frequency", value = certificate.interestPaymentFrequency.displayName)
                        DetailRow(label = "Status", value = certificate.status.displayName)
                    }
                }
            }
            
            // Notes
            if (certificate.notes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = certificate.notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Certificate") },
            text = { Text("Are you sure you want to delete this certificate?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.deleteCertificate(certificate)
                        }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}


