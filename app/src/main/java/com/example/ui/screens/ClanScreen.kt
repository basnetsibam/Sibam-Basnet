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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClanBannerCatalog
import com.example.data.ClanBannerSpec
import com.example.data.ClanLevelInfo
import com.example.data.ClanMissionCatalog
import com.example.data.local.ClanEntity
import com.example.data.local.ClanMissionProgressEntity
import com.example.data.local.FriendEntity
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
import kotlinx.coroutines.delay

@Composable
fun ClanScreen(
    profile: PlayerProfileEntity,
    clans: List<ClanEntity>,
    clanMissions: List<ClanMissionProgressEntity>,
    friends: List<FriendEntity>,
    statusMessage: String?,
    onCreateClan: (name: String, tag: String, motto: String) -> Unit,
    onJoinClan: (Int) -> Unit,
    onLeaveClan: () -> Unit,
    onRecruitMember: (String) -> Unit,
    onEquipBanner: (String) -> Unit,
    onClaimMission: (String) -> Unit,
    onRunSquadDrill: () -> Unit,
    onClearStatusMessage: () -> Unit,
    onBack: () -> Unit
) {
    val joinedClan = remember(clans) { clans.firstOrNull { it.isJoined } }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRecruitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3600L)
            onClearStatusMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Bar
        Surface(
            color = ObsidianSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("clan_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Column {
                        Text("CLAN & SQUADS HUB", style = MaterialTheme.typography.titleLarge, color = TacticalAmber, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (joinedClan != null) "[${joinedClan.tag}] ${joinedClan.name}" else "Join or create a squad to unlock Clan Banners",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("clan_open_create_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (statusMessage != null) {
            Surface(
                color = NanoEmerald.copy(alpha = 0.16f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, NanoEmerald, RoundedCornerShape(10.dp))
                    .clickable { onClearStatusMessage() }
                    .testTag("clan_status_banner")
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = NanoEmerald,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        if (joinedClan == null) {
            // No Clan Joined: Browse Preset/Custom Clans or Create One
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ElevatedCard(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("FORM YOUR INFINE CLAN", style = MaterialTheme.typography.titleLarge, color = ElectricCyan, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Create your own squad or join an existing Infine Clan to complete Clan-Only Missions, level up your Shared Clan XP bar, and unlock Cosmetic Clan Banners.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("clan_hero_create_button")
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Custom Clan Squad", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("AVAILABLE CLANS ON INFINE", style = MaterialTheme.typography.titleMedium, color = TacticalAmber)
                }

                items(clans, key = { it.id }) { clan ->
                    ClanDirectoryCard(
                        clan = clan,
                        isCurrent = false,
                        onJoin = { onJoinClan(clan.id) }
                    )
                }
            }
        } else {
            val clanLevel = ClanLevelInfo.fromClanXp(joinedClan.clanXp)
            val equippedBanner = ClanBannerCatalog.byId(joinedClan.equippedBannerId)
            val members = joinedClan.membersCsv.split(",").filter { it.isNotBlank() }

            // Joined Clan Banner + Shared XP Header Card
            Column(modifier = Modifier.fillMaxSize()) {
                ClanBannerHeroCard(
                    clan = joinedClan,
                    levelInfo = clanLevel,
                    banner = equippedBanner,
                    members = members,
                    onRecruit = { showRecruitDialog = true },
                    onDrill = onRunSquadDrill,
                    onLeave = onLeaveClan,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ObsidianSurface,
                    contentColor = TacticalAmber
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Missions") },
                        modifier = Modifier.testTag("clan_tab_missions")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Banners") },
                        modifier = Modifier.testTag("clan_tab_banners")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Directory") },
                        modifier = Modifier.testTag("clan_tab_directory")
                    )
                }

                when (selectedTab) {
                    0 -> {
                        val progressById = clanMissions.associateBy { it.missionId }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "CLAN-ONLY MISSIONS (PLAY MATCHES OR RUN SQUAD DRILLS)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan
                                )
                            }
                            items(ClanMissionCatalog.all, key = { it.id }) { spec ->
                                val progress = progressById[spec.id]
                                val currentCount = (progress?.progressCount ?: 0).coerceAtMost(spec.targetCount)
                                val isClaimed = progress?.isClaimed == true
                                val canClaim = !isClaimed && currentCount >= spec.targetCount

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (canClaim) NanoEmerald else TacticalBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .testTag("clan_mission_${spec.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Surface(
                                                        color = TacticalAmber.copy(alpha = 0.18f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = spec.badgeLabel,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = TacticalAmber,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = spec.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(spec.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            }
                                            Text(
                                                text = "$currentCount/${spec.targetCount}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (currentCount >= spec.targetCount) NanoEmerald else ElectricCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        LinearProgressIndicator(
                                            progress = { (currentCount.toFloat() / spec.targetCount.toFloat()).coerceIn(0f, 1f) },
                                            color = if (currentCount >= spec.targetCount) NanoEmerald else ElectricCyan,
                                            trackColor = ObsidianElevated,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "+${spec.clanXpReward} Clan XP • +${spec.goldReward} Gold",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TacticalAmber
                                            )
                                            Button(
                                                onClick = { onClaimMission(spec.id) },
                                                enabled = canClaim,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isClaimed) ObsidianElevated else NanoEmerald,
                                                    contentColor = if (isClaimed) TextSecondary else ObsidianVoid
                                                ),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("clan_claim_${spec.id}")
                                            ) {
                                                Text(
                                                    text = when {
                                                        isClaimed -> "Claimed"
                                                        canClaim -> "Claim Reward"
                                                        else -> "In Progress"
                                                    },
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "COSMETIC CLAN BANNERS (UNLOCK VIA SHARED CLAN XP)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TacticalAmber
                                )
                            }
                            items(ClanBannerCatalog.all, key = { it.id }) { banner ->
                                val unlocked = clanLevel.level >= banner.unlockLevel
                                val equipped = joinedClan.equippedBannerId == banner.id
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (equipped) 2.dp else 1.dp,
                                            color = if (equipped) banner.primaryColor else TacticalBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .testTag("clan_banner_${banner.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        banner.primaryColor.copy(alpha = if (unlocked) 0.22f else 0.08f),
                                                        ObsidianCard
                                                    )
                                                )
                                            )
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(banner.primaryColor.copy(alpha = 0.2f))
                                                .border(1.5.dp, banner.primaryColor, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = banner.emblemTag,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = banner.primaryColor,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = banner.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (unlocked) TextPrimary else TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(banner.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Button(
                                            onClick = { onEquipBanner(banner.id) },
                                            enabled = unlocked && !equipped,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (equipped) NanoEmerald else banner.primaryColor,
                                                contentColor = ObsidianVoid
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("clan_equip_banner_${banner.id}")
                                        ) {
                                            if (!unlocked) {
                                                Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Lv.${banner.unlockLevel}")
                                            } else if (equipped) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Equipped", modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Equipped")
                                            } else {
                                                Text("Equip", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("ALL CLANS ON INFINE", style = MaterialTheme.typography.labelLarge, color = ElectricCyan)
                            }
                            items(clans, key = { it.id }) { clan ->
                                ClanDirectoryCard(
                                    clan = clan,
                                    isCurrent = clan.id == joinedClan.id,
                                    onJoin = { onJoinClan(clan.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateClanDialog(
            onConfirm = { name, tag, motto ->
                onCreateClan(name, tag, motto)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showRecruitDialog && joinedClan != null) {
        RecruitClanMemberDialog(
            clan = joinedClan,
            friends = friends,
            localCallsign = profile.callsign,
            onRecruit = { callsign ->
                onRecruitMember(callsign)
                showRecruitDialog = false
            },
            onDismiss = { showRecruitDialog = false }
        )
    }
}

@Composable
private fun ClanBannerHeroCard(
    clan: ClanEntity,
    levelInfo: ClanLevelInfo,
    banner: ClanBannerSpec,
    members: List<String>,
    onRecruit: () -> Unit,
    onDrill: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, banner.primaryColor, RoundedCornerShape(16.dp))
            .testTag("clan_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            banner.primaryColor.copy(alpha = 0.24f),
                            ObsidianCard,
                            banner.accentColor.copy(alpha = 0.14f)
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(banner.primaryColor.copy(alpha = 0.22f))
                            .border(1.5.dp, banner.primaryColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = banner.name, tint = banner.primaryColor, modifier = Modifier.size(20.dp))
                            Text(banner.emblemTag, style = MaterialTheme.typography.labelSmall, color = banner.primaryColor, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Column {
                        Text(
                            text = "[${clan.tag}] ${clan.name}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${banner.name} • ${clan.memberCount}/8 Squadmates",
                            style = MaterialTheme.typography.labelSmall,
                            color = banner.primaryColor
                        )
                    }
                }
                TextButton(
                    onClick = onLeave,
                    modifier = Modifier.testTag("clan_leave_button")
                ) {
                    Text("Leave", color = CombatCrimson)
                }
            }

            Text(clan.motto, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            // Shared Clan XP Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CLAN LV.${levelInfo.level} • ${levelInfo.title.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${levelInfo.currentXpInLevel} / ${levelInfo.xpRequiredForNext} SHARED XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                }
                LinearProgressIndicator(
                    progress = { levelInfo.progress },
                    color = banner.primaryColor,
                    trackColor = ObsidianElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Text(
                text = "Squad Roster: ${members.joinToString(" • ")}",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecruit,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("clan_recruit_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Recruit", color = ElectricCyan)
                }
                Button(
                    onClick = onDrill,
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                    modifier = Modifier
                        .weight(1.2f)
                        .heightIn(min = 48.dp)
                        .testTag("clan_drill_button")
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Squad Drill (+XP)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClanDirectoryCard(
    clan: ClanEntity,
    isCurrent: Boolean,
    onJoin: () -> Unit
) {
    val banner = ClanBannerCatalog.byId(clan.equippedBannerId)
    val level = ClanLevelInfo.fromClanXp(clan.clanXp)
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isCurrent) NanoEmerald else TacticalBorder, RoundedCornerShape(14.dp))
            .testTag("clan_directory_card_${clan.tag}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(banner.primaryColor.copy(alpha = 0.2f))
                    .border(1.5.dp, banner.primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(clan.tag, style = MaterialTheme.typography.labelMedium, color = banner.primaryColor, fontWeight = FontWeight.ExtraBold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("[${clan.tag}] ${clan.name}", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Lv.${level.level} • ${clan.memberCount}/8 Members • ${clan.clanXp} XP", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                Text(clan.motto, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onJoin,
                enabled = !isCurrent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) ObsidianElevated else ElectricCyan,
                    contentColor = if (isCurrent) NanoEmerald else ObsidianVoid
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("clan_join_${clan.tag}")
            ) {
                Text(if (isCurrent) "Active" else "Join", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CreateClanDialog(
    onConfirm: (name: String, tag: String, motto: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var motto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = { Text("Create Infine Clan", color = TacticalAmber, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(22) },
                    label = { Text("Clan Name (min 3 chars)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_clan_name_input")
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(5) },
                    label = { Text("Clan Tag (2–5 chars)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_clan_tag_input")
                )
                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it.take(60) },
                    label = { Text("Squad Motto (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_clan_motto_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, tag, motto) },
                enabled = name.trim().length >= 3 && tag.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_clan_submit_button")
            ) {
                Text("Create Clan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun RecruitClanMemberDialog(
    clan: ClanEntity,
    friends: List<FriendEntity>,
    localCallsign: String,
    onRecruit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentMembers = clan.membersCsv.split(",").filter { it.isNotBlank() }.map { it.lowercase() }.toSet()
    val suggestions = remember(friends, currentMembers) {
        (friends.map { it.callsign } + listOf("Kira_INF", "AuraMend", "ZedRush", "NovaSniper", "ShadowAK"))
            .filter { it.lowercase() !in currentMembers && !it.equals(localCallsign, ignoreCase = true) }
            .distinct()
    }
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = { Text("Recruit to [${clan.tag}]", color = ElectricCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Recruited squadmates boost your clan roster and contribute +45 Shared Clan XP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                suggestions.take(4).forEach { callsign ->
                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                            .clickable { onRecruit(callsign) }
                            .testTag("recruit_suggestion_$callsign")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(callsign, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("+ Recruit", style = MaterialTheme.typography.labelLarge, color = NanoEmerald)
                        }
                    }
                }
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it.take(16) },
                    label = { Text("Or enter callsign") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (customName.isNotBlank()) onRecruit(customName) },
                enabled = customName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Recruit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}
