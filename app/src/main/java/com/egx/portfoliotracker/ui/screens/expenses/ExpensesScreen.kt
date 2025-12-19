package com.egx.portfoliotracker.ui.screens.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.Expense
import com.egx.portfoliotracker.ui.components.BlurredAmount
import com.egx.portfoliotracker.viewmodel.ExpensePeriod
import com.egx.portfoliotracker.viewmodel.ExpensesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    onNavigateToCategoryManagement: () -> Unit = {},
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val expenses by viewModel.filteredExpenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    
    var showCharts by remember { mutableStateOf(true) }
    var chartType by remember { mutableStateOf(ChartType.PIE) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCategoryManagement) {
                        Icon(Icons.Default.Category, contentDescription = "Manage Categories")
                    }
                    IconButton(onClick = { showCharts = !showCharts }) {
                        Icon(
                            imageVector = if (showCharts) Icons.Default.BarChart else Icons.Default.ShowChart,
                            contentDescription = if (showCharts) "Hide charts" else "Show charts"
                        )
                    }
                    IconButton(onClick = onNavigateToAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Expense")
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
            // Summary Card
            item {
                ExpenseSummaryCard(
                    totalExpenses = totalExpenses,
                    expenseCount = expenses.size,
                    period = selectedPeriod
                )
            }
            
            // Period Selector
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    viewModel = viewModel
                )
            }
            
            // Category Filter
            if (allCategories.isNotEmpty()) {
                item {
                    CategoryFilterChips(
                        categories = allCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategory(it) }
                    )
                }
            }
            
            // Charts Section
            if (showCharts && expenses.isNotEmpty()) {
                item {
                    ChartTypeSelector(
                        selectedType = chartType,
                        onTypeSelected = { chartType = it }
                    )
                }
                
                when (chartType) {
                    ChartType.PIE -> {
                        item {
                            CategoryPieChart(
                                categoryTotals = categoryTotals,
                                totalExpenses = totalExpenses
                            )
                        }
                    }
                    ChartType.BAR -> {
                        item {
                            CategoryBarChart(
                                categoryTotals = categoryTotals
                            )
                        }
                    }
                    ChartType.LINE -> {
                        item {
                            ExpenseTrendChart(
                                viewModel = viewModel,
                                period = selectedPeriod
                            )
                        }
                    }
                }
                
                // Category Breakdown List
                if (categoryTotals.isNotEmpty()) {
                    item {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(categoryTotals, key = { it.category }) { categoryTotal ->
                        CategoryBreakdownItem(
                            category = categoryTotal.category,
                            amount = categoryTotal.total,
                            percentage = if (totalExpenses > 0) (categoryTotal.total / totalExpenses * 100) else 0.0,
                            totalExpenses = totalExpenses
                        )
                    }
                }
            }
            
            // Expenses List
            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No expenses yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Add your first expense",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onClick = { onNavigateToEditExpense(expense.id) },
                        onDelete = { viewModel.deleteExpense(expense) }
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
private fun ExpenseSummaryCard(
    totalExpenses: Double,
    expenseCount: Int,
    period: ExpensePeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total ${period.displayName}",
                    style = MaterialTheme.typography.labelSmall
                )
                BlurredAmount(
                    amount = totalExpenses,
                    currency = "EGP",
                    isBlurred = false,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "$expenseCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: ExpensePeriod,
    onPeriodSelected: (ExpensePeriod) -> Unit,
    viewModel: ExpensesViewModel
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    
    Column {
        ScrollableTabRow(
            selectedTabIndex = ExpensePeriod.entries.indexOf(selectedPeriod),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            ExpensePeriod.entries.forEachIndexed { index, period ->
                Tab(
                    selected = selectedPeriod == period,
                    onClick = { 
                        when (period) {
                            ExpensePeriod.CUSTOM_MONTH -> showMonthPicker = true
                            ExpensePeriod.CUSTOM_DAY -> showDayPicker = true
                            else -> onPeriodSelected(period)
                        }
                    },
                    text = { Text(period.displayName) }
                )
            }
        }
        
        // Show selected custom date info
        val currentMonth = selectedMonth
        if (selectedPeriod == ExpensePeriod.CUSTOM_MONTH && currentMonth != null) {
            val dateFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
            val calendar = remember(currentMonth) { Calendar.getInstance().apply {
                set(Calendar.YEAR, currentMonth.first)
                set(Calendar.MONTH, currentMonth.second - 1)
            } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected: ${dateFormat.format(calendar.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { viewModel.clearCustomDateFilter() }) {
                    Text("Clear")
                }
            }
        }
        
        val currentDay = selectedDay
        if (selectedPeriod == ExpensePeriod.CUSTOM_DAY && currentDay != null) {
            val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected: ${dateFormat.format(java.util.Date(currentDay))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { viewModel.clearCustomDateFilter() }) {
                    Text("Clear")
                }
            }
        }
    }
    
    // Month Picker Dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { year, month ->
                viewModel.setSelectedMonth(year, month)
                showMonthPicker = false
            },
            initialYear = Calendar.getInstance().get(Calendar.YEAR),
            initialMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    }
    
    // Day Picker Dialog
    if (showDayPicker) {
        DayPickerDialog(
            onDismiss = { showDayPicker = false },
            onDaySelected = { timestamp ->
                viewModel.setSelectedDay(timestamp)
                showDayPicker = false
            },
            initialDate = System.currentTimeMillis()
        )
    }
}

@Composable
private fun MonthPickerDialog(
    onDismiss: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit,
    initialYear: Int,
    initialMonth: Int
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Month selector
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                Column {
                    Text("Month:", modifier = Modifier.padding(bottom = 8.dp))
                    monthNames.forEachIndexed { index, monthName ->
                        @OptIn(ExperimentalMaterial3Api::class)
                        FilterChip(
                            selected = selectedMonth == index + 1,
                            onClick = { selectedMonth = index + 1 },
                            label = { Text(monthName) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onMonthSelected(selectedYear, selectedMonth) }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DayPickerDialog(
    onDismiss: () -> Unit,
    onDaySelected: (Long) -> Unit,
    initialDate: Long
) {
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = initialDate } }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Day") },
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
                            // Update max day for the month
                            val cal = Calendar.getInstance()
                            cal.set(selectedYear, selectedMonth, 1)
                            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            selectedDay = selectedDay.coerceIn(1, maxDay)
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        val monthNames = arrayOf(
                            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                        )
                        Text(
                            text = monthNames[selectedMonth],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { 
                            selectedMonth = (selectedMonth + 1).coerceIn(0, 11)
                            // Update max day for the month
                            val cal = Calendar.getInstance()
                            cal.set(selectedYear, selectedMonth, 1)
                            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            selectedDay = selectedDay.coerceIn(1, maxDay)
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
                            val cal = Calendar.getInstance()
                            cal.set(selectedYear, selectedMonth, 1)
                            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
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
                            val cal = Calendar.getInstance()
                            cal.set(selectedYear, selectedMonth, 1)
                            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
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
                    val cal = Calendar.getInstance()
                    cal.set(selectedYear, selectedMonth, selectedDay)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    onDaySelected(cal.timeInMillis)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All") }
        )
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(if (selectedCategory == category) null else category) },
                label = { Text(category) }
            )
        }
    }
}

enum class ChartType {
    PIE, BAR, LINE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartTypeSelector(
    selectedType: ChartType,
    onTypeSelected: (ChartType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { 
                    Text(
                        when (type) {
                            ChartType.PIE -> "Pie"
                            ChartType.BAR -> "Bar"
                            ChartType.LINE -> "Trend"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryPieChart(
    categoryTotals: List<com.egx.portfoliotracker.viewmodel.CategoryTotal>,
    totalExpenses: Double
) {
    if (categoryTotals.isEmpty() || totalExpenses == 0.0) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val chartData = categoryTotals.map { it.category to it.total }
            com.egx.portfoliotracker.ui.components.DonutChart(
                data = chartData,
                modifier = Modifier.size(250.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            com.egx.portfoliotracker.ui.components.ChartLegend(
                data = chartData,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CategoryBarChart(
    categoryTotals: List<com.egx.portfoliotracker.viewmodel.CategoryTotal>
) {
    if (categoryTotals.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val maxValue = categoryTotals.maxOfOrNull { it.total } ?: 1.0
            val chartData = categoryTotals.map { categoryTotal ->
                val percentage = if (maxValue > 0) (categoryTotal.total / maxValue * 100) else 0.0
                Triple(
                    categoryTotal.category,
                    percentage,
                    com.egx.portfoliotracker.ui.theme.ChartColors[categoryTotals.indexOf(categoryTotal) % com.egx.portfoliotracker.ui.theme.ChartColors.size]
                )
            }
            
            com.egx.portfoliotracker.ui.components.HorizontalBarChart(
                data = chartData,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpenseTrendChart(
    viewModel: ExpensesViewModel,
    period: ExpensePeriod
) {
    var dailyExpenses by remember { mutableStateOf<List<com.egx.portfoliotracker.data.local.DailyExpense>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(period) {
        scope.launch {
            dailyExpenses = viewModel.getDailyExpensesForPeriod(period)
        }
    }
    
    if (dailyExpenses.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple line chart visualization
            ExpenseLineChart(
                data = dailyExpenses.map { it.total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ExpenseLineChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxValue = data.maxOrNull() ?: 1.0
    val minValue = data.minOrNull() ?: 0.0
    val range = if (maxValue - minValue == 0.0) 1.0 else maxValue - minValue
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height).toFloat()
            androidx.compose.ui.geometry.Offset(x, y)
        }
        
        // Draw line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = primaryColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun CategoryBreakdownItem(
    category: String,
    amount: Double,
    percentage: Double,
    totalExpenses: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BlurredAmount(
                amount = amount,
                currency = "EGP",
                isBlurred = false,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (expense.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${dateFormat.format(Date(expense.date))} • ${timeFormat.format(Date(expense.date))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    BlurredAmount(
                        amount = expense.amount,
                        currency = "EGP",
                        isBlurred = false,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
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
