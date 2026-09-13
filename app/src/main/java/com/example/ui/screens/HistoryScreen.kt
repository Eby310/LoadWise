package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Recharge
import com.example.ui.components.AppHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipRow
import com.example.ui.components.PillSearchBar
import com.example.ui.components.RechargeItemCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TypeFilter
import com.example.util.FormatUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val typeFilter by viewModel.historyTypeFilter.collectAsState()
    val networkFilter by viewModel.historyNetworkFilter.collectAsState()
    val allNetworks by viewModel.allNetworks.collectAsState()
    val filteredRecharges by viewModel.historyFilteredRecharges.collectAsState()
    val allRecharges by viewModel.allRecharges.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var rechargeToDelete by remember { mutableStateOf<Recharge?>(null) }

    val networkOptions = remember(allNetworks) {
        listOf("All") + allNetworks
    }

    val groupedRecharges = remember(filteredRecharges) {
        filteredRecharges.groupBy { FormatUtils.formatDateGroup(it.date) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    title = "History",
                    subtitle = "${allRecharges.size} total transactions logged"
                )
            }

            // Pill Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PillSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setHistorySearchQuery(it) },
                        placeholder = "Search note or network..."
                    )
                }
            }

            // Type Filter Chip Row
            item {
                Spacer(modifier = Modifier.height(6.dp))
                FilterChipRow(
                    items = listOf("All", "Data", "Airtime"),
                    selectedItem = when (typeFilter) {
                        TypeFilter.ALL -> "All"
                        TypeFilter.DATA -> "Data"
                        TypeFilter.AIRTIME -> "Airtime"
                    },
                    onItemSelected = { item ->
                        val filter = when (item) {
                            "Data" -> TypeFilter.DATA
                            "Airtime" -> TypeFilter.AIRTIME
                            else -> TypeFilter.ALL
                        }
                        viewModel.setHistoryTypeFilter(filter)
                    }
                )
            }

            // Network Filter Chip Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipRow(
                    items = networkOptions,
                    selectedItem = networkFilter,
                    onItemSelected = { viewModel.setHistoryNetworkFilter(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Empty States
            if (allRecharges.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "No history yet",
                        message = "Your logged airtime and data recharges will appear here."
                    )
                }
            } else if (filteredRecharges.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.SearchOff,
                        title = "No matches found",
                        message = "Try clearing your search query or selecting another filter.",
                        actionButton = {
                            Button(
                                onClick = {
                                    viewModel.setHistorySearchQuery("")
                                    viewModel.setHistoryTypeFilter(TypeFilter.ALL)
                                    viewModel.setHistoryNetworkFilter("All")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text("Reset Filters", color = PureWhite)
                            }
                        }
                    )
                }
            } else {
                groupedRecharges.forEach { (dateGroup, itemsForDate) ->
                    item(key = "history_header_$dateGroup") {
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
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                                    rechargeToDelete = recharge
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        else -> AlertRed
                                    },
                                    label = "dismiss_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = PureWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            content = {
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                    RechargeItemCard(
                                        recharge = recharge,
                                        onClick = { viewModel.openEditRecharge(recharge) }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for deletion
    if (rechargeToDelete != null) {
        val target = rechargeToDelete!!
        AlertDialog(
            onDismissRequest = { rechargeToDelete = null },
            title = {
                Text(
                    text = "Delete Recharge?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this ${target.network} recharge of ${FormatUtils.formatCurrency(target.amount)}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecharge(target)
                        rechargeToDelete = null
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Recharge deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.saveRecharge(
                                    id = 0,
                                    type = target.type,
                                    network = target.network,
                                    amount = target.amount,
                                    dataSizeMb = target.dataSizeMb,
                                    date = target.date,
                                    note = target.note
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Delete", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { rechargeToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
