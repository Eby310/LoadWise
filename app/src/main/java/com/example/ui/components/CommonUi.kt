package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recharge
import com.example.data.model.RechargeType
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightGreyFill
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Network9mobile
import com.example.ui.theme.NetworkAirtel
import com.example.ui.theme.NetworkGlo
import com.example.ui.theme.NetworkMtn
import com.example.ui.theme.NetworkOther
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        if (trailingAction != null) {
            trailingAction()
        }
    }
}

@Composable
fun PillSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search recharges or notes...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(LightGreyFill)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    cursorBrush = SolidColor(NavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipRow(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item.equals(selectedItem, ignoreCase = true)
            Surface(
                onClick = { onItemSelected(item) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) NavyPrimary else PureWhite,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("filter_chip_$item")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) PureWhite else TextPrimary
                    )
                }
            }
        }
    }
}

fun getNetworkColor(network: String): Color {
    return when (network.trim().uppercase()) {
        "MTN" -> NetworkMtn
        "AIRTEL" -> NetworkAirtel
        "GLO" -> NetworkGlo
        "9MOBILE" -> Network9mobile
        else -> NetworkOther
    }
}

@Composable
fun RechargeItemCard(
    recharge: Recharge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharge_card_${recharge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Avatar on the left
            val netColor = getNetworkColor(recharge.network)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(LightGreyFill)
                    .border(1.5.dp, netColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon: ImageVector = if (recharge.type == RechargeType.DATA) {
                    Icons.Default.DataUsage
                } else {
                    Icons.Default.Call
                }
                Icon(
                    imageVector = icon,
                    contentDescription = recharge.type.name,
                    tint = NavyPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                val titleText = if (recharge.type == RechargeType.DATA) {
                    val sizeStr = FormatUtils.formatDataSize(recharge.dataSizeMb)
                    if (sizeStr.isNotBlank()) "${recharge.network} • $sizeStr" else "${recharge.network} • Data"
                } else {
                    "${recharge.network} • Airtime"
                }

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val subDate = FormatUtils.formatFullDate(recharge.date)
                val subtitle = if (!recharge.note.isNullOrBlank()) {
                    "$subDate • \"${recharge.note}\""
                } else {
                    subDate
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Value or price aligned right in bold navy
            Text(
                text = FormatUtils.formatCurrency(recharge.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionButton: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LightGreyFill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = NavyPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(16.dp))
            actionButton()
        }
    }
}
