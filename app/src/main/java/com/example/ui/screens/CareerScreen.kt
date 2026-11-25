package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GlobalLeaderboardRow
import com.example.data.LeaderboardSortBy
import com.example.data.MatchReplayCodec
import com.example.data.PlayerLevelInfo
import com.example.data.RankTier
import com.example.data.local.IncidentReportEntity
import com.example.data.local.MatchHistoryEntity
import com.example.data.local.MatchReplayEntity
import com.example.data.local.PlayerProfileEntity
import com.example.ui.match.MatchReplayOverlay
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

@Composable
fun CareerScreen(
    profile: PlayerProfileEntity,
    history: List<MatchHistoryEntity>,
    incidentReports: List<IncidentReportEntity> = emptyList(),
    matchReplays: List<MatchReplayEntity> = emptyList(),
    leaderboardRows: List<GlobalLeaderboardRow>,
    leaderboardSortBy: LeaderboardSortBy,
    lastLeaderboardNotice: String?,
    globalWinsRank: Int,
    globalKdRank: Int,
    onSelectSortBy: (LeaderboardSortBy) -> Unit,
    onDismissLeaderboardNotice: () -> Unit,
    onUpdateCallsign: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showEditCallsign by remember { mutableStateOf(false) }
    var draftCallsign by remember(profile.callsign) { mutableStateOf(profile.callsign) }
    var activeReplay by remember { mutableStateOf<MatchReplayEntity?>(null) }

    val brTier = RankTier.fromRp(profile.brRp)
    val csTier = RankTier.fromRp(profile.csRp)

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ObsidianSurface,
            contentColor = TacticalAmber
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("My Career") },
                modifier = Modifier.testTag("ranked_tab_career")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Global Leaderboard") },
                modifier = Modifier.testTag("ranked_tab_leaderboard")
            )
        }

        if (selectedTab == 1) {
            GlobalLeaderboardContent(
                rows = leaderboardRows,
                sortBy = leaderboardSortBy,
                lastNotice = lastLeaderboardNotice,
                onSelectSortBy = onSelectSortBy,
                onDismissNotice = onDismissLeaderboardNotice
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .clickable { selectedTab = 1 }
                            .testTag("career_open_leaderboard_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = TacticalAmber)
                                Column {
                                    Text("GLOBAL LEADERBOARD", style = MaterialTheme.typography.titleMedium, color = TacticalAmber)
                                    Text(
                                        text = "Wins Rank #$globalWinsRank • K/D Rank #$globalKdRank",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Surface(color = ObsidianElevated, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "VIEW",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val levelInfo = PlayerLevelInfo.fromTotalXp(profile.totalXp)
                        Column {
                            Text("OPERATOR LEVEL ${levelInfo.level} • ${levelInfo.title.uppercase()}", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                            Text(profile.callsign, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        }
                        IconButton(
                            onClick = { showEditCallsign = true },
                            modifier = Modifier.testTag("edit_callsign_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Callsign", tint = ElectricCyan)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CareerStatBox("MATCHES", "${profile.totalMatches}", Modifier.weight(1f))
                        CareerStatBox("WINS", "${profile.totalWins}", Modifier.weight(1f))
                        CareerStatBox("KILLS", "${profile.totalKills}", Modifier.weight(1f))
                        CareerStatBox("REVIVES", "${profile.totalRevives}", Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text("RANKED TIERS (BR: ${brTier.label} • CS: ${csTier.label})", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RankTier.entries.forEach { tier ->
                    val reached = profile.brRp >= tier.minRp || profile.csRp >= tier.minRp
                    Surface(
                        color = if (reached) tier.badgeColor.copy(alpha = 0.18f) else ObsidianCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (reached) tier.badgeColor else TacticalBorder, RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.MilitaryTech,
                                contentDescription = tier.label,
                                tint = if (reached) tier.badgeColor else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tier.label.take(4),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reached) tier.badgeColor else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("RECENT INFINE MATCHES", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No Ranked Matches Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = "Drop into BR Ranked or Clash Squad on Infine to climb from Gold to Grandmaster!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(history) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LVL ${item.arenaLevel} • ${item.modeName} • ${item.placementText}", style = MaterialTheme.typography.titleMedium, color = TacticalAmber)
                            Text(
                                text = "${item.kills} Kills • ${item.revives} Revives • ${item.weaponUsed}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text("Sector: ${item.mapLocation} • +${item.xpEarned} XP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Surface(
                            color = ObsidianElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (item.rpDelta >= 0) "+${item.rpDelta} RP" else "${item.rpDelta} RP",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (item.rpDelta >= 0) NanoEmerald else CombatCrimson,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Replay, contentDescription = null, tint = TacticalAmber, modifier = Modifier.size(20.dp))
                Text("MATCH REPLAYS (${matchReplays.size})", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
        }

        if (matchReplays.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = "Finish a match in Infine to automatically save full 2D tactical replay telemetry here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(matchReplays, key = { it.id }) { replay ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .testTag("career_replay_card_${replay.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LVL ${replay.arenaLevel} • ${replay.modeName} • ${if (replay.won) "BOOYAH #1" else "#${replay.placementRank}"}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (replay.won) TacticalAmber else ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${replay.kills} Kills • ${replay.durationSeconds}s • ${replay.weaponUsed}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sector: ${replay.mapLocation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = { activeReplay = replay },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalAmber,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("career_watch_replay_${replay.id}")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Watch Replay", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = CombatCrimson, modifier = Modifier.size(20.dp))
                Text("ANTI-CHEAT INCIDENT LOG (DB)", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
        }

        if (incidentReports.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = "No player reports filed yet. Use the Flag button during a match or on the post-match summary screen to log suspicious behavior.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(incidentReports, key = { it.id }) { report ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CombatCrimson.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .testTag("career_incident_report_${report.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${report.reportedCallsign} • ${report.reasonLabel}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CombatCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${report.phaseLabel} • LVL ${report.arenaLevel} ${report.modeName} • ${report.mapLocation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                color = NanoEmerald.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(1.dp, NanoEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = report.statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NanoEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (report.details.isNotBlank()) {
                            Text(
                                text = "\"${report.details}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
            }
        }
    }

    if (showEditCallsign) {
        AlertDialog(
            onDismissRequest = { showEditCallsign = false },
            containerColor = ObsidianCard,
            title = { Text("Update Callsign", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = draftCallsign,
                    onValueChange = { draftCallsign = it.take(16) },
                    label = { Text("Callsign") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCallsign(draftCallsign)
                        showEditCallsign = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCallsign = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    activeReplay?.let { replay ->
        val decodedFrames = remember(replay.id, replay.framesJson) {
            MatchReplayCodec.decode(replay.framesJson)
        }
        MatchReplayOverlay(
            title = "${replay.modeName} • LVL ${replay.arenaLevel}",
            subtitle = "${if (replay.won) "BOOYAH #1" else "Rank #${replay.placementRank}"} • ${replay.kills} Kills • ${replay.mapLocation}",
            frames = decodedFrames,
            onClose = { activeReplay = null }
        )
    }
}

@Composable
private fun CareerStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = ElectricCyan)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
