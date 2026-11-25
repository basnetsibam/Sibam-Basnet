package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.GlobalLeaderboardRow
import com.example.data.LeaderboardSortBy
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

@Composable
fun LeaderboardScreen(
    rows: List<GlobalLeaderboardRow>,
    sortBy: LeaderboardSortBy,
    lastNotice: String?,
    onSelectSortBy: (LeaderboardSortBy) -> Unit,
    onDismissNotice: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = ObsidianSurface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("leaderboard_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Icon(Icons.Default.Public, contentDescription = null, tint = TacticalAmber)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GLOBAL LEADERBOARD",
                        style = MaterialTheme.typography.titleLarge,
                        color = TacticalAmber
                    )
                    Text(
                        text = "Updates dynamically after every competitive match",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        GlobalLeaderboardContent(
            rows = rows,
            sortBy = sortBy,
            lastNotice = lastNotice,
            onSelectSortBy = onSelectSortBy,
            onDismissNotice = onDismissNotice
        )
    }
}

@Composable
fun GlobalLeaderboardContent(
    rows: List<GlobalLeaderboardRow>,
    sortBy: LeaderboardSortBy,
    lastNotice: String?,
    onSelectSortBy: (LeaderboardSortBy) -> Unit,
    onDismissNotice: () -> Unit
) {
    val myRow = rows.firstOrNull { it.isLocalPlayer }
    val podium = rows.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lastNotice != null) {
            item {
                Surface(
                    color = NanoEmerald.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NanoEmerald, RoundedCornerShape(12.dp))
                        .testTag("leaderboard_update_notice")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = lastNotice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NanoEmerald,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissNotice) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss update notice", tint = NanoEmerald)
                        }
                    }
                }
            }
        }

        // My Global Standing Summary Card
        if (myRow != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(listOf(TacticalAmber, ElectricCyan)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("leaderboard_my_standing")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TacticalAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${myRow.rank}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TacticalAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("YOUR GLOBAL STANDING", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                                    Text(
                                        text = "${myRow.callsign} • ${myRow.rankTier.label}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Surface(
                                color = ObsidianElevated,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${myRow.rp} RP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = myRow.rankTier.badgeColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StandingStatPill(
                                label = "TOTAL WINS",
                                value = "${myRow.totalWins}",
                                accent = TacticalAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StandingStatPill(
                                label = "K/D RATIO",
                                value = String.format(Locale.US, "%.2f", myRow.kdRatio),
                                accent = ElectricCyan,
                                modifier = Modifier.weight(1f)
                            )
                            StandingStatPill(
                                label = "WIN RATE",
                                value = "${myRow.winRatePercent}%",
                                accent = NanoEmerald,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Rank By Selector (Total Wins vs K/D Ratio)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("RANK PLAYERS BY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeaderboardSortBy.entries.forEach { mode ->
                        val selected = sortBy == mode
                        val accent = if (mode == LeaderboardSortBy.TOTAL_WINS) TacticalAmber else ElectricCyan
                        val icon = if (mode == LeaderboardSortBy.TOTAL_WINS) Icons.Default.EmojiEvents else Icons.Default.GpsFixed
                        Surface(
                            color = if (selected) accent.copy(alpha = 0.2f) else ObsidianCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) accent else TacticalBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectSortBy(mode) }
                                .testTag("leaderboard_sort_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = mode.label,
                                    tint = if (selected) accent else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) accent else TextPrimary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top 3 Podium
        if (podium.size >= 3) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    PodiumCard(
                        row = podium[1],
                        sortBy = sortBy,
                        medalColor = ElectricCyan,
                        heightDp = 112,
                        modifier = Modifier.weight(1f)
                    )
                    PodiumCard(
                        row = podium[0],
                        sortBy = sortBy,
                        medalColor = TacticalAmber,
                        heightDp = 128,
                        modifier = Modifier.weight(1.1f)
                    )
                    PodiumCard(
                        row = podium[2],
                        sortBy = sortBy,
                        medalColor = Color(0xFFCD7F32),
                        heightDp = 104,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Text("GLOBAL STANDINGS (${rows.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Text(
                    text = if (sortBy == LeaderboardSortBy.TOTAL_WINS) "Sorted by Wins" else "Sorted by K/D",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        items(rows, key = { it.callsign }) { row ->
            LeaderboardPlayerRow(row = row, sortBy = sortBy)
        }
    }
}

@Composable
private fun StandingStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun PodiumCard(
    row: GlobalLeaderboardRow,
    sortBy: LeaderboardSortBy,
    medalColor: Color,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    val metricValue = if (sortBy == LeaderboardSortBy.TOTAL_WINS) {
        "${row.totalWins} Wins"
    } else {
        "${String.format(Locale.US, "%.2f", row.kdRatio)} K/D"
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (row.isLocalPlayer) ObsidianElevated else ObsidianCard
        ),
        modifier = modifier
            .height(heightDp.dp)
            .border(
                width = if (row.rank == 1 || row.isLocalPlayer) 1.5.dp else 1.dp,
                color = if (row.isLocalPlayer) ElectricCyan else medalColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.MilitaryTech, contentDescription = "Rank ${row.rank}", tint = medalColor, modifier = Modifier.size(16.dp))
                Text("#${row.rank}", style = MaterialTheme.typography.titleMedium, color = medalColor, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = row.callsign,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (row.isLocalPlayer) ElectricCyan else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (row.isLocalPlayer) "YOU" else row.regionTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Surface(
                color = medalColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = metricValue,
                    style = MaterialTheme.typography.labelMedium,
                    color = medalColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun LeaderboardPlayerRow(
    row: GlobalLeaderboardRow,
    sortBy: LeaderboardSortBy
) {
    val rankAccent = when (row.rank) {
        1 -> TacticalAmber
        2 -> ElectricCyan
        3 -> Color(0xFFCD7F32)
        else -> TextSecondary
    }
    val primaryMetric = if (sortBy == LeaderboardSortBy.TOTAL_WINS) {
        "${row.totalWins} WINS"
    } else {
        "${String.format(Locale.US, "%.2f", row.kdRatio)} K/D"
    }
    val secondaryMetric = if (sortBy == LeaderboardSortBy.TOTAL_WINS) {
        "${String.format(Locale.US, "%.2f", row.kdRatio)} K/D • ${row.totalKills} K"
    } else {
        "${row.totalWins} Wins • ${row.totalMatches} M"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (row.isLocalPlayer) ObsidianElevated else ObsidianCard
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (row.isLocalPlayer) 1.5.dp else 1.dp,
                color = if (row.isLocalPlayer) ElectricCyan else TacticalBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("leaderboard_row_${row.callsign}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (row.rank <= 3) rankAccent.copy(alpha = 0.2f) else ObsidianSurface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${row.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (row.rank <= 3) rankAccent else TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = row.callsign,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (row.isLocalPlayer) ElectricCyan else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (row.isLocalPlayer) {
                        Surface(color = ElectricCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    } else if (row.isFriend) {
                        Surface(color = NanoEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "FRIEND",
                                style = MaterialTheme.typography.labelSmall,
                                color = NanoEmerald,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (row.lastDeltaNote.isNotBlank()) {
                        Surface(color = NanoEmerald.copy(alpha = 0.16f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = row.lastDeltaNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = NanoEmerald,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${row.regionTag} • ${row.rankTier.label} (${row.rp} RP)",
                    style = MaterialTheme.typography.labelSmall,
                    color = row.rankTier.badgeColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = primaryMetric,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (sortBy == LeaderboardSortBy.TOTAL_WINS) TacticalAmber else ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = secondaryMetric,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
