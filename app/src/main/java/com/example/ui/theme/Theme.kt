package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val InfineDarkColorScheme = darkColorScheme(
    primary = TacticalAmber,
    onPrimary = ObsidianVoid,
    primaryContainer = TacticalAmberSoft,
    onPrimaryContainer = TacticalAmber,
    secondary = ElectricCyan,
    onSecondary = ObsidianVoid,
    secondaryContainer = ElectricCyanSoft,
    onSecondaryContainer = ElectricCyan,
    tertiary = NanoEmerald,
    onTertiary = ObsidianVoid,
    tertiaryContainer = NanoEmeraldSoft,
    onTertiaryContainer = NanoEmerald,
    error = CombatCrimson,
    onError = TextPrimary,
    errorContainer = CombatCrimsonSoft,
    onErrorContainer = CombatCrimson,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = TacticalBorder
)

private val InfineShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun InfineArenaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InfineDarkColorScheme,
        typography = InfineTypography,
        shapes = InfineShapes,
        content = content
    )
}
