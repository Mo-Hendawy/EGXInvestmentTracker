package com.egx.portfoliotracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Displays money amount with blur/privacy feature
 * When blurred, shows asterisks instead of actual amount
 */
@Composable
fun BlurredAmount(
    amount: Double,
    currency: String = "EGP",
    isBlurred: Boolean,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle? = null,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val displayText = if (isBlurred) {
        "••••••"
    } else {
        "$currency ${String.format("%,.2f", amount)}"
    }
    
    Text(
        text = displayText,
        modifier = modifier,
        style = style ?: MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = color,
        fontSize = fontSize
    )
}

/**
 * Displays money amount without decimals (for large amounts)
 */
@Composable
fun BlurredAmountNoDecimals(
    amount: Double,
    currency: String = "EGP",
    isBlurred: Boolean,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle? = null,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val displayText = if (isBlurred) {
        "••••••"
    } else {
        "$currency ${String.format("%,.0f", amount)}"
    }
    
    Text(
        text = displayText,
        modifier = modifier,
        style = style ?: MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = color,
        fontSize = fontSize
    )
}

/**
 * Displays percentage with blur
 */
@Composable
fun BlurredPercentage(
    percentage: Double,
    isBlurred: Boolean,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle? = null,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    showSign: Boolean = true
) {
    val displayText = if (isBlurred) {
        "•••"
    } else {
        val sign = if (showSign && percentage >= 0) "+" else ""
        "$sign${String.format("%.2f", percentage)}%"
    }
    
    Text(
        text = displayText,
        modifier = modifier,
        style = style ?: MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = color,
        fontSize = fontSize
    )
}
