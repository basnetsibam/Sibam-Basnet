package com.example.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import com.example.data.GameMode
import com.example.data.RoboHonorPolicy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WeaponCatalog
import com.example.ui.theme.CombatCrimson
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NanoEmerald
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PlasmaViolet
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MatchResultsSummaryOverlay(
    summary: MatchEndSummary,
    onReturnToLobby: () -> Unit,
    onViewLeaderboard: () -> Unit,
    onReportPlayer: () -> Unit = {},
    onWatchReplay: () -> Unit = {},
    onWatchDeathCam: () -> Unit = {}
) {
    val grade = computeCombatGrade(summary)
    val gradeColor = when (grade) {
        "S+" -> TacticalAmber
        "S" -> ElectricCyan
        "A" -> NanoEmerald
        else -> PlasmaViolet
    }
    val bannerAccent = if (summary.won) TacticalAmber else ElectricCyan
    val weaponName = WeaponCatalog.byId(summary.weaponUsed).name
    val durationFormatted = formatMatchDuration(summary.durationSeconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("match_results_summary_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Victory / Placement Banner + Combat Grade
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, bannerAccent, RoundedCornerShape(18.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    bannerAccent.copy(alpha = 0.22f),
                                    ObsidianCard,
                                    ObsidianElevated
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = bannerAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = if (summary.won) "BOOYAH! VICTORY" else "RANK #${summary.placementRank} FINISH",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = bannerAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${summary.mode.title} • Level ${summary.arenaLevel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Final Sector: ${summary.lastLocation} • Weapon: $weaponName",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (!summary.won && !summary.eliminatedByName.isNullOrBlank()) {
                                Text(
                                    text = "💀 Eliminated by ${summary.eliminatedByName} (${summary.eliminatedByWeapon})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CombatCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Combat Grade Medallion
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(gradeColor.copy(alpha = 0.18f))
                                .border(2.dp, gradeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GRADE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    text = grade,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = gradeColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 2x2 Combat Telemetry KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryKpiCard(
                    label = "ELIMINATIONS",
                    value = "${summary.kills}",
                    icon = Icons.Default.GpsFixed,
                    accent = CombatCrimson,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    label = "DAMAGE DEALT",
                    value = "${summary.damageDealt}",
                    icon = Icons.Default.Bolt,
                    accent = TacticalAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryKpiCard(
                    label = "REVIVES",
                    value = "${summary.revives}",
                    icon = Icons.Default.Favorite,
                    accent = NanoEmerald,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    label = "SURVIVAL TIME",
                    value = durationFormatted,
                    icon = Icons.Default.Timer,
                    accent = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            // Rewards & Progression Breakdown
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "REWARDS & PROGRESSION",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RewardChip(
                            label = "Gold",
                            value = "+${summary.goldEarned}",
                            icon = Icons.Default.Stars,
                            accent = TacticalAmber,
                            modifier = Modifier.weight(1f)
                        )
                        RewardChip(
                            label = "Gems",
                            value = "+${summary.gemsEarned}",
                            icon = Icons.Default.Diamond,
                            accent = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        RewardChip(
                            label = "Infinite XP",
                            value = "+${summary.xpEarned}",
                            icon = Icons.Default.Bolt,
                            accent = PlasmaViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (summary.mode.isRanked) {
                        val rpPositive = summary.rpDelta >= 0
                        Surface(
                            color = (if (rpPositive) NanoEmerald else CombatCrimson).copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (rpPositive) NanoEmerald.copy(alpha = 0.5f) else CombatCrimson.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ranked Points (RP) Update", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(
                                    text = "${if (rpPositive) "+${summary.rpDelta}" else "${summary.rpDelta}"} RP",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (rpPositive) NanoEmerald else CombatCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Robo-Sentinel Honor Verdict
                    val honorDelta = when {
                        summary.abandonedEarly && summary.mode.isRanked -> -RoboHonorPolicy.PENALTY_RANKED_QUIT
                        summary.abandonedEarly && summary.mode == GameMode.BATTLE_ROYALE_CASUAL -> -RoboHonorPolicy.PENALTY_CASUAL_QUIT
                        summary.mode == GameMode.BATTLE_ROYALE_CASUAL -> RoboHonorPolicy.GAIN_CASUAL_FINISH
                        summary.mode == GameMode.TRAINING_GROUND -> RoboHonorPolicy.GAIN_TRAINING_DRILL
                        else -> RoboHonorPolicy.GAIN_RANKED_CLEAN
                    }
                    val honorColor = when {
                        honorDelta < 0 -> CombatCrimson
                        summary.mode == GameMode.BATTLE_ROYALE_CASUAL -> ElectricCyan
                        else -> NanoEmerald
                    }
                    val honorTitle = when {
                        honorDelta < 0 -> "ROBO-SENTINEL PENALTY ($honorDelta HONOR)"
                        summary.mode == GameMode.BATTLE_ROYALE_CASUAL -> "ROBO-SENTINEL HONOR RESTORED (+$honorDelta)"
                        else -> "ROBO-SENTINEL FAIR PLAY (+$honorDelta HONOR)"
                    }
                    val honorSubtitle = when {
                        honorDelta < 0 -> "Early exit logged. Ranked locks below 90 Honor — finish Casual BR or Training Drill to recover."
                        summary.mode == GameMode.BATTLE_ROYALE_CASUAL -> "Completed Infine Casual! Your Honor Score increased toward Ranked clearance."
                        else -> "Match completed fairly. Keep Honor >= 90 to stay cleared for Ranked."
                    }
                    Surface(
                        color = honorColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, honorColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .testTag("match_result_robo_honor_verdict")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Robo-Sentinel Honor Verdict",
                                tint = honorColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = honorTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = honorColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = honorSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Squad Performance Breakdown
            if (summary.squadResults.isNotEmpty()) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = TacticalAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = "SQUAD PERFORMANCE",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        summary.squadResults.forEach { member ->
                            Surface(
                                color = if (member.isLocalPlayer) ObsidianElevated else ObsidianSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (member.isLocalPlayer) TacticalAmber.copy(alpha = 0.6f) else TacticalBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (member.isLocalPlayer) "${member.name} (YOU)" else member.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (member.isLocalPlayer) TacticalAmber else TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (member.isEliminated) "Eliminated" else "Combat Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (member.isEliminated) CombatCrimson else NanoEmerald
                                        )
                                    }
                                    Surface(
                                        color = ObsidianCard,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${member.kills} Kills",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ElectricCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            if (!summary.won && summary.deathCamFrames.isNotEmpty()) {
                Button(
                    onClick = onWatchDeathCam,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CombatCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("match_result_death_cam_button")
                ) {
                    Text(
                        text = "🎥 Watch Slow-Mo Death Cam (${summary.eliminatedByName ?: "Killer"} POV)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onWatchReplay,
                    enabled = summary.replayFrames.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("match_result_replay_button")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Watch Replay", tint = TacticalAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Replay", color = TacticalAmber, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReportPlayer,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("match_result_report_button")
                ) {
                    Icon(Icons.Default.Flag, contentDescription = "Report Player", tint = CombatCrimson, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Report", color = CombatCrimson, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onViewLeaderboard,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("match_result_leaderboard_button")
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Ranks", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onReturnToLobby,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalAmber,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("match_result_continue_button")
            ) {
                Text("Return to Lobby", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = ObsidianCard),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RewardChip(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(18.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

private fun computeCombatGrade(summary: MatchEndSummary): String {
    val score = (if (summary.won) 45 else 15) +
        summary.kills * 12 +
        summary.revives * 8 +
        (summary.damageDealt / 60)
    return when {
        score >= 95 -> "S+"
        score >= 70 -> "S"
        score >= 45 -> "A"
        else -> "B"
    }
}

private fun formatMatchDuration(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
}
