package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.Recharge
import com.example.ui.components.AppHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomeBarChart
import com.example.ui.components.RechargeItemCard
import com.example.ui.components.UssdQuickSection
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyLightSurface
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TypeFilter
import com.example.util.FormatUtils

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val thisWeekSpend by viewModel.thisWeekSpend.collectAsState()
    val thisMonthSpend by viewModel.thisMonthSpend.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isBudgetExceeded by viewModel.isBudgetExceeded.collectAsState()
    val isBudget80PercentReached by viewModel.isBudget80PercentReached.collectAsState()
    val budgetPercentage by viewModel.budgetPercentage.collectAsState()
    val last7Days by viewModel.last7DaysSpend.collectAsState()
    val homeFilter by viewModel.homeTypeFilter.collectAsState()
    val recentRecharges by viewModel.homeFilteredRecharges.collectAsState()

    // Group recharges by date string
    val groupedRecharges = remember(recentRecharges) {
        recentRecharges.groupBy { FormatUtils.formatDateGroup(it.date) }
    }

    // Subtle fade-in & slide animation for summary cards triggered on launch or when spend data refreshes
    val cardsAlpha = remember { Animatable(0.25f) }
    val cardsTranslationY = remember { Animatable(10f) }

    LaunchedEffect(thisWeekSpend, thisMonthSpend) {
        cardsAlpha.snapTo(0.25f)
        cardsTranslationY.snapTo(10f)
        launch {
            cardsAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        launch {
            cardsTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
    }

    // Subtle floating hovering animation on the add/log recharge button
    val fabInfiniteTransition = rememberInfiniteTransition(label = "fab_floating_anim")
    val fabHoverOffsetY by fabInfiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_hover_offset"
    )
    val fabElevation by fabInfiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_elevation"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddRecharge() },
                shape = CircleShape,
                containerColor = NavyPrimary,
                contentColor = PureWhite,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = fabElevation.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 8.dp)
                    .offset(y = fabHoverOffsetY.dp)
                    .testTag("home_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recharge",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = PureWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Top Header
            item {
                AppHeader(
                    title = "Loadwise",
                    subtitle = "Data & Airtime Spend Tracker",
                    trailingAction = {
                        IconButton(
                            onClick = { viewModel.openSettings() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LightGreyFill)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            // 2. Budget Warning Banner (If 80% or 100% reached)
            if (monthlyBudget != null) {
                if (isBudgetExceeded) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AlertRedContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .testTag("budget_exceeded_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = AlertRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "100% Budget Limit Reached!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertRed
                                    )
                                    Text(
                                        text = "You've spent ${FormatUtils.formatCurrency(thisMonthSpend)} of your ${FormatUtils.formatCurrency(monthlyBudget!!)} limit. Status bar alert is active.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AlertRed.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                } else if (isBudget80PercentReached) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningAmberContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .testTag("budget_warning_80_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "80% Budget Limit Reached",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                    Text(
                                        text = "You've spent ${FormatUtils.formatCurrency(thisMonthSpend)} of your ${FormatUtils.formatCurrency(monthlyBudget!!)} limit (${budgetPercentage.toInt()}% used). Persistent status bar alert is active.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningAmber.copy(alpha = 0.95f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Top Summary Cards (This Week & This Month) with subtle fade-in & slide animation
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .graphicsLayer {
                            alpha = cardsAlpha.value
                            translationY = cardsTranslationY.value
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // This Week Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyLightSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "THIS WEEK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AnimatedContent(
                                targetState = FormatUtils.formatCurrency(thisWeekSpend),
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
                                },
                                label = "this_week_spend_anim"
                            ) { formattedSpend ->
                                Text(
                                    text = formattedSpend,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }

                    // This Month Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "THIS MONTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AnimatedContent(
                                targetState = FormatUtils.formatCurrency(thisMonthSpend),
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
                                },
                                label = "this_month_spend_anim"
                            ) { formattedSpend ->
                                Text(
                                    text = formattedSpend,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Quick Universal USSD Section (*310#, *312#, *323#)
            item {
                UssdQuickSection()
            }

            // 5. Last 7 Days Compose Canvas Bar Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HomeBarChart(days = last7Days)
                }
            }

            // 5. Segmented Control Toggle: All / Data / Airtime
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = "Recent Recharges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(LightGreyFill, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filterOptions = listOf(
                            TypeFilter.ALL to "All",
                            TypeFilter.DATA to "Data",
                            TypeFilter.AIRTIME to "Airtime"
                        )
                        filterOptions.forEach { (type, label) ->
                            val isSelected = homeFilter == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(
                                        color = if (isSelected) NavyPrimary else PureWhite.copy(alpha = 0f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setHomeTypeFilter(type) }
                                    .testTag("home_filter_$label"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recent Recharges List or Empty State
            if (recentRecharges.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.PhoneAndroid,
                        title = "No recharges yet",
                        message = "Log your airtime or data purchases to see spend trends and stay on budget.",
                        actionButton = {
                            Button(
                                onClick = { viewModel.openAddRecharge() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavyPrimary,
                                    contentColor = PureWhite
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log First Recharge")
                            }
                        }
                    )
                }
            } else {
                groupedRecharges.forEach { (dateGroup, itemsForDate) ->
                    item(key = "header_$dateGroup") {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    items(
                        items = itemsForDate,
                        key = { it.id }
                    ) { recharge ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            RechargeItemCard(
                                recharge = recharge,
                                onClick = { viewModel.openEditRecharge(recharge) }
                            )
                        }
                    }
                }
            }
        }
    }
}
