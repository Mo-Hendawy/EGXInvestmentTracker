package com.egx.portfoliotracker.ui.screens.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Watchlist
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWatchlistScreen(
    watchlistId: String?,
    onNavigateBack: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val stocks by viewModel.getAllStocks().collectAsState(initial = emptyList())
    
    val existingItem = watchlistId?.let { id ->
        watchlistItems.find { it.id == id }
    }
    
    var selectedSymbol by remember { mutableStateOf(existingItem?.stockSymbol ?: "") }
    var targetPriceText by remember { mutableStateOf(existingItem?.targetPrice?.toString() ?: "") }
    var notesText by remember { mutableStateOf(existingItem?.notes ?: "") }
    
    val selectedStock = stocks.find { it.symbol == selectedSymbol }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (watchlistId == null) "Add to Watchlist" else "Edit Watchlist") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stock symbol dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSymbol,
                    onValueChange = { selectedSymbol = it },
                    label = { Text("Stock Symbol") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    stocks.forEach { stock ->
                        DropdownMenuItem(
                            text = { Text("${stock.symbol} - ${stock.nameEn}") },
                            onClick = {
                                selectedSymbol = stock.symbol
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Target price
            OutlinedTextField(
                value = targetPriceText,
                onValueChange = { targetPriceText = it },
                label = { Text("Target Price (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button
            Button(
                onClick = {
                    if (selectedStock != null) {
                        val targetPrice = targetPriceText.toDoubleOrNull()
                        val watchlist = Watchlist(
                            id = existingItem?.id ?: "",
                            stockSymbol = selectedStock.symbol,
                            stockNameEn = selectedStock.nameEn,
                            stockNameAr = selectedStock.nameAr,
                            sector = selectedStock.sector,
                            targetPrice = targetPrice,
                            notes = notesText
                        )
                        if (watchlistId == null) {
                            viewModel.addWatchlistItem(watchlist)
                        } else {
                            viewModel.updateWatchlistItem(watchlist)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSymbol.isNotEmpty() && selectedStock != null
            ) {
                Text(if (watchlistId == null) "Add to Watchlist" else "Update")
            }
        }
    }
}

