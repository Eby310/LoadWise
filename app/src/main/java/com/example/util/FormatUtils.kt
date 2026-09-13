package com.example.util

import com.example.data.Constants
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val currencyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun formatCurrency(amount: Double): String {
        return "${Constants.CURRENCY_SYMBOL}${currencyFormat.format(amount)}"
    }

    fun formatNaira(amount: Double): String = formatCurrency(amount)

    fun formatDateGroup(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val isToday = isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            isSameYear -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatMonthYear(calendar: Calendar): String {
        return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    fun formatMonthShort(calendar: Calendar): String {
        return SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    }

    fun formatDataSize(mb: Int?): String {
        if (mb == null || mb <= 0) return ""
        return if (mb >= 1000) {
            val gb = mb / 1000.0
            if (gb % 1.0 == 0.0) {
                "${gb.toInt()} GB"
            } else {
                String.format(Locale.US, "%.1f GB", gb)
            }
        } else {
            "$mb MB"
        }
    }
}
