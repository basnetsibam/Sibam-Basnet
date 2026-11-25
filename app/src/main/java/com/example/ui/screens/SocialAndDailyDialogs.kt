package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DailyRewardCatalog
import com.example.data.RankTier
import com.example.data.local.FriendEntity
import com.example.ui.DailyRewardPopupState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyRewardDialog(
    state: DailyRewardPopupState,
    currentStreak: Int,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TacticalAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = TacticalAmber)
                }
                Column {
                    Text(
                        text = "DAILY LOGIN REWARDS",
                        style = MaterialTheme.typography.titleLarge,
                        color = TacticalAmber
                    )
                    Text(
                        text = "Login Streak: $currentStreak ${if (currentStreak == 1) "Day" else "Days"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.claimedReward?.let { reward ->
                    Surface(
                        color = NanoEmerald.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NanoEmerald, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CLAIMED ${reward.title.uppercase()}!", style = MaterialTheme.typography.titleMedium, color = NanoEmerald)
                            Text(
                                text = "+${reward.creditsReward} Gold • +${reward.gemsReward} Gems • +${reward.xpReward} XP added!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DailyRewardCatalog.cycle.forEach { dayItem ->
                        val isTarget = dayItem.day == state.nextDayNumber
                        val isClaimedAlready = (!state.canClaimToday && dayItem.day <= state.nextDayNumber) ||
                            (state.canClaimToday && dayItem.day < state.nextDayNumber)
                        Surface(
                            color = when {
                                isTarget && state.canClaimToday -> dayItem.accentColor.copy(alpha = 0.24f)
                                isClaimedAlready -> ObsidianElevated
                                else -> ObsidianSurface
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(126.dp)
                                .border(
                                    width = if (isTarget) 1.5.dp else 1.dp,
                                    color = if (isTarget) dayItem.accentColor else TacticalBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("DAY ${dayItem.day}", style = MaterialTheme.typography.labelSmall, color = dayItem.accentColor)
                                    if (isClaimedAlready) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Claimed", tint = NanoEmerald, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text("+${dayItem.creditsReward} Gold", style = MaterialTheme.typography.bodyMedium, color = TacticalAmber, fontWeight = FontWeight.Bold)
                                Text("+${dayItem.gemsReward} Gems • +${dayItem.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (state.canClaimToday) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                    modifier = Modifier.testTag("claim_daily_reward_button")
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Claim Day ${state.nextDayNumber}", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                    modifier = Modifier.testTag("close_daily_reward_button")
                ) {
                    Text("Awesome", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (state.canClaimToday) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = TextSecondary)
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FriendsSquadDialog(
    friends: List<FriendEntity>,
    onAddFriend: (String, String) -> Unit,
    onToggleSquad: (FriendEntity) -> Unit,
    onRemoveFriend: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var newCallsign by remember { mutableStateOf("") }
    var selectedRank by remember { mutableStateOf(RankTier.GOLD.label) }
    val rankOptions = listOf(RankTier.SILVER.label, RankTier.GOLD.label, RankTier.PLATINUM.label, RankTier.DIAMOND.label, RankTier.HEROIC.label)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SOCIAL FRIENDS & SQUAD", style = MaterialTheme.typography.titleLarge, color = ElectricCyan)
                    Text("Invited friends join your Infine Squad", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Friends", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newCallsign,
                    onValueChange = { newCallsign = it.take(16) },
                    label = { Text("Add Friend Callsign") },
                    placeholder = { Text("e.g. Kira_INF") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_callsign_input")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rankOptions.forEach { rank ->
                        FilterChip(
                            selected = selectedRank == rank,
                            onClick = { selectedRank = rank },
                            label = { Text(rank, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (newCallsign.isNotBlank()) {
                            onAddFriend(newCallsign, selectedRank)
                            newCallsign = ""
                        }
                    },
                    enabled = newCallsign.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Friend", fontWeight = FontWeight.Bold)
                }

                if (friends.isEmpty()) {
                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No friends added yet. Add a friend above and tap Invite to bring them into your Infine Squad!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(210.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(friends, key = { it.id }) { friend ->
                            Surface(
                                color = if (friend.isInSquad) ObsidianElevated else ObsidianSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (friend.isInSquad) NanoEmerald else TacticalBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (friend.isInSquad) NanoEmerald else ElectricCyan)
                                        )
                                        Column {
                                            Text(friend.callsign, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                            Text(
                                                text = if (friend.isInSquad) "${friend.rankLabel} • In Squad" else "${friend.rankLabel} • Online",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (friend.isInSquad) NanoEmerald else TextSecondary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { onToggleSquad(friend) },
                                            modifier = Modifier.testTag("invite_friend_${friend.callsign}")
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (friend.isInSquad) "Invited" else "Invite", style = MaterialTheme.typography.labelSmall)
                                        }
                                        IconButton(onClick = { onRemoveFriend(friend.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove friend", tint = CombatCrimson, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
