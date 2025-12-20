package com.egx.portfoliotracker.ui.screens.editholding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Holding
import com.egx.portfoliotracker.data.model.HoldingRole
import com.egx.portfoliotracker.data.model.HoldingStatus
import com.egx.portfoliotracker.ui.theme.*
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHoldingScreen(
    holdingId: String,
    onNavigateBack: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val holdings by viewModel.holdings.collectAsState()
    val holding = holdings.find { it.id == holdingId }
    val uiState by viewModel.uiState.collectAsState()
    
    if (holding == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    var shares by remember { mutableStateOf(holding.shares.toString()) }
    var avgCost by remember { mutableStateOf(holding.avgCost.toString()) }
    var currentPrice by remember { mutableStateOf(holding.currentPrice.toString()) }
    var selectedRole by remember { mutableStateOf(holding.role) }
    var selectedStatus by remember { mutableStateOf(holding.status) }
    var notes by remember { mutableStateOf(holding.notes) }
    
    var roleExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    
    // Handle success
    LaunchedEffect(uiState.showAddSuccess) {
        if (uiState.showAddSuccess) {
            viewModel.dismissAddSuccess()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Stock") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
            // Stock Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = holding.stockSymbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = holding.stockNameEn,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = holding.sector,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Shares input
            OutlinedTextField(
                value = shares,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    shares = filtered
                },
                label = { Text("Number of Shares") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                singleLine = true
            )
            
            // Average Cost input
            OutlinedTextField(
                value = avgCost,
                onValueChange = { newValue ->
                    val filtered = if (newValue.isEmpty()) {
                        ""
                    } else {
                        val parts = newValue.split('.')
                        when {
                            parts.size == 1 -> parts[0].filter { it.isDigit() }
                            parts.size == 2 -> {
                                val beforeDecimal = parts[0].filter { it.isDigit() }
                                val afterDecimal = parts[1].filter { it.isDigit() }
                                if (beforeDecimal.isEmpty() && afterDecimal.isEmpty()) {
                                    ""
                                } else {
                                    "$beforeDecimal.$afterDecimal"
                                }
                            }
                            else -> avgCost
                        }
                    }
                    avgCost = filtered
                },
                label = { Text("Average Cost (EGP)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                singleLine = true
            )
            
            // Current Price input
            OutlinedTextField(
                value = currentPrice,
                onValueChange = { newValue ->
                    val filtered = if (newValue.isEmpty()) {
                        ""
                    } else {
                        val parts = newValue.split('.')
                        when {
                            parts.size == 1 -> parts[0].filter { it.isDigit() }
                            parts.size == 2 -> {
                                val beforeDecimal = parts[0].filter { it.isDigit() }
                                val afterDecimal = parts[1].filter { it.isDigit() }
                                if (beforeDecimal.isEmpty() && afterDecimal.isEmpty()) {
                                    ""
                                } else {
                                    "$beforeDecimal.$afterDecimal"
                                }
                            }
                            else -> currentPrice
                        }
                    }
                    currentPrice = filtered
                },
                label = { Text("Current Price (EGP)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                singleLine = true
            )
            
            // Role Dropdown
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRole.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    HoldingRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(role.displayName, fontWeight = FontWeight.Bold)
                                    Text(
                                        role.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                selectedRole = role
                                roleExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    HoldingStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayName) },
                            onClick = {
                                selectedStatus = status
                                statusExpanded = false
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    val updatedHolding = holding.copy(
                        shares = shares.toIntOrNull() ?: holding.shares,
                        avgCost = avgCost.toDoubleOrNull() ?: holding.avgCost,
                        currentPrice = currentPrice.toDoubleOrNull() ?: holding.currentPrice,
                        role = selectedRole,
                        status = selectedStatus,
                        notes = notes,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateHolding(updatedHolding)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = shares.toIntOrNull() != null && shares.toInt() > 0 &&
                    avgCost.toDoubleOrNull() != null && avgCost.toDouble() > 0 &&
                    currentPrice.toDoubleOrNull() != null && currentPrice.toDouble() > 0
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes")
            }
        }
    }
}
