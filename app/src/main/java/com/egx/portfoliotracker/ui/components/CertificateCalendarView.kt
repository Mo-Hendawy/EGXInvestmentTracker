package com.egx.portfoliotracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egx.portfoliotracker.data.model.Certificate
import com.egx.portfoliotracker.data.model.CertificateIncomeDetail
import com.egx.portfoliotracker.data.model.InterestFrequency
import com.egx.portfoliotracker.ui.theme.ProfitGreen
import java.util.Calendar

/**
 * Calendar view showing certificate payment dates
 * Shows all months with payment dates highlighted
 * Past dates: one color, future dates: different color
 * Shows certificate IDs on payment days
 */
@Composable
fun CertificateCalendarView(
    certificates: List<Certificate>,
    selectedYear: Int,
    selectedMonth: Int,
    onMonthChange: (Int, Int) -> Unit,
    isBlurred: Boolean = false,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    val currentDate = Calendar.getInstance()
    val currentYear = currentDate.get(Calendar.YEAR)
    val currentMonth = currentDate.get(Calendar.MONTH) + 1
    val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
    
    // Get payment days for this month
    val paymentDays = remember(certificates, selectedYear, selectedMonth) {
        certificates
            .filter { cert ->
                cert.status == com.egx.portfoliotracker.data.model.CertificateStatus.ACTIVE &&
                cert.interestPaymentFrequency == InterestFrequency.MONTHLY &&
                cert.getMonthlyIncomeForMonth(selectedYear, selectedMonth) > 0
            }
            .mapNotNull { cert ->
                val paymentDay = cert.getPaymentDayOfMonth()
                val dueDate = cert.getInterestDueDateForMonth(selectedYear, selectedMonth)
                if (dueDate != null) {
                    PaymentDayInfo(
                        day = paymentDay,
                        amount = cert.getMonthlyIncomeForMonth(selectedYear, selectedMonth),
                        certificateId = cert.certificateNumber.ifEmpty { cert.id.take(8) },
                        isPast = selectedYear < currentYear || 
                                (selectedYear == currentYear && selectedMonth < currentMonth) ||
                                (selectedYear == currentYear && selectedMonth == currentMonth && paymentDay < currentDay)
                    )
                } else null
            }
            .groupBy { it.day }
            .mapValues { (_, infos) ->
                PaymentDayInfo(
                    day = infos.first().day,
                    amount = infos.sumOf { it.amount },
                    certificateId = infos.joinToString(", ") { it.certificateId },
                    isPast = infos.first().isPast
                )
            }
    }
    
    // Calculate calendar days
    calendar.set(selectedYear, selectedMonth - 1, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val monthName = remember(selectedYear, selectedMonth) {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        monthNames[selectedMonth - 1]
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prevMonth = if (selectedMonth == 1) 12 else selectedMonth - 1
                val prevYear = if (selectedMonth == 1) selectedYear - 1 else selectedYear
                onMonthChange(prevYear, prevMonth)
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
            }
            
            Text(
                text = "$monthName $selectedYear",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                val nextMonth = if (selectedMonth == 12) 1 else selectedMonth + 1
                val nextYear = if (selectedMonth == 12) selectedYear + 1 else selectedYear
                onMonthChange(nextYear, nextMonth)
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Day headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar days
                var dayCounter = 1
                while (dayCounter <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (weekDay in 0..6) {
                            if (weekDay < firstDayOfWeek && dayCounter == 1) {
                                // Empty cell before first day
                                Spacer(modifier = Modifier.weight(1f))
                            } else if (dayCounter <= daysInMonth) {
                                val day = dayCounter++
                                val paymentInfo = paymentDays[day]
                                
                                CalendarDayCell(
                                    day = day,
                                    paymentInfo = paymentInfo,
                                    isToday = selectedYear == currentYear && 
                                             selectedMonth == currentMonth && 
                                             day == currentDay,
                                    isBlurred = isBlurred,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Empty cell after last day
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    paymentInfo: PaymentDayInfo?,
    isToday: Boolean,
    isBlurred: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        paymentInfo == null -> Color.Transparent
        paymentInfo.isPast -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else -> ProfitGreen.copy(alpha = 0.3f)
    }
    
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (paymentInfo != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
            
            if (paymentInfo != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBlurred) "•••" else "EGP ${String.format("%.0f", paymentInfo.amount)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (paymentInfo.isPast) MaterialTheme.colorScheme.onSecondaryContainer else ProfitGreen
                )
                if (paymentInfo.certificateId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "#${paymentInfo.certificateId}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private data class PaymentDayInfo(
    val day: Int,
    val amount: Double,
    val certificateId: String,
    val isPast: Boolean
)


