package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerLevelInfo
import com.example.data.RankTier
import com.example.data.WeaponCatalog
import com.example.data.local.MatchHistoryEntity
import com.example.data.local.PlayerProfileEntity
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

data class WeaponUsageStat(
    val weaponName: String,
    val matchesUsed: Int,
    val totalKills: Int,
    val totalDamage: Int,
    val accentColor: Color
)

@Composable
fun DashboardScreen(
    profile: PlayerProfileEntity,
    history: List<MatchHistoryEntity>,
    globalWinsRank: Int,
    globalKdRank: Int,
    onGoToLobby: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val rankedHistory = remember(history) {
        history.filter { it.modeName != "Training" }
    }

    val deaths = remember(profile, rankedHistory) {
        if (rankedHistory.isNotEmpty()) {
            rankedHistory.count {
                !it.placementText.contains("BOOYAH", ignoreCase = true) &&
                    !it.placementText.contains("VICTORY", ignoreCase = true)
            }
        } else {
            (profile.totalMatches - profile.totalWins).coerceAtLeast(0)
        }
    }

    val kdRatio = remember(profile.totalKills, deaths) {
        if (deaths == 0) profile.totalKills.toFloat() else profile.totalKills.toFloat() / deaths.toFloat()
    }

    val winRate = remember(profile.totalWins, profile.totalMatches) {
        if (profile.totalMatches == 0) 0f else (profile.totalWins.toFloat() / profile.totalMatches.toFloat())
    }

    val weaponStats = remember(history) {
        val grouped = history.groupBy { it.weaponUsed }
        grouped.map { (name, matches) ->
            val spec = WeaponCatalog.all.firstOrNull { it.name.equals(name, ignoreCase = true) }
            WeaponUsageStat(
                weaponName = name,
                matchesUsed = matches.size,
                totalKills = matches.sumOf { it.kills },
                totalDamage = matches.sumOf { it.damageDealt },
                accentColor = spec?.accentColor ?: ElectricCyan
            )
        }.sortedWith(compareByDescending<WeaponUsageStat> { it.matchesUsed }.thenByDescending { it.totalKills })
    }

    var selectedWeaponStat by remember(weaponStats) {
        mutableStateOf(weaponStats.firstOrNull())
    }

    val chartAnim = remember { Animatable(0f) }
    LaunchedEffect(history.size, profile.totalKills, profile.totalWins) {
        chartAnim.snapTo(0f)
        chartAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("COMBAT DASHBOARD", style = MaterialTheme.typography.headlineMedium, color = TacticalAmber)
                Text(
                    text = "${profile.callsign} • ${RankTier.fromRp(profile.brRp).label} BR / ${RankTier.fromRp(profile.csRp).label} CS",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(1.dp, TacticalAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenLeaderboard)
                    .testTag("dashboard_leaderboard_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = "Open Global Leaderboard", tint = TacticalAmber, modifier = Modifier.size(16.dp))
                    Text("TOP #$globalWinsRank / #$globalKdRank", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                }
            }
        }

        val levelInfo = PlayerLevelInfo.fromTotalXp(profile.totalXp)
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NanoEmerald.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .testTag("dashboard_level_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PLAYER LEVEL ${levelInfo.level} • ${levelInfo.title}", style = MaterialTheme.typography.titleMedium, color = NanoEmerald)
                    Text("${levelInfo.currentXpInLevel}/${levelInfo.xpRequiredForNext} XP", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
                LinearProgressIndicator(
                    progress = { levelInfo.progress },
                    color = NanoEmerald,
                    trackColor = ObsidianElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // Top KPI Row: K/D Ratio, Total Wins, Win Rate, Total Kills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardKpiCard(
                label = "K/D RATIO",
                value = String.format(Locale.US, "%.2f", kdRatio),
                subtext = "${profile.totalKills} K / $deaths D",
                accent = ElectricCyan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_kd_ratio")
            )
            DashboardKpiCard(
                label = "TOTAL WINS",
                value = "${profile.totalWins}",
                subtext = "${(winRate * 100).toInt()}% Win Rate",
                accent = TacticalAmber,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_total_wins")
            )
        }

        // Chart 1: Dual Concentric Arc Gauge (Win Rate & Combat Efficiency / K/D)
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
                .testTag("chart_kd_wins_gauge")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TacticalAmber)
                    Text("K/D & WIN EFFICIENCY", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Concentric Ring Chart
                    Box(
                        modifier = Modifier.size(144.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val kdNormalized = (kdRatio / 4.0f).coerceIn(0f, 1f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeOuter = 16.dp.toPx()
                            val strokeInner = 12.dp.toPx()
                            val progress = chartAnim.value

                            // Outer track + Win Rate arc
                            drawArc(
                                color = ObsidianElevated,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = TacticalAmber,
                                startAngle = -90f,
                                sweepAngle = 360f * winRate * progress,
                                useCenter = false,
                                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                            )

                            // Inner track + K/D Efficiency arc (scaled up to 4.0 K/D)
                            val inset = strokeOuter + 8.dp.toPx()
                            drawArc(
                                color = ObsidianElevated,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - inset * 2, size.height - inset * 2),
                                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = ElectricCyan,
                                startAngle = -90f,
                                sweepAngle = 360f * kdNormalized * progress,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - inset * 2, size.height - inset * 2),
                                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.2f", kdRatio),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ElectricCyan
                            )
                            Text("K/D", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }

                    // Legend & Breakdown
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GaugeLegendRow(
                            color = TacticalAmber,
                            title = "Total Wins",
                            value = "${profile.totalWins} / ${profile.totalMatches} Matches"
                        )
                        GaugeLegendRow(
                            color = ElectricCyan,
                            title = "K/D Efficiency",
                            value = "${profile.totalKills} Eliminations"
                        )
                        GaugeLegendRow(
                            color = NanoEmerald,
                            title = "Revives",
                            value = "${profile.totalRevives} Squad Saves"
                        )
                    }
                }
            }
        }

        // Chart 2: Most Used Weapons Horizontal Bar Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
                .testTag("chart_most_used_weapons")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = CombatCrimson)
                    Text("MOST USED WEAPONS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                }

                if (weaponStats.isEmpty()) {
                    EmptyChartPrompt(
                        message = "No weapon telemetry recorded yet. Complete a match in BR Ranked or Clash Squad to see your weapon bar chart.",
                        onGoToLobby = onGoToLobby
                    )
                } else {
                    val maxMatches = weaponStats.maxOf { it.matchesUsed }.coerceAtLeast(1)
                    weaponStats.take(6).forEach { stat ->
                        val isSelected = selectedWeaponStat?.weaponName == stat.weaponName
                        val ratio = (stat.matchesUsed.toFloat() / maxMatches.toFloat()) * chartAnim.value
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ObsidianElevated else ObsidianSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) stat.accentColor else TacticalBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedWeaponStat = stat }
                                .padding(10.dp)
                                .testTag("weapon_bar_${stat.weaponName}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stat.weaponName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    text = "${stat.matchesUsed} ${if (stat.matchesUsed == 1) "match" else "matches"} • ${stat.totalKills} kills",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = stat.accentColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            ) {
                                drawRoundRect(
                                    color = ObsidianVoid,
                                    size = size,
                                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                                )
                                drawRoundRect(
                                    color = stat.accentColor,
                                    size = Size((size.width * ratio).coerceAtLeast(6.dp.toPx()), size.height),
                                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                                )
                            }
                        }
                    }

                    selectedWeaponStat?.let { sel ->
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Selected: ${sel.weaponName}", style = MaterialTheme.typography.labelLarge, color = sel.accentColor)
                                Text("Total Damage: ${sel.totalDamage}", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Chart 3: Recent Match Kills & Performance Area Line Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
                .testTag("chart_recent_kills_trend")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NanoEmerald)
                    Text("MATCH ELIMINATIONS TREND", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                }

                val trendMatches = remember(history) { history.take(10).reversed() }
                if (trendMatches.size < 2) {
                    Text(
                        text = if (trendMatches.isEmpty()) {
                            "Play matches to plot your elimination trend across Infine."
                        } else {
                            "Latest match: ${trendMatches.first().kills} kills with ${trendMatches.first().weaponUsed}. Play 1 more match to plot trend line."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    val maxKills = trendMatches.maxOf { it.kills }.coerceAtLeast(4)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val padX = 16.dp.toPx()
                        val padY = 16.dp.toPx()
                        val chartW = (size.width - padX * 2).coerceAtLeast(1f)
                        val chartH = (size.height - padY * 2).coerceAtLeast(1f)
                        val stepX = chartW / (trendMatches.size - 1).coerceAtLeast(1)

                        // Horizontal reference grid lines
                        for (g in 0..2) {
                            val gy = padY + chartH * (g / 2f)
                            drawLine(
                                color = TacticalBorder.copy(alpha = 0.5f),
                                start = Offset(padX, gy),
                                end = Offset(size.width - padX, gy),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val points = trendMatches.mapIndexed { idx, m ->
                            val px = padX + idx * stepX
                            val ratio = (m.kills.toFloat() / maxKills.toFloat()) * chartAnim.value
                            val py = padY + chartH * (1f - ratio)
                            Offset(px, py)
                        }

                        val fillPath = Path().apply {
                            moveTo(points.first().x, padY + chartH)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, padY + chartH)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                listOf(ElectricCyan.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = ElectricCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        points.forEach { pt ->
                            drawCircle(color = ObsidianVoid, radius = 6.dp.toPx(), center = pt)
                            drawCircle(color = TacticalAmber, radius = 4.dp.toPx(), center = pt)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Oldest (${trendMatches.first().kills} K)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("Peak: $maxKills Kills", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                        Text("Latest (${trendMatches.last().kills} K)", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardKpiCard(
    label: String,
    value: String,
    subtext: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.displaySmall, color = accent)
            Text(subtext, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
        }
    }
}

@Composable
private fun GaugeLegendRow(color: Color, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyChartPrompt(
    message: String,
    onGoToLobby: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Button(
            onClick = onGoToLobby,
            colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
            modifier = Modifier.testTag("dashboard_play_now_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Deploy to Infine", fontWeight = FontWeight.Bold)
        }
    }
}
