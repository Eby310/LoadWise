package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.service.BudgetMonitorService
import com.example.ui.LoadwiseApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
          if (isGranted) {
            BudgetMonitorService.checkBudget(context)
          }
        }

        LaunchedEffect(Unit) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
              context,
              Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          }
          BudgetMonitorService.checkBudget(context)
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = PureWhite
        ) {
          LoadwiseApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    BudgetMonitorService.checkBudget(applicationContext)
  }
}

