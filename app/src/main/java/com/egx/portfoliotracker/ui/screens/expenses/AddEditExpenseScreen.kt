package com.egx.portfoliotracker.ui.screens.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Expense
import com.egx.portfoliotracker.viewmodel.ExpensesViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val isEditing = expenseId != null
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val allCategories by viewModel.allCategories.collectAsState()
    val availableCategories = remember(allCategories) {
        if (allCategories.isEmpty()) {
            Expense.DEFAULT_CATEGORIES
        } else {
            (allCategories + Expense.DEFAULT_CATEGORIES).distinct().sorted()
        }
    }
    
    // Load expense if editing
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                category = expense.category
                amount = expense.amount.toString()
                description = expense.description
                selectedDate = expense.date
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // Category Selection - Allow free text or dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = !showCategoryMenu }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    trailingIcon = { 
                        if (showCategoryMenu) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                        } else {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Show categories")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    },
                    placeholder = { Text("Type or select category") }
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    availableCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }
            
            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                        amount = it
                    }
                },
                label = { Text("Amount (EGP) *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                maxLines = 3
            )
            
            // Date Selection
            val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
            OutlinedTextField(
                value = dateFormat.format(Date(selectedDate)),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Select Date")
                    }
                }
            )
            
            // Date Picker
            if (showDatePicker) {
                val calendar = remember { Calendar.getInstance().apply { timeInMillis = selectedDate } }
                val year = remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
                val month = remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
                val day = remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
                
                DatePickerDialog(
                    onDateSelected = { y, m, d ->
                        val cal = Calendar.getInstance()
                        cal.set(y, m, d)
                        selectedDate = cal.timeInMillis
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false },
                    initialYear = year.value,
                    initialMonth = month.value,
                    initialDay = day.value
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (category.isNotEmpty() && amountValue > 0) {
                        val expense = if (isEditing && expenseId != null) {
                            Expense(
                                id = expenseId,
                                category = category,
                                amount = amountValue,
                                description = description,
                                date = selectedDate,
                                createdAt = System.currentTimeMillis(), // Keep original
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            Expense(
                                id = UUID.randomUUID().toString(),
                                category = category,
                                amount = amountValue,
                                description = description,
                                date = selectedDate
                            )
                        }
                        
                        if (isEditing) {
                            viewModel.updateExpense(expense)
                        } else {
                            viewModel.addExpense(expense)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = category.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull() ?: 0.0 > 0
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Update Expense" else "Add Expense")
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && expenseId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(Expense(id = expenseId, category = "", amount = 0.0, date = 0L))
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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
private fun DatePickerDialog(
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Year:")
                    Row {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$selectedYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Month:")
                    Row {
                        IconButton(onClick = { 
                            selectedMonth = (selectedMonth - 1).coerceIn(0, 11)
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "${selectedMonth + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { 
                            selectedMonth = (selectedMonth + 1).coerceIn(0, 11)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Day selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Day:")
                    Row {
                        IconButton(onClick = { 
                            val calendar = Calendar.getInstance()
                            calendar.set(selectedYear, selectedMonth, 1)
                            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            selectedDay = (selectedDay - 1).coerceIn(1, maxDay)
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$selectedDay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { 
                            val calendar = Calendar.getInstance()
                            calendar.set(selectedYear, selectedMonth, 1)
                            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            selectedDay = (selectedDay + 1).coerceIn(1, maxDay)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedYear, selectedMonth, selectedDay)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
