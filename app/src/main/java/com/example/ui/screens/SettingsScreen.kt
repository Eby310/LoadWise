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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Constants
import com.example.service.BudgetMonitorService
import com.example.ui.components.UssdDirectorySheet
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FormatUtils

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentBudget by viewModel.monthlyBudget.collectAsState()
    val customNetworks by viewModel.customNetworks.collectAsState()
    val thisMonthSpend by viewModel.thisMonthSpend.collectAsState()
    val budgetPercentage by viewModel.budgetPercentage.collectAsState()
    val isBudget80PercentReached by viewModel.isBudget80PercentReached.collectAsState()
    val isBudgetExceeded by viewModel.isBudgetExceeded.collectAsState()

    var budgetInput by remember(currentBudget) {
        mutableStateOf(currentBudget?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        } ?: "")
    }
    var isBudgetEnabled by remember(currentBudget) {
        mutableStateOf(currentBudget != null && currentBudget!! > 0)
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAddNetworkDialog by remember { mutableStateOf(false) }
    var showUssdDirectory by remember { mutableStateOf(false) }
    var newNetworkInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = PureWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header with back button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LightGreyFill)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Preferences & data management",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Section 1: Monthly Budget Limit
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LightGreyFill),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Monthly Spending Limit",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "Warn when spending exceeds limit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = isBudgetEnabled,
                                onCheckedChange = { checked ->
                                    isBudgetEnabled = checked
                                    if (!checked) {
                                        viewModel.setMonthlyBudget(null)
                                    } else {
                                        val budgetVal = budgetInput.toDoubleOrNull()
                                        if (budgetVal != null && budgetVal > 0) {
                                            viewModel.setMonthlyBudget(budgetVal)
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = NavyPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = LightGreyFill
                                )
                            )
                        }

                        if (isBudgetEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Budget Amount (${Constants.CURRENCY_SYMBOL})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = budgetInput,
                                    onValueChange = {
                                        budgetInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    },
                                    placeholder = { Text("e.g. 15000") },
                                    leadingIcon = {
                                        Text(
                                            text = Constants.CURRENCY_SYMBOL,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NavyPrimary,
                                        unfocusedBorderColor = BorderGrey
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("budget_input_field")
                                )
                                Button(
                                    onClick = {
                                        val budgetVal = budgetInput.toDoubleOrNull()
                                        viewModel.setMonthlyBudget(budgetVal)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("Set", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Budget Alert Status & Details
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isBudgetExceeded -> AlertRedContainer
                                        isBudget80PercentReached -> WarningAmberContainer
                                        else -> LightGreyFill
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = when {
                                                    isBudgetExceeded -> AlertRed
                                                    isBudget80PercentReached -> WarningAmber
                                                    else -> NavyPrimary
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Status Bar Alert Service",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isBudgetExceeded -> AlertRed
                                                    isBudget80PercentReached -> WarningAmber
                                                    else -> NavyPrimary
                                                }
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                BudgetMonitorService.checkBudget(context)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Check Now",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            isBudgetExceeded -> "🚨 100% Limit Reached — Persistent notification is active in the status bar (${FormatUtils.formatCurrency(thisMonthSpend)} spent of ${FormatUtils.formatCurrency(currentBudget ?: 0.0)})."
                                            isBudget80PercentReached -> "⚠️ 80% Warning — Persistent notification is active in the status bar (${FormatUtils.formatCurrency(thisMonthSpend)} spent, ${budgetPercentage.toInt()}% of budget)."
                                            else -> "✅ Active — Displays a persistent notification in the status bar if spending reaches 80% or 100% of your limit (currently at ${budgetPercentage.toInt()}%)."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isBudgetExceeded -> AlertRed.copy(alpha = 0.9f)
                                            isBudget80PercentReached -> WarningAmber.copy(alpha = 0.95f)
                                            else -> TextSecondary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Manage Custom Networks
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Custom Networks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Default: MTN, Airtel, Glo, 9mobile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { showAddNetworkDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightGreyFill),
                                elevation = null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (customNetworks.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No custom networks added yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                customNetworks.forEach { network ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(LightGreyFill.copy(alpha = 0.6f))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = network,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeCustomNetwork(network) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove network",
                                                tint = AlertRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Universal USSD Directory
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NavyContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dialpad,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Universal USSD Codes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "Standard NCC codes (*310#, *312#, *323#)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { showUssdDirectory = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("Open", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 4: Data Management & Privacy
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Data & Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All data is stored locally on this device. No network calls or accounts required.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showClearDataDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRedContainer,
                                contentColor = AlertRed
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("clear_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear All Records",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 4: App Info
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Loadwise v1.0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Native Android Data & Airtime Spend Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }

    // Add Custom Network Dialog
    if (showAddNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showAddNetworkDialog = false },
            title = {
                Text(
                    text = "New Network Provider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = newNetworkInput,
                    onValueChange = { newNetworkInput = it },
                    placeholder = { Text("e.g. Spectranet, Starlink, Smile") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = BorderGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newNetworkInput.trim()
                        if (clean.isNotBlank()) {
                            viewModel.addCustomNetwork(clean)
                            newNetworkInput = ""
                            showAddNetworkDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNetworkDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Clear All Data?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all logged recharge transactions and custom settings? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        viewModel.clearAllData()
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Clear Everything", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showUssdDirectory) {
        UssdDirectorySheet(
            onDismiss = { showUssdDirectory = false }
        )
    }
}
