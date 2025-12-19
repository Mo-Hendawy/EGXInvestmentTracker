package com.egx.portfoliotracker.ui.screens.expenses

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
import com.egx.portfoliotracker.data.model.Expense
import com.egx.portfoliotracker.viewmodel.ExpensesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val allCategories by viewModel.allCategories.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    
    val defaultCategories = Expense.DEFAULT_CATEGORIES
    val customCategories = allCategories.filter { it !in defaultCategories }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Default Categories Section
            item {
                Text(
                    text = "Default Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(defaultCategories, key = { it }) { category ->
                CategoryItem(
                    category = category,
                    isDefault = true,
                    expenseCount = allExpenses.count { it.category == category },
                    onEdit = null,
                    onDelete = null
                )
            }
            
            // Custom Categories Section
            if (customCategories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Custom Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(customCategories, key = { it }) { category ->
                    CategoryItem(
                        category = category,
                        isDefault = false,
                        expenseCount = allExpenses.count { it.category == category },
                        onEdit = { editingCategory = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                Column {
                    Text(
                        text = "Categories are automatically created when you add expenses with new category names.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To add a new category:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Go back to Expenses",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Tap 'Add Expense'",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Type a new category name in the Category field",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Edit Category Dialog
    editingCategory?.let { category ->
        EditCategoryDialog(
            oldCategoryName = category,
            onDismiss = { editingCategory = null },
            onSave = { newCategoryName ->
                if (newCategoryName.isNotBlank() && newCategoryName != category) {
                    // Update all expenses with this category
                    allExpenses.filter { it.category == category }.forEach { expense ->
                        viewModel.updateExpense(expense.copy(category = newCategoryName.trim()))
                    }
                }
                editingCategory = null
            },
            existingCategories = allCategories
        )
    }
    
    // Delete Category Confirmation
    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = {
                val count = allExpenses.count { it.category == category }
                Text(
                    if (count > 0) {
                        "This category is used by $count expense(s). Deleting will remove the category from those expenses. Continue?"
                    } else {
                        "Are you sure you want to delete this category?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update all expenses with this category to "Other"
                        allExpenses.filter { it.category == category }.forEach { expense ->
                            viewModel.updateExpense(expense.copy(category = "Other"))
                        }
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryItem(
    category: String,
    isDefault: Boolean,
    expenseCount: Int,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$expenseCount expense(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isDefault) {
                Row {
                    IconButton(onClick = { onEdit?.invoke() }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete?.invoke() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCategoryDialog(
    oldCategoryName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    existingCategories: List<String>
) {
    var categoryName by remember { mutableStateOf(oldCategoryName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (categoryName.isNotBlank() && categoryName != oldCategoryName && categoryName in existingCategories) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Category already exists",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(categoryName) },
                enabled = categoryName.isNotBlank() && (categoryName == oldCategoryName || categoryName !in existingCategories)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
