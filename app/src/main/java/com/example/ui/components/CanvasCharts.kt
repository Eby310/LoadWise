package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NavyTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DaySpend
import com.example.ui.viewmodel.MonthTrend
import com.example.ui.viewmodel.NetworkSpend
import com.example.ui.viewmodel.TypeBreakdown
import com.example.util.FormatUtils

@Composable
fun HomeBarChart(
    days: List<DaySpend>,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DaySpend?>(null) }
    val maxSpend = remember(days) {
        val highest = days.maxOfOrNull { it.amount } ?: 0.0
        if (highest <= 0.0) 1000.0 else highest
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_bar_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last 7 Days Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = if (selectedDay != null) {
                            "${selectedDay!!.dayLabel}: ${FormatUtils.formatCurrency(selectedDay!!.amount)}"
                        } else {
                            "Tap any day to view details"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedDay != null) NavySecondary else TextSecondary
                    )
                }

                val total7Days = remember(days) { days.sumOf { it.amount } }
                Text(
                    text = FormatUtils.formatCurrency(total7Days),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = days.size.coerceAtLeast(1)
                    val slotWidth = canvasWidth / barCount
                    val barWidth = (slotWidth * 0.45f).coerceIn(12.dp.toPx(), 28.dp.toPx())

                    // Baseline guideline
                    drawLine(
                        color = BorderGrey,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw each bar
                    days.forEachIndexed { index, day ->
                        val slotCenter = index * slotWidth + (slotWidth / 2f)
                        val barLeft = slotCenter - (barWidth / 2f)

                        val barHeightRatio = (day.amount / maxSpend).toFloat().coerceIn(0f, 1f)
                        val calculatedBarHeight = barHeightRatio * (canvasHeight - 12.dp.toPx())
                        val finalBarHeight = if (day.amount > 0) calculatedBarHeight.coerceAtLeast(6.dp.toPx()) else 4.dp.toPx()
                        val barTop = canvasHeight - finalBarHeight

                        val barColor = when {
                            selectedDay?.timestamp == day.timestamp -> NavySecondary
                            day.amount > 0 -> NavyPrimary
                            else -> LightGreyFill
                        }

                        // Rounded bar
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, finalBarHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isSelected = selectedDay?.timestamp == day.timestamp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedDay = if (selectedDay?.timestamp == day.timestamp) null else day
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NavyPrimary else if (day.isToday) NavySecondary else TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = day.dayNumber,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (day.isToday) NavyPrimary else TextSecondary,
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendChart(
    trends: List<MonthTrend>,
    modifier: Modifier = Modifier
) {
    val maxTotal = remember(trends) {
        val highest = trends.maxOfOrNull { it.totalSpend } ?: 0.0
        if (highest <= 0.0) 5000.0 else highest
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "6-Month Spending Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Monthly comparison of total recharges",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val count = trends.size.coerceAtLeast(1)
                    val slotWidth = canvasWidth / count
                    val barWidth = (slotWidth * 0.48f).coerceIn(16.dp.toPx(), 34.dp.toPx())

                    // Baseline
                    drawLine(
                        color = BorderGrey,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    trends.forEachIndexed { i, month ->
                        val slotCenter = i * slotWidth + (slotWidth / 2f)
                        val barLeft = slotCenter - (barWidth / 2f)

                        val ratio = (month.totalSpend / maxTotal).toFloat().coerceIn(0f, 1f)
                        val totalHeight = ratio * (canvasHeight - 14.dp.toPx())
                        val barHeight = if (month.totalSpend > 0) totalHeight.coerceAtLeast(6.dp.toPx()) else 4.dp.toPx()
                        val barTop = canvasHeight - barHeight

                        // Stacked portion: Data vs Airtime
                        val dataRatio = if (month.totalSpend > 0) (month.dataSpend / month.totalSpend).toFloat() else 0f
                        val dataHeight = barHeight * dataRatio
                        val airtimeHeight = barHeight - dataHeight

                        if (month.totalSpend > 0) {
                            // Airtime portion (top)
                            if (airtimeHeight > 0) {
                                drawRoundRect(
                                    color = NavySecondary,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, airtimeHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                            // Data portion (bottom)
                            if (dataHeight > 0) {
                                drawRoundRect(
                                    color = NavyPrimary,
                                    topLeft = Offset(barLeft, barTop + airtimeHeight),
                                    size = Size(barWidth, dataHeight),
                                    cornerRadius = CornerRadius(0f, 0f)
                                )
                            }
                        } else {
                            // Empty placeholder bar
                            drawRoundRect(
                                color = LightGreyFill,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEach { month ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = month.monthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (month.totalSpend > 0) {
                                if (month.totalSpend >= 1000) "${(month.totalSpend / 1000).toInt()}k" else "${month.totalSpend.toInt()}"
                            } else "0",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NavyPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Data", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NavySecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Airtime", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun NetworkBreakdownView(
    breakdown: List<NetworkSpend>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("network_breakdown_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Spend by Network",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Distribution across mobile providers",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (breakdown.isEmpty()) {
                Text(
                    text = "No network data recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Segmented horizontal progress bar (Canvas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        var startX = 0f
                        val width = size.width
                        val height = size.height

                        breakdown.forEach { item ->
                            val segWidth = (item.percentage / 100f) * width
                            val color = getNetworkColor(item.network)
                            drawRect(
                                color = color,
                                topLeft = Offset(startX, 0f),
                                size = Size(segWidth, height)
                            )
                            startX += segWidth
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown list items
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    breakdown.forEach { item ->
                        val netColor = getNetworkColor(item.network)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(netColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.network,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${String.format("%.1f", item.percentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = FormatUtils.formatCurrency(item.amount),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataVsAirtimeView(
    breakdown: TypeBreakdown,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("data_vs_airtime_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Data vs Airtime",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Percentage split of all expenses",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visual segmented comparison bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGreyFill)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    val dataWidth = (breakdown.dataPercentage / 100f) * width

                    // Data segment
                    if (dataWidth > 0) {
                        drawRect(
                            color = NavyPrimary,
                            topLeft = Offset(0f, 0f),
                            size = Size(dataWidth, height)
                        )
                    }
                    // Airtime segment
                    val airtimeWidth = width - dataWidth
                    if (airtimeWidth > 0 && breakdown.airtimePercentage > 0) {
                        drawRect(
                            color = NavySecondary,
                            topLeft = Offset(dataWidth, 0f),
                            size = Size(airtimeWidth, height)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two distinct summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Data Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreyFill)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Data",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatCurrency(breakdown.dataSpend),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", breakdown.dataPercentage)}% (${breakdown.dataCount} transactions)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Airtime Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreyFill)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NavySecondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Airtime",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatCurrency(breakdown.airtimeSpend),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", breakdown.airtimePercentage)}% (${breakdown.airtimeCount} transactions)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
