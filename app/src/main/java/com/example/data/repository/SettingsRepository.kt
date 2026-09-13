package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("loadwise_settings", Context.MODE_PRIVATE)

    private val _monthlyBudget = MutableStateFlow<Double?>(loadMonthlyBudget())
    val monthlyBudget: StateFlow<Double?> = _monthlyBudget.asStateFlow()

    private val _customNetworks = MutableStateFlow<List<String>>(loadCustomNetworks())
    val customNetworks: StateFlow<List<String>> = _customNetworks.asStateFlow()

    fun getMonthlyBudget(): Double? = loadMonthlyBudget()

    private fun loadMonthlyBudget(): Double? {
        val raw = prefs.getString(KEY_MONTHLY_BUDGET, null)
        return raw?.toDoubleOrNull()
    }

    private fun loadCustomNetworks(): List<String> {
        val set = prefs.getStringSet(KEY_CUSTOM_NETWORKS, emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    fun setMonthlyBudget(budget: Double?) {
        if (budget == null || budget <= 0.0) {
            prefs.edit().remove(KEY_MONTHLY_BUDGET).apply()
            _monthlyBudget.value = null
        } else {
            prefs.edit().putString(KEY_MONTHLY_BUDGET, budget.toString()).apply()
            _monthlyBudget.value = budget
        }
    }

    fun addCustomNetwork(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val current = _customNetworks.value.toMutableSet()
        if (current.add(cleanName)) {
            prefs.edit().putStringSet(KEY_CUSTOM_NETWORKS, current).apply()
            _customNetworks.value = current.toList().sorted()
        }
    }

    fun removeCustomNetwork(name: String) {
        val current = _customNetworks.value.toMutableSet()
        if (current.remove(name)) {
            prefs.edit().putStringSet(KEY_CUSTOM_NETWORKS, current).apply()
            _customNetworks.value = current.toList().sorted()
        }
    }

    fun clearAllSettings() {
        prefs.edit().clear().apply()
        _monthlyBudget.value = null
        _customNetworks.value = emptyList()
    }

    companion object {
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_CUSTOM_NETWORKS = "key_custom_networks"
    }
}
