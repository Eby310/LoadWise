package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.repository.SettingsRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Service that checks the monthly total against the budget and displays a persistent
 * notification in the status bar if the user has reached 80% or 100% of their set limit.
 */
class BudgetMonitorService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            checkBudgetAndNotify(applicationContext)
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        const val CHANNEL_ID = "loadwise_budget_alerts"
        const val NOTIFICATION_ID = 2001
        const val ACTION_CHECK_BUDGET = "com.example.service.CHECK_BUDGET"

        /**
         * Triggers the BudgetMonitorService to evaluate the current month's spending.
         */
        fun checkBudget(context: Context) {
            val intent = Intent(context, BudgetMonitorService::class.java).apply {
                action = ACTION_CHECK_BUDGET
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // Fallback for background restrictions or test environments
                CoroutineScope(Dispatchers.IO).launch {
                    checkBudgetAndNotify(context.applicationContext)
                }
            }
        }

        /**
         * Evaluates current monthly spend against budget and posts or cancels persistent status bar notification.
         */
        suspend fun checkBudgetAndNotify(context: Context) {
            val db = AppDatabase.getInstance(context)
            val settingsRepo = SettingsRepository(context)

            val budget = settingsRepo.getMonthlyBudget()
            if (budget == null || budget <= 0.0) {
                cancelNotification(context)
                return
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfMonth = cal.timeInMillis

            val endCal = Calendar.getInstance().apply {
                timeInMillis = startOfMonth
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfMonth = endCal.timeInMillis

            val monthlySpend = db.rechargeDao().getMonthlySpend(startOfMonth, endOfMonth) ?: 0.0
            val percent = if (budget > 0.0) (monthlySpend / budget) * 100.0 else 0.0

            when {
                monthlySpend >= budget -> {
                    // 100% or more reached
                    showPersistentNotification(
                        context = context,
                        is100Percent = true,
                        monthlySpend = monthlySpend,
                        budget = budget,
                        percent = percent
                    )
                }
                percent >= 80.0 -> {
                    // 80% to 99.9% reached
                    showPersistentNotification(
                        context = context,
                        is100Percent = false,
                        monthlySpend = monthlySpend,
                        budget = budget,
                        percent = percent
                    )
                }
                else -> {
                    // Below 80%, cancel any existing persistent notification
                    cancelNotification(context)
                }
            }
        }

        private fun showPersistentNotification(
            context: Context,
            is100Percent: Boolean,
            monthlySpend: Double,
            budget: Double,
            percent: Double
        ) {
            createNotificationChannel(context)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (is100Percent) {
                "⚠️ Budget Limit Reached (100%)"
            } else {
                "⚠️ Budget Warning: 80% Limit Reached"
            }

            val contentText = if (is100Percent) {
                "You have reached 100% of your monthly limit: ${FormatUtils.formatNaira(monthlySpend)} spent of ${FormatUtils.formatNaira(budget)}."
            } else {
                "You have used ${percent.toInt()}% of your monthly limit: ${FormatUtils.formatNaira(monthlySpend)} of ${FormatUtils.formatNaira(budget)}."
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_budget_alert)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true) // Persistent notification in status bar
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }

            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Ignore if notification permission revoked
            }
        }

        fun cancelNotification(context: Context) {
            try {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            } catch (e: Exception) {
                // Ignore
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Budget Limit Alerts"
                val descriptionText = "Persistent status bar notifications when spending reaches 80% or 100% of monthly budget"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
