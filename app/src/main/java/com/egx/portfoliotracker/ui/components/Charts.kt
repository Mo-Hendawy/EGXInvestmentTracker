package com.egx.portfoliotracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egx.portfoliotracker.ui.theme.ChartColors

/**
 * Animated Donut/Pie Chart for allocation visualization
 */
@Composable
fun DonutChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 32.dp,
    animationDuration: Int = 1000
) {
    val total = data.sumOf { it.second }
    if (total == 0.0) return
    
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        )
    }
    
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val stroke = strokeWidth.toPx()
        val innerRadius = radius - stroke / 2
        
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, value) ->
            val sweepAngle = (value / total * 360f * animatedProgress.value).toFloat()
            val color = ChartColors[index % ChartColors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(canvasSize - stroke, canvasSize - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            
            startAngle += sweepAngle
        }
    }
}

/**
 * Portfolio Donut Chart with total value in center (like the user's screenshot)
 */
@Composable
fun PortfolioDonutChart(
    stockAllocations: List<Pair<String, Double>>,
    totalValue: Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    animationDuration: Int = 1000
) {
    val total = stockAllocations.sumOf { it.second }
    if (total == 0.0) return
    
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(stockAllocations) {
        animatedProgress.snapTo(0f)
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val stroke = strokeWidth.toPx()
            
            var startAngle = -90f
            
            stockAllocations.forEachIndexed { index, (_, percentage) ->
                val sweepAngle = (percentage / total * 360f * animatedProgress.value).toFloat()
                val color = ChartColors[index % ChartColors.size]
                
                // Add small gap between segments
                val gapAngle = if (stockAllocations.size > 1) 1f else 0f
                val adjustedSweep = (sweepAngle - gapAngle).coerceAtLeast(0f)
                
                drawArc(
                    color = color,
                    startAngle = startAngle + gapAngle / 2,
                    sweepAngle = adjustedSweep,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(canvasSize - stroke, canvasSize - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Center text showing total value
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%,.2f", totalValue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
                Text(
                    text = "EGP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
    }
}

/**
 * Stock allocation legend with colored indicators
 */
@Composable
fun StockAllocationLegend(
    allocations: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        allocations.forEachIndexed { index, (symbol, percentage) ->
            val color = ChartColors[index % ChartColors.size]
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "–",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f%%", percentage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}

/**
 * Horizontal bar chart for performance comparison
 */
@Composable
fun HorizontalBarChart(
    data: List<Triple<String, Double, Color>>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 800
) {
    val maxValue = data.maxOfOrNull { kotlin.math.abs(it.second) } ?: 1.0
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { (label, value, color) ->
            HorizontalBar(
                label = label,
                value = value,
                maxValue = maxValue,
                color = color,
                animationDuration = animationDuration
            )
        }
    }
}

@Composable
private fun HorizontalBar(
    label: String,
    value: Double,
    maxValue: Double,
    color: Color,
    animationDuration: Int
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        )
    }
    
    val widthFraction = (kotlin.math.abs(value) / maxValue * animatedProgress.value).toFloat()
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format("%.1f%%", value),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Legend component for charts
 */
@Composable
fun ChartLegend(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEachIndexed { index, (label, value) ->
            val percentage = if (total > 0) value / total * 100 else 0.0
            val color = ChartColors[index % ChartColors.size]
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Circular progress indicator with center text
 */
@Composable
fun CircularProgressWithText(
    progress: Float,
    text: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val stroke = strokeWidth.toPx()
            
            // Background circle
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(canvasSize - stroke, canvasSize - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(canvasSize - stroke, canvasSize - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Mini sparkline chart
 */
@Composable
fun SparklineChart(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val minValue = data.minOrNull() ?: 0.0
    val maxValue = data.maxOrNull() ?: 1.0
    val range = if (maxValue - minValue == 0.0) 1.0 else maxValue - minValue
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height).toFloat()
            Offset(x, y)
        }
        
        // Draw line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Draw end dot
        points.lastOrNull()?.let { lastPoint ->
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

/**
 * Performance period selector tabs
 */
@Composable
fun PerformancePeriodCard(
    periodLabel: String,
    valueChange: Double,
    valueChangePercent: Double,
    dividends: Double,
    totalReturn: Double,
    totalReturnPercent: Double,
    modifier: Modifier = Modifier
) {
    val isPositive = totalReturn >= 0
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            text = periodLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Total Return
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Return",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${if (isPositive) "+" else ""}${String.format("%.2f", totalReturnPercent)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        // Price Change
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Price Change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (valueChange >= 0) "+" else ""}${String.format("%,.0f", valueChange)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Dividends
        if (dividends > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dividends",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${String.format("%,.0f", dividends)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}
