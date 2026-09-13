package com.example

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Recharge
import com.example.data.model.RechargeType
import com.example.data.model.UssdDirectory
import com.example.data.repository.SettingsRepository
import com.example.service.BudgetMonitorService
import com.example.util.UssdHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var settingsRepository: SettingsRepository
  private lateinit var notificationManager: NotificationManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    val app = context as android.app.Application
    org.robolectric.Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

    database = AppDatabase.getInstance(context)
    settingsRepository = SettingsRepository(context)
    notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    runBlocking {
      database.rechargeDao().clearAll()
      settingsRepository.clearAllSettings()
      BudgetMonitorService.cancelNotification(context)
    }
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Loadwise", appName)
  }

  @Test
  fun `budget check below 80 percent displays no notification`() = runBlocking {
    settingsRepository.setMonthlyBudget(10000.0)
    // Add recharge of 5000 (50% of budget)
    database.rechargeDao().insertRecharge(
      Recharge(
        type = RechargeType.AIRTIME,
        network = "MTN",
        amount = 5000.0,
        date = System.currentTimeMillis()
      )
    )

    BudgetMonitorService.checkBudgetAndNotify(context)

    val notification = notificationManager.activeNotifications.find {
      it.id == BudgetMonitorService.NOTIFICATION_ID
    }
    assertNull(notification)
  }

  @Test
  fun `budget check at 80 percent displays persistent notification`() = runBlocking {
    settingsRepository.setMonthlyBudget(10000.0)
    // Add recharge of 8000 (80% of budget)
    database.rechargeDao().insertRecharge(
      Recharge(
        type = RechargeType.DATA,
        network = "Airtel",
        amount = 8000.0,
        dataSizeMb = 5000,
        date = System.currentTimeMillis()
      )
    )

    BudgetMonitorService.checkBudgetAndNotify(context)

    val activeNotification = notificationManager.activeNotifications.find {
      it.id == BudgetMonitorService.NOTIFICATION_ID
    }
    assertNotNull("Notification should be posted at 80% limit", activeNotification)
    val notification = activeNotification!!.notification
    val isPersistent = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
    assertTrue("Notification must be persistent (ongoing)", isPersistent)
    val title = notification.extras.getString(Notification.EXTRA_TITLE)
    assertTrue("Title should indicate 80% warning", title?.contains("80%") == true)
  }

  @Test
  fun `budget check at 100 percent displays persistent notification`() = runBlocking {
    settingsRepository.setMonthlyBudget(10000.0)
    // Add recharge of 12000 (120% of budget)
    database.rechargeDao().insertRecharge(
      Recharge(
        type = RechargeType.AIRTIME,
        network = "Glo",
        amount = 12000.0,
        date = System.currentTimeMillis()
      )
    )

    BudgetMonitorService.checkBudgetAndNotify(context)

    val activeNotification = notificationManager.activeNotifications.find {
      it.id == BudgetMonitorService.NOTIFICATION_ID
    }
    assertNotNull("Notification should be posted at 100% limit", activeNotification)
    val notification = activeNotification!!.notification
    val isPersistent = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
    assertTrue("Notification must be persistent (ongoing)", isPersistent)
    val title = notification.extras.getString(Notification.EXTRA_TITLE)
    assertTrue("Title should indicate 100% limit reached", title?.contains("100%") == true)
  }

  @Test
  fun `cancelling or increasing budget clears persistent notification`() = runBlocking {
    settingsRepository.setMonthlyBudget(5000.0)
    database.rechargeDao().insertRecharge(
      Recharge(
        type = RechargeType.AIRTIME,
        network = "MTN",
        amount = 5000.0,
        date = System.currentTimeMillis()
      )
    )

    BudgetMonitorService.checkBudgetAndNotify(context)
    assertNotNull(notificationManager.activeNotifications.find { it.id == BudgetMonitorService.NOTIFICATION_ID })

    // Now increase budget to 20,000 (spending is now 25%)
    settingsRepository.setMonthlyBudget(20000.0)
    BudgetMonitorService.checkBudgetAndNotify(context)

    val cleared = notificationManager.activeNotifications.find { it.id == BudgetMonitorService.NOTIFICATION_ID }
    assertNull("Notification should be cleared when below 80%", cleared)
  }

  @Test
  fun `universal ussd codes contain standard ncc codes`() {
    val primaryCodes = UssdDirectory.PRIMARY_CODES.map { it.code }
    assertTrue("Should include *310# for airtime balance", primaryCodes.contains("*310#"))
    assertTrue("Should include *312# for buy data/plans menu", primaryCodes.contains("*312#"))
    assertTrue("Should include *323# for check data balance", primaryCodes.contains("*323#"))
  }

  @Test
  fun `copy ussd code to clipboard`() {
    UssdHelper.copyToClipboard(context, "*310#", "Airtime Balance")
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = clipboard.primaryClip
    assertNotNull(clip)
    assertEquals("*310#", clip?.getItemAt(0)?.text?.toString())
  }
}
