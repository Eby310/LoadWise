package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddEditRechargeSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyLightSurface
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

@Composable
fun LoadwiseApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isAddEditSheetOpen by viewModel.isAddEditSheetOpen.collectAsState()
    val editingRecharge by viewModel.editingRecharge.collectAsState()
    val allNetworks by viewModel.allNetworks.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (isSettingsOpen) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSettings() }
            )
        } else {
            Scaffold(
                bottomBar = {
                    LoadwiseBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                },
                containerColor = PureWhite
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_navigation_transition",
                    modifier = Modifier.padding(innerPadding)
                ) { tab ->
                    when (tab) {
                        ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                        ScreenTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                        ScreenTab.INSIGHTS -> InsightsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Add/Edit Bottom Sheet
        if (isAddEditSheetOpen) {
            AddEditRechargeSheet(
                initialRecharge = editingRecharge,
                availableNetworks = allNetworks,
                onDismiss = { viewModel.closeAddEditSheet() },
                onSave = { id, type, network, amount, dataSizeMb, date, note ->
                    viewModel.saveRecharge(id, type, network, amount, dataSizeMb, date, note)
                },
                onAddNewNetwork = { viewModel.addCustomNetwork(it) }
            )
        }
    }
}

@Composable
fun LoadwiseBottomNavigation(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(ScreenTab.HOME, Icons.Default.Home, "Home"),
                Triple(ScreenTab.HISTORY, Icons.Default.ReceiptLong, "History"),
                Triple(ScreenTab.INSIGHTS, Icons.Default.Insights, "Insights")
            )

            tabs.forEach { (tab, icon, label) ->
                val isSelected = currentTab == tab

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NavyLightSurface else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .testTag("nav_tab_$label"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) NavyPrimary else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
