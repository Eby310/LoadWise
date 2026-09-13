package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.components.DataVsAirtimeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MonthlyTrendChart
import com.example.ui.components.NetworkBreakdownView
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyLightSurface
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FormatUtils

@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allRecharges by viewModel.allRecharges.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()
    val networkBreakdown by viewModel.networkBreakdown.collectAsState()
    val typeBreakdown by viewModel.typeBreakdown.collectAsState()
    val avgWeeklySpend by viewModel.averageWeeklySpend.collectAsState()

    Scaffold(
        containerColor = PureWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Insights",
                    subtitle = "Spending trends and provider breakdowns"
                )
            }

            if (allRecharges.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Insights,
                        title = "No insights yet",
                        message = "Start logging your data and airtime purchases to unlock personalized spending trends and analytics."
                    )
                }
            } else {
                // Top Stat Highlights: Average Weekly Spend & All-time Spend
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Average Weekly Spend
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyLightSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AVG. WEEKLY SPEND",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(avgWeeklySpend),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }

                        // Total All-time Spend
                        val allTimeSpend = allRecharges.sumOf { it.amount }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.4f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "TOTAL SPENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(allTimeSpend),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }

                // 1. Monthly spend trend (last 6 months Compose Canvas chart)
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        MonthlyTrendChart(trends = monthlyTrend)
                    }
                }

                // 2. Spend breakdown: Data vs Airtime
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        DataVsAirtimeView(breakdown = typeBreakdown)
                    }
                }

                // 3. Spend breakdown by network
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        NetworkBreakdownView(breakdown = networkBreakdown)
                    }
                }
            }
        }
    }
}
