package com.egx.portfoliotracker.ui.screens.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egx.portfoliotracker.data.model.PortfolioSnapshot
import com.egx.portfoliotracker.data.model.TimePeriod
import com.egx.portfoliotracker.ui.theme.ProfitGreen
import com.egx.portfoliotracker.ui.theme.LossRed
import com.egx.portfoliotracker.viewmodel.PortfolioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val portfolioSnapshots by viewModel.portfolioSnapshots.collectAsState(initial = emptyList())
    val holdings by viewModel.holdings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Charts") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Portfolio Value Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Portfolio Value Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PortfolioValueLineChart(
                        snapshots = portfolioSnapshots,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
            
            // Profit/Loss Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Profit/Loss Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfitLossLineChart(
                        snapshots = portfolioSnapshots,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioValueLineChart(
    snapshots: List<PortfolioSnapshot>,
    modifier: Modifier = Modifier
) {
    if (snapshots.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    val density = LocalDensity.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    val sortedSnapshots = snapshots.sortedBy { it.timestamp }
    val minValue = sortedSnapshots.minOfOrNull { it.totalValue } ?: 0.0
    val maxValue = sortedSnapshots.maxOfOrNull { it.totalValue } ?: 1.0
    val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val x = tapOffset.x
                        val index = ((x / width) * sortedSnapshots.size).toInt().coerceIn(0, sortedSnapshots.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val padding = 40.dp.toPx()
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2
            
            // Draw grid lines
            for (i in 0..4) {
                val y = padding + (chartHeight / 4) * i
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw line
            val path = Path()
            sortedSnapshots.forEachIndexed { index, snapshot ->
                val x = padding + (chartWidth / (sortedSnapshots.size - 1).coerceAtLeast(1)) * index
                val normalizedValue = ((snapshot.totalValue - minValue) / valueRange).toFloat()
                val y = padding + chartHeight - (normalizedValue * chartHeight)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = ProfitGreen,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw points
            sortedSnapshots.forEachIndexed { index, snapshot ->
                val x = padding + (chartWidth / (sortedSnapshots.size - 1).coerceAtLeast(1)) * index
                val normalizedValue = ((snapshot.totalValue - minValue) / valueRange).toFloat()
                val y = padding + chartHeight - (normalizedValue * chartHeight)
                
                val pointColor = if (selectedIndex == index) Color.Blue else ProfitGreen
                drawCircle(
                    color = pointColor,
                    radius = if (selectedIndex == index) 8.dp.toPx() else 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Draw tooltip
            selectedIndex?.let { idx ->
                if (idx in sortedSnapshots.indices) {
                    val snapshot = sortedSnapshots[idx]
                    val x = padding + (chartWidth / (sortedSnapshots.size - 1).coerceAtLeast(1)) * idx
                    val normalizedValue = ((snapshot.totalValue - minValue) / valueRange).toFloat()
                    val y = padding + chartHeight - (normalizedValue * chartHeight)
                    
                    // Tooltip background
                    val tooltipWidth = 150.dp.toPx()
                    val tooltipHeight = 60.dp.toPx()
                    val tooltipX = (x - tooltipWidth / 2.0f).coerceIn(0f, width - tooltipWidth)
                    val tooltipY = (y - tooltipHeight - 20.dp.toPx()).coerceAtLeast(0f)
                    
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.8f),
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipWidth, tooltipHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
        }
        
        // Tooltip text
        selectedIndex?.let { idx ->
            if (idx in sortedSnapshots.indices) {
                val snapshot = sortedSnapshots[idx]
                val dateStr = dateFormat.format(Date(snapshot.timestamp))
                val valueStr = String.format("%,.2f", snapshot.totalValue)
                
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "$valueStr EGP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitLossLineChart(
    snapshots: List<PortfolioSnapshot>,
    modifier: Modifier = Modifier
) {
    if (snapshots.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    val sortedSnapshots = snapshots.sortedBy { it.timestamp }
    val minValue = sortedSnapshots.minOfOrNull { it.profitLoss } ?: 0.0
    val maxValue = sortedSnapshots.maxOfOrNull { it.profitLoss } ?: 1.0
    val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
            val zeroY = if (minValue < 0 && maxValue > 0) {
                (-minValue / valueRange).toFloat()
            } else null
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val x = tapOffset.x
                        val index = ((x / width) * sortedSnapshots.size).toInt().coerceIn(0, sortedSnapshots.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val padding = 40.dp.toPx()
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2
            
            // Draw zero line
            zeroY?.let {
                val zeroLineY = padding + chartHeight - (it * chartHeight)
                drawLine(
                    color = Color.Gray,
                    start = Offset(padding, zeroLineY),
                    end = Offset(width - padding, zeroLineY),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Draw line
            val path = Path()
            sortedSnapshots.forEachIndexed { index, snapshot ->
                val x = padding + (chartWidth / (sortedSnapshots.size - 1).coerceAtLeast(1)) * index
                val normalizedValue = ((snapshot.profitLoss - minValue) / valueRange).toFloat()
                val y = padding + chartHeight - (normalizedValue * chartHeight)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            val lineColor = if (sortedSnapshots.lastOrNull()?.profitLoss ?: 0.0 >= 0) ProfitGreen else LossRed
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw points
            sortedSnapshots.forEachIndexed { index, snapshot ->
                val x = padding + (chartWidth / (sortedSnapshots.size - 1).coerceAtLeast(1)) * index
                val normalizedValue = ((snapshot.profitLoss - minValue) / valueRange).toFloat()
                val y = padding + chartHeight - (normalizedValue * chartHeight)
                
                val pointColor = if (selectedIndex == index) Color.Blue else lineColor
                drawCircle(
                    color = pointColor,
                    radius = if (selectedIndex == index) 8.dp.toPx() else 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        // Tooltip
        selectedIndex?.let { idx ->
            if (idx in sortedSnapshots.indices) {
                val snapshot = sortedSnapshots[idx]
                val dateStr = dateFormat.format(Date(snapshot.timestamp))
                val profitLossStr = String.format("%+,.2f", snapshot.profitLoss)
                
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "$profitLossStr EGP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (snapshot.profitLoss >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }
        }
    }
}

