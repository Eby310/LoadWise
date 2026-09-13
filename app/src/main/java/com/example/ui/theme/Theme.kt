package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Light theme color scheme exclusively as instructed
private val LoadwiseColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = PureWhite,
  primaryContainer = NavyLightSurface,
  onPrimaryContainer = NavyPrimary,
  secondary = NavySecondary,
  onSecondary = PureWhite,
  secondaryContainer = NavyContainer,
  onSecondaryContainer = NavyPrimary,
  tertiary = NavyTertiary,
  onTertiary = PureWhite,
  background = PureWhite,
  onBackground = TextPrimary,
  surface = PureWhite,
  onSurface = TextPrimary,
  surfaceVariant = LightGreyFill,
  onSurfaceVariant = TextSecondary,
  outline = BorderGrey,
  outlineVariant = LightGreyFill,
  error = AlertRed,
  onError = PureWhite,
  errorContainer = AlertRedContainer,
  onErrorContainer = AlertRed
)

val LoadwiseShapes = Shapes(
  small = RoundedCornerShape(12.dp),   // Buttons & chips (12dp)
  medium = RoundedCornerShape(16.dp),  // Cards (16dp)
  large = RoundedCornerShape(24.dp),   // Bottom sheet top / dialogs (24dp)
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Strictly Light Theme only
  dynamicColor: Boolean = false, // Keep intentional Navy styling across all devices
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LoadwiseColorScheme,
    typography = Typography,
    shapes = LoadwiseShapes,
    content = content
  )
}

