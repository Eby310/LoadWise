package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Constants
import com.example.data.db.AppDatabase
import com.example.data.model.Recharge
import com.example.data.model.RechargeType
import com.example.data.repository.RechargeRepository
import com.example.data.repository.SettingsRepository
import com.example.service.BudgetMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class DaySpend(
    val dayLabel: String,
    val dayNumber: String,
    val amount: Double,
    val isToday: Boolean,
    val timestamp: Long
)

data class MonthTrend(
    val monthLabel: String,
    val year: Int,
    val totalSpend: Double,
    val dataSpend: Double,
    val airtimeSpend: Double
)

data class NetworkSpend(
    val network: String,
    val amount: Double,
    val percentage: Float
)

data class TypeBreakdown(
    val dataSpend: Double,
    val dataPercentage: Float,
    val airtimeSpend: Double,
    val airtimePercentage: Float,
    val dataCount: Int,
    val airtimeCount: Int
)

enum class ScreenTab {
    HOME,
    HISTORY,
    INSIGHTS
}

enum class TypeFilter {
    ALL,
    DATA,
    AIRTIME
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val rechargeRepository = RechargeRepository(database.rechargeDao())
    private val settingsRepository = SettingsRepository(application)

    // Room flow
    val allRecharges: StateFlow<List<Recharge>> = rechargeRepository.allRecharges
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Settings flows
    val monthlyBudget: StateFlow<Double?> = settingsRepository.monthlyBudget
    val customNetworks: StateFlow<List<String>> = settingsRepository.customNetworks

    init {
        BudgetMonitorService.checkBudget(application)
    }

    // All available networks = Default + Custom
    val allNetworks: StateFlow<List<String>> = customNetworks.combine(
        MutableStateFlow(Constants.DEFAULT_NETWORKS)
    ) { custom, defaults ->
        val list = defaults.toMutableList()
        custom.forEach { if (!list.contains(it)) list.add(it) }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Constants.DEFAULT_NETWORKS)

    // Navigation & Sheet States
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _editingRecharge = MutableStateFlow<Recharge?>(null)
    val editingRecharge: StateFlow<Recharge?> = _editingRecharge.asStateFlow()

    // Home screen filter
    private val _homeTypeFilter = MutableStateFlow(TypeFilter.ALL)
    val homeTypeFilter: StateFlow<TypeFilter> = _homeTypeFilter.asStateFlow()

    // History screen filters
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyTypeFilter = MutableStateFlow(TypeFilter.ALL)
    val historyTypeFilter: StateFlow<TypeFilter> = _historyTypeFilter.asStateFlow()

    private val _historyNetworkFilter = MutableStateFlow("All")
    val historyNetworkFilter: StateFlow<String> = _historyNetworkFilter.asStateFlow()

    // Filtered recharges for Home
    val homeFilteredRecharges: StateFlow<List<Recharge>> = combine(
        allRecharges,
        homeTypeFilter
    ) { list, filter ->
        when (filter) {
            TypeFilter.ALL -> list
            TypeFilter.DATA -> list.filter { it.type == RechargeType.DATA }
            TypeFilter.AIRTIME -> list.filter { it.type == RechargeType.AIRTIME }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered recharges for History
    val historyFilteredRecharges: StateFlow<List<Recharge>> = combine(
        allRecharges,
        historySearchQuery,
        historyTypeFilter,
        historyNetworkFilter
    ) { list, query, typeFilter, networkFilter ->
        list.filter { recharge ->
            val matchesQuery = query.isBlank() ||
                    recharge.network.contains(query, ignoreCase = true) ||
                    (recharge.note?.contains(query, ignoreCase = true) == true)

            val matchesType = when (typeFilter) {
                TypeFilter.ALL -> true
                TypeFilter.DATA -> recharge.type == RechargeType.DATA
                TypeFilter.AIRTIME -> recharge.type == RechargeType.AIRTIME
            }

            val matchesNetwork = networkFilter == "All" ||
                    recharge.network.equals(networkFilter, ignoreCase = true)

            matchesQuery && matchesType && matchesNetwork
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculations
    val thisWeekSpend: StateFlow<Double> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateThisWeekSpend(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val thisMonthSpend: StateFlow<Double> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateThisMonthSpend(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val budgetPercentage: StateFlow<Float> = combine(
        thisMonthSpend,
        monthlyBudget
    ) { monthSpend, budget ->
        if (budget != null && budget > 0.0) ((monthSpend / budget) * 100.0).toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val isBudget80PercentReached: StateFlow<Boolean> = combine(
        thisMonthSpend,
        monthlyBudget
    ) { monthSpend, budget ->
        if (budget != null && budget > 0.0) {
            val pct = (monthSpend / budget) * 100.0
            pct >= 80.0 && pct < 100.0
        } else false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBudgetExceeded: StateFlow<Boolean> = combine(
        thisMonthSpend,
        monthlyBudget
    ) { monthSpend, budget ->
        budget != null && budget > 0.0 && monthSpend >= budget
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Home 7-day Bar Chart Data
    val last7DaysSpend: StateFlow<List<DaySpend>> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateLast7DaysSpend(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insights: Monthly Trend (last 6 months)
    val monthlyTrend: StateFlow<List<MonthTrend>> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateMonthlyTrend(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insights: Network Breakdown
    val networkBreakdown: StateFlow<List<NetworkSpend>> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateNetworkBreakdown(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insights: Data vs Airtime Breakdown
    val typeBreakdown: StateFlow<TypeBreakdown> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateTypeBreakdown(list)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TypeBreakdown(0.0, 0f, 0.0, 0f, 0, 0)
    )

    // Insights: Average Weekly Spend
    val averageWeeklySpend: StateFlow<Double> = allRecharges.combine(MutableStateFlow(Unit)) { list, _ ->
        calculateAverageWeeklySpend(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Actions
    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun openAddRecharge() {
        _editingRecharge.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditRecharge(recharge: Recharge) {
        _editingRecharge.value = recharge
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _editingRecharge.value = null
    }

    fun setHomeTypeFilter(filter: TypeFilter) {
        _homeTypeFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryTypeFilter(filter: TypeFilter) {
        _historyTypeFilter.value = filter
    }

    fun setHistoryNetworkFilter(network: String) {
        _historyNetworkFilter.value = network
    }

    fun saveRecharge(
        id: Long = 0,
        type: RechargeType,
        network: String,
        amount: Double,
        dataSizeMb: Int?,
        date: Long,
        note: String?
    ) {
        viewModelScope.launch {
            val trimmedNote = note?.trim()?.ifBlank { null }
            val cleanNetwork = network.trim().ifBlank { "Other" }
            val item = Recharge(
                id = id,
                type = type,
                network = cleanNetwork,
                amount = amount,
                dataSizeMb = if (type == RechargeType.DATA) dataSizeMb else null,
                date = date,
                note = trimmedNote
            )
            if (id == 0L) {
                rechargeRepository.insert(item)
            } else {
                rechargeRepository.update(item)
            }
            BudgetMonitorService.checkBudget(getApplication())
            closeAddEditSheet()
        }
    }

    fun deleteRecharge(recharge: Recharge) {
        viewModelScope.launch {
            rechargeRepository.delete(recharge)
            BudgetMonitorService.checkBudget(getApplication())
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            rechargeRepository.clearAll()
            settingsRepository.clearAllSettings()
            BudgetMonitorService.cancelNotification(getApplication())
            _isSettingsOpen.value = false
        }
    }

    fun setMonthlyBudget(budget: Double?) {
        settingsRepository.setMonthlyBudget(budget)
        BudgetMonitorService.checkBudget(getApplication())
    }

    fun addCustomNetwork(name: String) {
        settingsRepository.addCustomNetwork(name)
    }

    fun removeCustomNetwork(name: String) {
        settingsRepository.removeCustomNetwork(name)
    }

    // Calculations helpers
    private fun calculateThisWeekSpend(recharges: List<Recharge>): Double {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = cal.timeInMillis
        return recharges.filter { it.date >= startOfWeek }.sumOf { it.amount }
    }

    private fun calculateThisMonthSpend(recharges: List<Recharge>): Double {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis
        return recharges.filter { it.date >= startOfMonth }.sumOf { it.amount }
    }

    private fun calculateLast7DaysSpend(recharges: List<Recharge>): List<DaySpend> {
        val days = mutableListOf<DaySpend>()
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Generate for 6 days ago up to today (7 days total)
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = todayCal.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

            val daySpend = recharges.filter { it.date in startOfDay..endOfDay }.sumOf { it.amount }
            val dayLabel = dayCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
            val dayNumber = dayCal.get(Calendar.DAY_OF_MONTH).toString()

            days.add(
                DaySpend(
                    dayLabel = dayLabel,
                    dayNumber = dayNumber,
                    amount = daySpend,
                    isToday = (i == 0),
                    timestamp = startOfDay
                )
            )
        }
        return days
    }

    private fun calculateMonthlyTrend(recharges: List<Recharge>): List<MonthTrend> {
        val list = mutableListOf<MonthTrend>()
        val now = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (i in 5 downTo 0) {
            val mCal = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.MONTH, -i)
            }
            val startOfMonth = mCal.timeInMillis
            val endCal = Calendar.getInstance().apply {
                timeInMillis = startOfMonth
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfMonth = endCal.timeInMillis

            val inMonth = recharges.filter { it.date in startOfMonth..endOfMonth }
            val total = inMonth.sumOf { it.amount }
            val data = inMonth.filter { it.type == RechargeType.DATA }.sumOf { it.amount }
            val airtime = inMonth.filter { it.type == RechargeType.AIRTIME }.sumOf { it.amount }

            val label = mCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
            list.add(
                MonthTrend(
                    monthLabel = label,
                    year = mCal.get(Calendar.YEAR),
                    totalSpend = total,
                    dataSpend = data,
                    airtimeSpend = airtime
                )
            )
        }
        return list
    }

    private fun calculateNetworkBreakdown(recharges: List<Recharge>): List<NetworkSpend> {
        if (recharges.isEmpty()) return emptyList()
        val total = recharges.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        val grouped = recharges.groupBy { it.network }
        return grouped.map { (net, items) ->
            val sum = items.sumOf { it.amount }
            val pct = ((sum / total) * 100).toFloat()
            NetworkSpend(network = net, amount = sum, percentage = pct)
        }.sortedByDescending { it.amount }
    }

    private fun calculateTypeBreakdown(recharges: List<Recharge>): TypeBreakdown {
        if (recharges.isEmpty()) {
            return TypeBreakdown(0.0, 0f, 0.0, 0f, 0, 0)
        }
        val total = recharges.sumOf { it.amount }
        val dataItems = recharges.filter { it.type == RechargeType.DATA }
        val airtimeItems = recharges.filter { it.type == RechargeType.AIRTIME }

        val dataSpend = dataItems.sumOf { it.amount }
        val airtimeSpend = airtimeItems.sumOf { it.amount }

        val dataPct = if (total > 0) ((dataSpend / total) * 100).toFloat() else 0f
        val airtimePct = if (total > 0) ((airtimeSpend / total) * 100).toFloat() else 0f

        return TypeBreakdown(
            dataSpend = dataSpend,
            dataPercentage = dataPct,
            airtimeSpend = airtimeSpend,
            airtimePercentage = airtimePct,
            dataCount = dataItems.size,
            airtimeCount = airtimeItems.size
        )
    }

    private fun calculateAverageWeeklySpend(recharges: List<Recharge>): Double {
        if (recharges.isEmpty()) return 0.0
        val total = recharges.sumOf { it.amount }
        val earliest = recharges.minOf { it.date }
        val now = System.currentTimeMillis()
        val diffWeeks = ((now - earliest) / (1000L * 60 * 60 * 24 * 7)).coerceAtLeast(1L)
        return total / diffWeeks.toDouble()
    }
}
