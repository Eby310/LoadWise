package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Constants
import com.example.data.model.Recharge
import com.example.data.model.RechargeType
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyLightSurface
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils
import com.example.util.UssdHelper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRechargeSheet(
    initialRecharge: Recharge?,
    availableNetworks: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        type: RechargeType,
        network: String,
        amount: Double,
        dataSizeMb: Int?,
        date: Long,
        note: String?
    ) -> Unit,
    onAddNewNetwork: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var selectedType by remember {
        mutableStateOf(initialRecharge?.type ?: RechargeType.DATA)
    }
    var selectedNetwork by remember {
        mutableStateOf(
            initialRecharge?.network ?: availableNetworks.firstOrNull() ?: "MTN"
        )
    }
    var amountText by remember {
        mutableStateOf(if (initialRecharge != null) initialRecharge.amount.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        } else "")
    }

    // Data size: unit toggle (MB vs GB)
    val initialIsGb = remember {
        initialRecharge?.dataSizeMb?.let { it >= 1000 && it % 1000 == 0 } ?: true
    }
    var isGbUnit by remember { mutableStateOf(initialIsGb) }
    var dataSizeText by remember {
        mutableStateOf(
            initialRecharge?.dataSizeMb?.let { mb ->
                if (mb >= 1000 && mb % 1000 == 0) (mb / 1000).toString()
                else if (mb >= 1000) String.format("%.1f", mb / 1000.0)
                else mb.toString()
            } ?: ""
        )
    }

    var selectedTimestamp by remember {
        mutableLongStateOf(initialRecharge?.date ?: System.currentTimeMillis())
    }
    var noteText by remember {
        mutableStateOf(initialRecharge?.note ?: "")
    }

    var showCustomNetworkDialog by remember { mutableStateOf(false) }
    var customNetworkInput by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    // Subtle floating animation for the bottom action button
    val buttonInfiniteTransition = rememberInfiniteTransition(label = "save_button_float_anim")
    val buttonHoverOffsetY by buttonInfiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "save_btn_hover"
    )
    val buttonElevation by buttonInfiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "save_btn_elevation"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .background(BorderGrey, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialRecharge == null) "Log Recharge" else "Edit Recharge",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Segmented Toggle: Data or Airtime
            Text(
                text = "Recharge Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(LightGreyFill, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val types = listOf(RechargeType.DATA to "Data", RechargeType.AIRTIME to "Airtime")
                types.forEach { (type, label) ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (isSelected) NavyPrimary else PureWhite.copy(alpha = 0f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = type }
                            .testTag("toggle_type_${type.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureWhite else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contextual USSD Quick Dial Bar (*310# for Airtime, *312# & *323# for Data)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavyLightSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (selectedType == RechargeType.AIRTIME) "Balance code: *310#" else "Buy: *312# • Bal: *323#",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedType == RechargeType.AIRTIME) {
                            Surface(
                                onClick = { UssdHelper.dialUssd(context, "*310#") },
                                shape = RoundedCornerShape(8.dp),
                                color = NavyPrimary,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Dial *310#",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }
                        } else {
                            Surface(
                                onClick = { UssdHelper.dialUssd(context, "*312#") },
                                shape = RoundedCornerShape(8.dp),
                                color = NavyPrimary,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Dial *312#",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }

                            Surface(
                                onClick = { UssdHelper.dialUssd(context, "*323#") },
                                shape = RoundedCornerShape(8.dp),
                                color = LightGreyFill,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "*323#",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Network Selector (Chip row + Add Custom)
            Text(
                text = "Select Network",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableNetworks.forEach { network ->
                    val isSelected = selectedNetwork.equals(network, ignoreCase = true)
                    Surface(
                        onClick = { selectedNetwork = network },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavyPrimary else PureWhite,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = network,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) PureWhite else TextPrimary
                            )
                        }
                    }
                }

                // Add Custom Network Button Chip
                Surface(
                    onClick = { showCustomNetworkDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = LightGreyFill,
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add custom network",
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelMedium,
                            color = NavyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Amount Input (Numeric keypad)
            Text(
                text = "Amount (${Constants.CURRENCY_SYMBOL})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { char -> char.isDigit() || char == '.' }
                    amountError = false
                },
                placeholder = { Text("0.00", color = TextMuted) },
                leadingIcon = {
                    Text(
                        text = Constants.CURRENCY_SYMBOL,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Please enter a valid amount greater than zero", color = AlertRed) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderGrey,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = LightGreyFill.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            // 4. Data Size Input (If Data selected)
            if (selectedType == RechargeType.DATA) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data Bundle Size (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    // MB / GB toggle
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .background(LightGreyFill, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f, false)
                                .height(24.dp)
                                .background(
                                    if (isGbUnit) NavyPrimary else PureWhite.copy(alpha = 0f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { isGbUnit = true }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isGbUnit) PureWhite else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f, false)
                                .height(24.dp)
                                .background(
                                    if (!isGbUnit) NavyPrimary else PureWhite.copy(alpha = 0f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { isGbUnit = false }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isGbUnit) PureWhite else TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataSizeText,
                    onValueChange = {
                        dataSizeText = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    placeholder = { Text(if (isGbUnit) "e.g. 1.5" else "e.g. 1500", color = TextMuted) },
                    trailingIcon = {
                        Text(
                            text = if (isGbUnit) "GB" else "MB",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = LightGreyFill.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("data_size_input")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Date Picker
            Text(
                text = "Recharge Date",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = selectedTimestamp
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            selectedTimestamp = newCal.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                shape = RoundedCornerShape(12.dp),
                color = LightGreyFill.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("date_picker_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FormatUtils.formatDateOnly(selectedTimestamp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Optional Note Field
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("e.g. Emergency bundle, Night plan", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderGrey,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = LightGreyFill.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        amountError = true
                        return@Button
                    }

                    val finalMb: Int? = if (selectedType == RechargeType.DATA) {
                        val num = dataSizeText.toDoubleOrNull()
                        if (num != null && num > 0) {
                            if (isGbUnit) (num * 1000).toInt() else num.toInt()
                        } else null
                    } else null

                    onSave(
                        initialRecharge?.id ?: 0L,
                        selectedType,
                        selectedNetwork,
                        amountVal,
                        finalMb,
                        selectedTimestamp,
                        noteText
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = PureWhite
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = buttonElevation.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .offset(y = buttonHoverOffsetY.dp)
                    .testTag("save_recharge_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (initialRecharge == null) Icons.Default.Add else Icons.Default.Check,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (initialRecharge == null) {
                            if (selectedType == RechargeType.DATA) "Log Data" else "Log Airtime"
                        } else {
                            "Update Recharge"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Add Custom Network Dialog
    if (showCustomNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showCustomNetworkDialog = false },
            title = {
                Text(
                    text = "Add Custom Network",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = customNetworkInput,
                    onValueChange = { customNetworkInput = it },
                    placeholder = { Text("e.g. Spectranet, Smile, Starlink") },
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
                        val trimmed = customNetworkInput.trim()
                        if (trimmed.isNotBlank()) {
                            onAddNewNetwork(trimmed)
                            selectedNetwork = trimmed
                            customNetworkInput = ""
                            showCustomNetworkDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomNetworkDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
