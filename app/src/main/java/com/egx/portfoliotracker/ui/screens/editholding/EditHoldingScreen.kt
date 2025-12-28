package com.egx.portfoliotracker.ui.screens.editholding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.*
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
    
    if (holding == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    var sharesText by remember { mutableStateOf(holding.shares.toString()) }
    var avgCostText by remember { mutableStateOf(holding.avgCost.toString()) }
    var currentPriceText by remember { mutableStateOf(holding.currentPrice.toString()) }
    var role by remember { mutableStateOf(holding.role) }
    var status by remember { mutableStateOf(holding.status) }
    var notesText by remember { mutableStateOf(holding.notes) }
    var targetPercentageText by remember { mutableStateOf(holding.targetPercentage?.toString() ?: "") }
    var fairValueText by remember { mutableStateOf(holding.fairValue?.toString() ?: "") }
    var epsText by remember { mutableStateOf(holding.eps?.toString() ?: "") }
    var growthRateText by remember { mutableStateOf(holding.growthRate?.toString() ?: "") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit ${holding.stockSymbol}") },
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
            OutlinedTextField(
                value = sharesText,
                onValueChange = { sharesText = it },
                label = { Text("Shares") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = avgCostText,
                onValueChange = { avgCostText = it },
                label = { Text("Average Cost") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = currentPriceText,
                onValueChange = { currentPriceText = it },
                label = { Text("Current Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Role dropdown
            var roleExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = !roleExpanded }
            ) {
                OutlinedTextField(
                    value = role.displayName,
                    onValueChange = {},
                    label = { Text("Role") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    HoldingRole.values().forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.displayName) },
                            onClick = {
                                role = r
                                roleExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Status dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = status.displayName,
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    HoldingStatus.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.displayName) },
                            onClick = {
                                status = s
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Target Allocation",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = targetPercentageText,
                onValueChange = { targetPercentageText = it },
                label = { Text("Target Allocation (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Target portfolio weight 0-100%") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Valuation Analysis",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                "Enter EPS and Growth Rate to auto-calculate fair value using Graham's formula",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = epsText,
                    onValueChange = { epsText = it },
                    label = { Text("EPS") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Earnings/Share") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = growthRateText,
                    onValueChange = { growthRateText = it },
                    label = { Text("Growth %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Expected growth") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Show calculated fair value preview
            val epsVal = epsText.toDoubleOrNull()
            val growthVal = growthRateText.toDoubleOrNull() ?: 5.0
            if (epsVal != null && epsVal > 0) {
                val calculatedFV = epsVal * (8.5 + 2 * growthVal)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Calculated Fair Value (Graham's Formula)",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "EPS × (8.5 + 2 × Growth) = ${String.format("%.2f", epsVal)} × (8.5 + 2 × ${String.format("%.1f", growthVal)}) = ${String.format("%.2f", calculatedFV)} EGP",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = fairValueText,
                onValueChange = { fairValueText = it },
                label = { Text("Fair Value Override (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Override calculated value if you have your own estimate") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val shares = sharesText.toIntOrNull() ?: return@Button
                    val avgCost = avgCostText.toDoubleOrNull() ?: return@Button
                    val currentPrice = currentPriceText.toDoubleOrNull() ?: return@Button
                    
                    val targetPercentage = targetPercentageText.toDoubleOrNull()?.takeIf { it >= 0 && it <= 100 }
                    val fairValue = fairValueText.toDoubleOrNull()?.takeIf { it > 0 }
                    val eps = epsText.toDoubleOrNull()?.takeIf { it > 0 }
                    val growthRate = growthRateText.toDoubleOrNull()
                    
                    val updatedHolding = holding.copy(
                        shares = shares,
                        avgCost = avgCost,
                        currentPrice = currentPrice,
                        role = role,
                        status = status,
                        notes = notesText,
                        targetPercentage = targetPercentage,
                        fairValue = fairValue,
                        eps = eps,
                        growthRate = growthRate
                    )
                    viewModel.updateHolding(updatedHolding)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

