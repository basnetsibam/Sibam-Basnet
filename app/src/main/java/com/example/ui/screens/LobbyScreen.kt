package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.VoiceChatState
import com.example.data.ArenaLevelCatalog
import com.example.data.CharacterSkinCatalog
import com.example.data.ClanBannerCatalog
import com.example.data.ClanLevelInfo
import com.example.data.EmoteCatalog
import com.example.data.GameMode
import com.example.data.HonorTier
import com.example.data.InfineMapBlueprint
import com.example.data.OperatorCatalog
import com.example.data.PlayerLevelInfo
import com.example.data.RankTier
import com.example.data.RoboHonorPolicy
import com.example.data.SkillType
import com.example.data.SquadMode
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import com.example.data.local.ClanEntity
import com.example.data.local.FriendEntity
import com.example.data.local.PlayerProfileEntity
import com.example.ui.MatchmakingState
import kotlinx.coroutines.delay
import kotlin.math.hypot
import com.example.ui.theme.PlasmaViolet
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
fun LobbyScreen(
    profile: PlayerProfileEntity,
    matchmaking: MatchmakingState,
    friends: List<FriendEntity> = emptyList(),
    joinedClan: ClanEntity? = null,
    friendsCount: Int,
    invitedSquadCount: Int,
    equippedSkinName: String,
    canClaimDaily: Boolean,
    globalWinsRank: Int,
    globalKdRank: Int,
    voiceChatState: VoiceChatState = VoiceChatState(),
    honorLockNotice: String? = null,
    onClearHonorLockNotice: () -> Unit = {},
    onSelectMode: (GameMode, SquadMode) -> Unit,
    onSelectArenaLevel: (Int) -> Unit,
    onStartMatchmaking: () -> Unit,
    onCancelMatchmaking: () -> Unit,
    onOpenArmory: () -> Unit,
    onOpenStore: () -> Unit = {},
    onOpenClan: () -> Unit = {},
    onOpenInfineMap: () -> Unit,
    onOpenDailyRewards: () -> Unit,
    onOpenFriends: () -> Unit,
    onToggleFriendSquad: (FriendEntity) -> Unit = {},
    onEquipOperator: (String) -> Unit = {},
    onEquipWeapon: (WeaponId) -> Unit = {},
    onSelectCharacterSkin: (String) -> Unit = {},
    onOpenLeaderboard: () -> Unit,
    onOpenCurrencyVault: () -> Unit,
    onToggleVoiceMic: () -> Unit = {},
    onVoicePermissionResult: (Boolean) -> Unit = {},
    onToggleVoiceSpeaker: () -> Unit = {},
    isSfxEnabled: Boolean = true,
    onToggleSfx: () -> Unit = {}
) {
    val selectedMode = runCatching { GameMode.valueOf(profile.preferredModeName) }
        .getOrDefault(GameMode.BATTLE_ROYALE_RANKED)
    val selectedSquad = runCatching { SquadMode.valueOf(profile.preferredSquadName) }
        .getOrDefault(SquadMode.SQUAD)
    val equippedWeapon = WeaponCatalog.byId(
        runCatching { WeaponId.valueOf(profile.equippedWeaponName) }.getOrDefault(WeaponId.AK47)
    )
    val equippedOperator = OperatorCatalog.byId(profile.equippedOperatorId)
    val equippedCharacterSkin = CharacterSkinCatalog.byId(profile.equippedCharacterSkinId)
    val equippedEmote = EmoteCatalog.byId(profile.equippedEmoteId)
    val brTier = RankTier.fromRp(profile.brRp)
    val csTier = RankTier.fromRp(profile.csRp)
    val levelInfo = PlayerLevelInfo.fromTotalXp(profile.totalXp)
    val activeArenaLevel = ArenaLevelCatalog.byLevel(profile.selectedArenaLevel)
    val isRankedCleared = RoboHonorPolicy.canPlayRanked(profile.honorScore)
    var showRoboRulesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(honorLockNotice) {
        if (honorLockNotice != null) {
            delay(5000L)
            onClearHonorLockNotice()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onVoicePermissionResult(granted)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner featuring Infine Clock Tower art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, TacticalBorder, RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_lobby_hero),
                    contentDescription = "Infine Clock Tower Arena",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x440B101B),
                                    Color(0xEE0B101B)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ObsidianSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, TacticalAmber.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = profile.callsign,
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ObsidianSurface.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSfxEnabled) ElectricCyan.copy(alpha = 0.7f) else TacticalBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onToggleSfx)
                                    .testTag("lobby_sfx_toggle_button")
                            ) {
                                Text(
                                    text = if (isSfxEnabled) "🔊 SFX" else "🔇 MUTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSfxEnabled) ElectricCyan else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                color = ObsidianSurface.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (RoboHonorPolicy.canPlayRanked(profile.honorScore)) NanoEmerald.copy(alpha = 0.7f) else CombatCrimson.copy(alpha = 0.7f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .testTag("lobby_anti_cheat_indicator")
                            ) {
                                Text(
                                    text = if (RoboHonorPolicy.canPlayRanked(profile.honorScore)) "🛡️ AC SHIELD" else "⚠️ AC WATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (RoboHonorPolicy.canPlayRanked(profile.honorScore)) NanoEmerald else CombatCrimson,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                color = ObsidianSurface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.local_arena_notice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "INFINE ROYALE",
                            style = MaterialTheme.typography.displaySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "Clock Tower • Factory • School • Hospital • Revival Points",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Currency, Daily Login Rewards & Social Friends Quick Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .border(1.dp, TacticalAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenCurrencyVault)
                        .testTag("lobby_credits_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = "Infine Gold",
                                tint = TacticalAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${profile.credits}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Diamond,
                                contentDescription = "Infine Gems",
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${profile.gems}",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onOpenDailyRewards,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = if (canClaimDaily) 1.5.dp else 1.dp,
                            color = if (canClaimDaily) NanoEmerald else TacticalBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("lobby_daily_rewards_button")
                ) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = "Daily Rewards",
                        tint = if (canClaimDaily) NanoEmerald else TacticalAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canClaimDaily) "Daily Gift!" else "Daily",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canClaimDaily) NanoEmerald else TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = onOpenFriends,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .testTag("lobby_friends_button")
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = "Social Friends",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (invitedSquadCount > 0) "Squad ($invitedSquadCount)" else "Friends ($friendsCount)",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
            }

            // Dual Ranked Badges (BR Ranked + CS Ranked)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RankSummaryCard(
                    title = "BR RANKED",
                    tier = brTier,
                    rp = profile.brRp,
                    modifier = Modifier.weight(1f)
                )
                RankSummaryCard(
                    title = "CLASH SQUAD",
                    tier = csTier,
                    rp = profile.csRp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Robo-Sentinel Honor Monitor & Ranked Fair-Play Gate
            RoboHonorSentinelCard(
                honorScore = profile.honorScore,
                lastNotice = profile.lastHonorNotice,
                onSelectCasualBr = { onSelectMode(GameMode.BATTLE_ROYALE_CASUAL, selectedSquad) },
                onSelectTraining = { onSelectMode(GameMode.TRAINING_GROUND, selectedSquad) },
                onOpenRules = { showRoboRulesDialog = true }
            )

            if (honorLockNotice != null) {
                val noticeColor = if (isRankedCleared) NanoEmerald else CombatCrimson
                Surface(
                    color = noticeColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, noticeColor, RoundedCornerShape(12.dp))
                        .clickable { onClearHonorLockNotice() }
                        .testTag("lobby_robo_honor_notice")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRankedCleared) Icons.Default.SmartToy else Icons.Default.Warning,
                            contentDescription = "Robo-Sentinel Alert",
                            tint = noticeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = honorLockNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Robo Notice",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Global Leaderboard & In-Game Store Shortcut Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, TacticalAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenLeaderboard)
                        .testTag("lobby_leaderboard_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TacticalAmber.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Public, contentDescription = "Global Leaderboard", tint = TacticalAmber)
                        }
                        Column {
                            Text("LEADERBOARD", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                            Text(
                                text = "W #$globalWinsRank • K/D #$globalKdRank",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, PlasmaViolet.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenStore)
                        .testTag("lobby_store_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PlasmaViolet.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = "In-Game Store", tint = PlasmaViolet)
                        }
                        Column {
                            Text("IN-GAME STORE", style = MaterialTheme.typography.labelSmall, color = PlasmaViolet)
                            Text(
                                text = "Skins • Wraps • Emotes",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Clan & Shared XP Banner Card
            run {
                val clanBanner = joinedClan?.let { ClanBannerCatalog.byId(it.equippedBannerId) }
                val clanLevel = joinedClan?.let { ClanLevelInfo.fromClanXp(it.clanXp) }
                val accent = clanBanner?.primaryColor ?: ElectricCyan
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenClan)
                        .testTag("lobby_clan_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(accent.copy(alpha = 0.18f))
                                        .border(1.dp, accent, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (clanBanner != null) {
                                        Text(clanBanner.emblemTag, style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.ExtraBold)
                                    } else {
                                        Icon(Icons.Default.Shield, contentDescription = "Clan Hub", tint = accent)
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (joinedClan != null && clanLevel != null) {
                                            "[${joinedClan.tag}] ${joinedClan.name} • Lv.${clanLevel.level}"
                                        } else {
                                            "CLAN & SQUAD MISSIONS"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (joinedClan != null) accent else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (joinedClan != null && clanBanner != null) {
                                            "${clanBanner.name} • ${joinedClan.memberCount}/8 Members • Tap for Missions"
                                        } else {
                                            "Create or join a clan to unlock banners & shared XP"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Surface(color = accent.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = if (joinedClan != null) "CLAN HUB" else "JOIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        if (clanLevel != null) {
                            LinearProgressIndicator(
                                progress = { clanLevel.progress },
                                color = accent,
                                trackColor = ObsidianElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            // Squad Voice Channel Quick Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (voiceChatState.isMicActive) NanoEmerald.copy(alpha = 0.65f) else TacticalBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .testTag("lobby_voice_chat_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (voiceChatState.isMicActive) "SQUAD VOICE • MIC LIVE" else "SQUAD VOICE CHAT",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voiceChatState.isMicActive) NanoEmerald else ElectricCyan
                        )
                        Text(
                            text = voiceChatState.statusNotice ?: "Tap mic to talk with your squad on Infine",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (voiceChatState.isMicActive) {
                            LinearProgressIndicator(
                                progress = { voiceChatState.micLevel.coerceIn(0.08f, 1f) },
                                color = NanoEmerald,
                                trackColor = ObsidianElevated,
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (voiceChatState.hasMicPermission) {
                                    onToggleVoiceMic()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("lobby_voice_mic_button")
                        ) {
                            Icon(
                                imageVector = if (voiceChatState.isMicActive) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = if (voiceChatState.isMicActive) "Mute Mic" else "Enable Mic",
                                tint = if (voiceChatState.isMicActive) NanoEmerald else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = onToggleVoiceSpeaker,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("lobby_voice_speaker_button")
                        ) {
                            Icon(
                                imageVector = if (voiceChatState.isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (voiceChatState.isSpeakerEnabled) "Mute Speaker" else "Unmute Speaker",
                                tint = if (voiceChatState.isSpeakerEnabled) ElectricCyan else CombatCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Player Level & XP Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .testTag("lobby_player_level_card")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LEVEL ${levelInfo.level} • ${levelInfo.title.uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricCyan
                        )
                        Text(
                            text = "${levelInfo.currentXpInLevel} / ${levelInfo.xpRequiredForNext} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { levelInfo.progress },
                        color = ElectricCyan,
                        trackColor = ObsidianElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            // Pre-Game Party Members Roster (Local Player + Up to 3 Invited Squadmates)
            PreGamePartyCard(
                profile = profile,
                selectedMode = selectedMode,
                selectedSquad = selectedSquad,
                partyMembers = friends.filter { it.isInSquad }.take(3),
                equippedOperatorName = equippedOperator.name,
                equippedSuitColor = equippedCharacterSkin.suitColor,
                equippedRimColor = equippedCharacterSkin.rimColor,
                isMicActive = voiceChatState.isMicActive,
                onOpenFriends = onOpenFriends,
                onRemovePartyMember = onToggleFriendSquad
            )

            // Interactive Pre-Game Character & Loadout Selector
            PreGameLoadoutSelectorCard(
                profile = profile,
                equippedWeapon = equippedWeapon,
                equippedSkinName = equippedSkinName,
                equippedOperator = equippedOperator,
                equippedCharacterSkin = equippedCharacterSkin,
                equippedEmoteName = equippedEmote.name,
                onEquipOperator = onEquipOperator,
                onEquipWeapon = onEquipWeapon,
                onSelectCharacterSkin = onSelectCharacterSkin,
                onOpenArmory = onOpenArmory,
                onOpenStore = onOpenStore
            )

            // Game Mode Selector (BR Ranked, CS Ranked, Casual BR, Training Ground)
            Text("SELECT MODE", style = MaterialTheme.typography.titleLarge, color = TextPrimary)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GameMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val isLockedByRobo = mode.isRanked && !isRankedCleared
                    val baseAccent = when (mode) {
                        GameMode.BATTLE_ROYALE_RANKED -> TacticalAmber
                        GameMode.CLASH_SQUAD_RANKED -> CombatCrimson
                        GameMode.BATTLE_ROYALE_CASUAL -> ElectricCyan
                        GameMode.TRAINING_GROUND -> NanoEmerald
                    }
                    val accent = if (isLockedByRobo) CombatCrimson.copy(alpha = 0.7f) else baseAccent
                    val icon = when (mode) {
                        GameMode.BATTLE_ROYALE_RANKED -> Icons.Default.EmojiEvents
                        GameMode.CLASH_SQUAD_RANKED -> Icons.Default.Shield
                        GameMode.BATTLE_ROYALE_CASUAL -> Icons.Default.Groups
                        GameMode.TRAINING_GROUND -> Icons.Default.FitnessCenter
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isLockedByRobo -> ObsidianSurface.copy(alpha = 0.72f)
                                isSelected -> ObsidianElevated
                                else -> ObsidianCard
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected && !isLockedByRobo) 2.dp else 1.dp,
                                color = when {
                                    isLockedByRobo -> CombatCrimson.copy(alpha = 0.45f)
                                    isSelected -> accent
                                    else -> TacticalBorder
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectMode(mode, selectedSquad) }
                            .testTag("mode_card_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLockedByRobo) Icons.Default.Lock else icon,
                                    contentDescription = mode.title,
                                    tint = accent
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isLockedByRobo) TextSecondary else TextPrimary
                                    )
                                    if (isLockedByRobo) {
                                        Surface(color = CombatCrimson.copy(alpha = 0.22f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "LOCKED (< 90 HONOR)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CombatCrimson,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (mode.isRanked) {
                                        Surface(color = accent.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                "RANKED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = accent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(color = accent.copy(alpha = 0.18f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                if (mode == GameMode.BATTLE_ROYALE_CASUAL) "+8 HONOR" else "+5 HONOR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = accent,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(mode.subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(mode.maxPlayersLabel, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                            }
                        }
                    }
                }
            }

            // Solo / Squad Selector for BR Ranked & Casual BR
            AnimatedVisibility(visible = selectedMode == GameMode.BATTLE_ROYALE_RANKED || selectedMode == GameMode.BATTLE_ROYALE_CASUAL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SquadMode.entries.forEach { sq ->
                        val active = selectedSquad == sq
                        FilterChip(
                            selected = active,
                            onClick = { onSelectMode(selectedMode, sq) },
                            label = { Text(sq.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (sq == SquadMode.SQUAD) Icons.Default.Groups else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TacticalAmber.copy(alpha = 0.24f),
                                selectedLabelColor = TacticalAmber,
                                selectedLeadingIconColor = TacticalAmber
                            ),
                            modifier = Modifier.testTag("squad_chip_${sq.name}")
                        )
                    }
                }
            }

            // Infinite Arena Combat Levels (Level 1 -> ∞)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("INFINITE ARENA LEVELS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        text = "${activeArenaLevel.difficultyLabel} • ${String.format(java.util.Locale.US, "%.2f", activeArenaLevel.xpMultiplier)}x XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeArenaLevel.accentColor
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, activeArenaLevel.accentColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onSelectArenaLevel((profile.selectedArenaLevel - 1).coerceAtLeast(1)) },
                                    enabled = profile.selectedArenaLevel > 1,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("arena_level_dec")
                                ) {
                                    Text("-1", fontWeight = FontWeight.Bold)
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LEVEL ${activeArenaLevel.level}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = activeArenaLevel.accentColor
                                )
                                Text(
                                    text = activeArenaLevel.name.substringAfter("• ").trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onSelectArenaLevel(profile.selectedArenaLevel + 1) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeArenaLevel.accentColor,
                                        contentColor = ObsidianVoid
                                    ),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("arena_level_inc")
                                ) {
                                    Text("+1", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enemy HP ${activeArenaLevel.botHp} • Shield ${activeArenaLevel.botShield}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = ObsidianElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { onSelectArenaLevel(1) }
                                        .testTag("arena_level_reset")
                                ) {
                                    Text(
                                        "LVL 1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                                Surface(
                                    color = activeArenaLevel.accentColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { onSelectArenaLevel(profile.selectedArenaLevel + 5) }
                                        .testTag("arena_level_plus5")
                                ) {
                                    Text(
                                        "+5 LVLS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = activeArenaLevel.accentColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Main-Screen Mini-Map tracking player position & nearby revival points
            LobbyMiniMapCard(
                activeSectorId = activeArenaLevel.sectorId,
                accentColor = activeArenaLevel.accentColor,
                onOpenFullMap = onOpenInfineMap
            )

            // Primary Launch Action + Infine Map Quick Inspect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenInfineMap,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("lobby_inspect_map_button")
                ) {
                    Text("Infine Map")
                }

                val isSelectedRankedLocked = selectedMode.isRanked && !isRankedCleared
                val startBgColor = when {
                    isSelectedRankedLocked -> CombatCrimson
                    selectedMode == GameMode.TRAINING_GROUND -> NanoEmerald
                    selectedMode == GameMode.BATTLE_ROYALE_CASUAL -> ElectricCyan
                    else -> TacticalAmber
                }
                val startText = when {
                    isSelectedRankedLocked -> "RANKED LOCKED — SWITCH CASUAL"
                    selectedMode == GameMode.TRAINING_GROUND -> "ENTER TRAINING (+5 HONOR)"
                    selectedMode == GameMode.BATTLE_ROYALE_CASUAL -> "PLAY CASUAL BR (+8 HONOR)"
                    else -> "FIND RANKED MATCH"
                }

                Button(
                    onClick = {
                        if (isSelectedRankedLocked) {
                            onSelectMode(GameMode.BATTLE_ROYALE_CASUAL, selectedSquad)
                        } else {
                            onStartMatchmaking()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = startBgColor,
                        contentColor = ObsidianVoid
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("start_match_button")
                ) {
                    Icon(
                        imageVector = if (isSelectedRankedLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = startText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Ranked Matchmaking Queue Overlay
        if (matchmaking.isSearching) {
            MatchmakingQueueOverlay(
                state = matchmaking,
                onCancel = onCancelMatchmaking
            )
        }

        if (showRoboRulesDialog) {
            RoboHonorRulesDialog(
                honorScore = profile.honorScore,
                onDismiss = { showRoboRulesDialog = false }
            )
        }
    }
}

@Composable
private fun RankSummaryCard(
    title: String,
    tier: RankTier,
    rp: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = modifier.border(1.dp, tier.badgeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tier.badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MilitaryTech, contentDescription = tier.label, tint = tier.badgeColor)
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(tier.label, style = MaterialTheme.typography.titleMedium, color = tier.badgeColor)
                Text("$rp RP", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun MatchmakingQueueOverlay(
    state: MatchmakingState,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.92f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, if (state.isLobbyLocked) NanoEmerald else ElectricCyan, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isLobbyLocked) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NanoEmerald, modifier = Modifier.size(28.dp))
                    } else {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isLobbyLocked) stringResource(R.string.matchmaking_found) else stringResource(R.string.matchmaking_searching),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (state.isLobbyLocked) NanoEmerald else ElectricCyan
                        )
                        Text(state.statusMessage, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Icon(Icons.Default.Radar, contentDescription = null, tint = TacticalAmber)
                }

                LazyColumn(
                    modifier = Modifier.height(240.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.competitors) { competitor ->
                        Surface(
                            color = if (competitor.isLocalPlayer) ObsidianElevated else ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (competitor.isLocalPlayer) TacticalAmber else TacticalBorder,
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (competitor.isLocalPlayer) "${competitor.callsign} (YOU)" else competitor.callsign,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (competitor.isLocalPlayer) TacticalAmber else TextPrimary
                                    )
                                    Text(
                                        text = "${competitor.teamLabel} • ${competitor.rankTier.label} (${competitor.rp} RP)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = competitor.rankTier.badgeColor
                                    )
                                }
                                Text("READY", style = MaterialTheme.typography.labelSmall, color = NanoEmerald)
                            }
                        }
                    }
                }

                if (!state.isLobbyLocked) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cancel_matchmaking_button")
                    ) {
                        Text(stringResource(R.string.btn_cancel_matchmaking), color = CombatCrimson)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreGamePartyCard(
    profile: PlayerProfileEntity,
    selectedMode: GameMode,
    selectedSquad: SquadMode,
    partyMembers: List<FriendEntity>,
    equippedOperatorName: String,
    equippedSuitColor: Color,
    equippedRimColor: Color,
    isMicActive: Boolean,
    onOpenFriends: () -> Unit,
    onRemovePartyMember: (FriendEntity) -> Unit
) {
    val isTeamMode = selectedMode == GameMode.CLASH_SQUAD_RANKED || selectedSquad == SquadMode.SQUAD
    val partyTitle = if (isTeamMode) {
        "PRE-GAME PARTY (${1 + partyMembers.size}/4)"
    } else {
        "PRE-GAME PARTY (SOLO)"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("lobby_party_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(partyTitle, style = MaterialTheme.typography.labelLarge, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isTeamMode) "Squadmates deploy alongside you into Infine" else "Solo queue active — switch to 4-Player Squad to fill slots",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenFriends,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("party_manage_squad_button")
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Invite Party Member", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Invite", color = ElectricCyan)
                }
            }

            // Local Player Slot (Party Leader)
            Surface(
                color = ObsidianElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalAmber.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .testTag("party_leader_slot")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ObsidianSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            drawCircle(color = equippedRimColor, radius = size.minDimension * 0.45f)
                            drawCircle(color = equippedSuitColor, radius = size.minDimension * 0.34f)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(profile.callsign, style = MaterialTheme.typography.titleMedium, color = TacticalAmber, fontWeight = FontWeight.Bold)
                            Surface(color = TacticalAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "LEADER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TacticalAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "$equippedOperatorName • ${RankTier.fromRp(profile.brRp).label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isMicActive) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic Active", tint = NanoEmerald, modifier = Modifier.size(16.dp))
                        }
                        Surface(color = NanoEmerald.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = NanoEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (isTeamMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (slotIdx in 0 until 3) {
                        val ally = partyMembers.getOrNull(slotIdx)
                        if (ally != null) {
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, NanoEmerald.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                    .testTag("party_member_slot_$slotIdx")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ally.callsign,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove ${ally.callsign} from party",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onRemovePartyMember(ally) }
                                        )
                                    }
                                    Text(ally.rankLabel, style = MaterialTheme.typography.labelSmall, color = ElectricCyan, maxLines = 1)
                                    Text("READY", style = MaterialTheme.typography.labelSmall, color = NanoEmerald, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                color = ObsidianVoid.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                                    .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                                    .clickable(onClick = onOpenFriends)
                                    .testTag("party_empty_slot_$slotIdx")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = "Invite Friend", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("+ Invite", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreGameLoadoutSelectorCard(
    profile: PlayerProfileEntity,
    equippedWeapon: com.example.data.WeaponSpec,
    equippedSkinName: String,
    equippedOperator: com.example.data.CharacterOperator,
    equippedCharacterSkin: com.example.data.CharacterSkinSpec,
    equippedEmoteName: String,
    onEquipOperator: (String) -> Unit,
    onEquipWeapon: (WeaponId) -> Unit,
    onSelectCharacterSkin: (String) -> Unit,
    onOpenArmory: () -> Unit,
    onOpenStore: () -> Unit
) {
    val unlockedSuits = profile.unlockedCharacterSkinsCsv.split(",").filter { it.isNotBlank() }.toSet() + "default_vanguard"

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TacticalAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("lobby_loadout_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("CHARACTER & LOADOUT", style = MaterialTheme.typography.labelLarge, color = TacticalAmber, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${equippedOperator.name} (${equippedCharacterSkin.name}) • ${equippedWeapon.name} ($equippedSkinName)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onOpenArmory,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("lobby_open_armory_button")
                ) {
                    Text("Armory", color = ElectricCyan)
                }
            }

            // 1. Operator Selector
            Text("SELECT CHARACTER SKILL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OperatorCatalog.all.forEach { op ->
                    val selected = op.id == equippedOperator.id
                    val opIcon = when (op.skillType) {
                        SkillType.HEAL -> Icons.Default.LocalHospital
                        SkillType.REVIVE -> Icons.Default.Favorite
                        SkillType.DAMAGE -> Icons.Default.Bolt
                    }
                    Surface(
                        color = if (selected) op.accentColor.copy(alpha = 0.18f) else ObsidianSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) op.accentColor else TacticalBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onEquipOperator(op.id) }
                            .testTag("lobby_select_operator_${op.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(opIcon, contentDescription = op.name, tint = op.accentColor, modifier = Modifier.size(15.dp))
                                Text(
                                    text = op.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) op.accentColor else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = op.skillType.shortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 2. Character Suit / Skin Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CHARACTER SUIT • EMOTE: ${equippedEmoteName.uppercase()}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = "Store",
                    style = MaterialTheme.typography.labelSmall,
                    color = PlasmaViolet,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onOpenStore)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CharacterSkinCatalog.all) { skin ->
                    val unlocked = skin.id in unlockedSuits || (skin.costCredits == 0 && skin.costGems == 0)
                    val selected = profile.equippedCharacterSkinId == skin.id
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (unlocked) onSelectCharacterSkin(skin.id) else onOpenStore()
                        },
                        label = {
                            Text(
                                text = if (unlocked) skin.name else "${skin.name} (Store)",
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(skin.suitColor)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = skin.suitColor.copy(alpha = 0.24f),
                            selectedLabelColor = skin.suitColor
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("lobby_suit_chip_${skin.id}")
                    )
                }
            }

            // 3. Primary Weapon Selector (all 8 guns)
            Text("PRIMARY WEAPON LOADOUT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WeaponCatalog.all) { weapon ->
                    val selected = weapon.id == equippedWeapon.id
                    FilterChip(
                        selected = selected,
                        onClick = { onEquipWeapon(weapon.id) },
                        label = { Text(weapon.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = weapon.accentColor.copy(alpha = 0.24f),
                            selectedLabelColor = weapon.accentColor,
                            selectedLeadingIconColor = weapon.accentColor
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("lobby_weapon_chip_${weapon.id.name}")
                    )
                }
            }
        }
    }
}

@Composable
private fun RoboHonorSentinelCard(
    honorScore: Int,
    lastNotice: String,
    onSelectCasualBr: () -> Unit,
    onSelectTraining: () -> Unit,
    onOpenRules: () -> Unit
) {
    val tier = RoboHonorPolicy.tierFor(honorScore)
    val accent = tier.badgeColor
    val isLocked = !RoboHonorPolicy.canPlayRanked(honorScore)

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .testTag("robo_honor_sentinel_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Robo-Sentinel Avatar Canvas
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.5.dp, accent, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(44.dp)) {
                        val w = size.width
                        val h = size.height
                        // Antenna
                        drawLine(
                            color = accent,
                            start = Offset(w * 0.5f, h * 0.04f),
                            end = Offset(w * 0.5f, h * 0.22f),
                            strokeWidth = 3f
                        )
                        drawCircle(color = accent, radius = 4f, center = Offset(w * 0.5f, h * 0.06f))
                        // Robo Chassis Head
                        drawRoundRect(
                            color = Color(0xFF162032),
                            topLeft = Offset(w * 0.15f, h * 0.22f),
                            size = Size(w * 0.70f, h * 0.62f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = accent,
                            topLeft = Offset(w * 0.15f, h * 0.22f),
                            size = Size(w * 0.70f, h * 0.62f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 2.5f)
                        )
                        // Visor Screen
                        drawRoundRect(
                            color = Color(0xFF0A101B),
                            topLeft = Offset(w * 0.22f, h * 0.34f),
                            size = Size(w * 0.56f, h * 0.26f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Glowing Robo Eyes
                        drawCircle(color = accent, radius = if (isLocked) 4.5f else 5f, center = Offset(w * 0.37f, h * 0.47f))
                        drawCircle(color = accent, radius = if (isLocked) 4.5f else 5f, center = Offset(w * 0.63f, h * 0.47f))
                        // Vocalizer grill
                        drawLine(
                            color = accent.copy(alpha = 0.75f),
                            start = Offset(w * 0.32f, h * 0.71f),
                            end = Offset(w * 0.68f, h * 0.71f),
                            strokeWidth = 2.5f
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROBO-SENTINEL MONITOR",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = accent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tier.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$honorScore / 100",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "• Ranked Gate: ${RoboHonorPolicy.MIN_RANKED_HONOR}+",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLocked) CombatCrimson else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = lastNotice.ifBlank { RoboHonorPolicy.statusAdvice(honorScore) },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (honorScore / 100f).coerceIn(0f, 1f) },
                color = accent,
                trackColor = ObsidianElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocked) {
                    Button(
                        onClick = onSelectCasualBr,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("robo_recover_casual_button")
                    ) {
                        Text("Casual BR (+8)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSelectTraining,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("robo_recover_training_button")
                    ) {
                        Text("Training (+5)", color = NanoEmerald, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = RoboHonorPolicy.statusAdvice(honorScore),
                        style = MaterialTheme.typography.labelSmall,
                        color = NanoEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = onOpenRules,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("robo_honor_rules_button")
                ) {
                    Text("Rules", color = TacticalAmber, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun RoboHonorRulesDialog(
    honorScore: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        icon = {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Robo-Sentinel Rules",
                tint = ElectricCyan,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Robo-Sentinel Honor Rules", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current Score: $honorScore / 100 (Ranked requires ${RoboHonorPolicy.MIN_RANKED_HONOR}+)",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (RoboHonorPolicy.canPlayRanked(honorScore)) NanoEmerald else CombatCrimson,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Early Exit (Ranked BR / Clash Squad): -${RoboHonorPolicy.PENALTY_RANKED_QUIT} Honor Score",
                    style = MaterialTheme.typography.bodySmall,
                    color = CombatCrimson
                )
                Text(
                    text = "• Early Exit (Infine Casual): -${RoboHonorPolicy.PENALTY_CASUAL_QUIT} Honor Score",
                    style = MaterialTheme.typography.bodySmall,
                    color = TacticalAmber
                )
                Text(
                    text = "• Below 90 Honor: Both Ranked modes are LOCKED.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• How to Plus (+) Score: Finish Infine Casual (+${RoboHonorPolicy.GAIN_CASUAL_FINISH}), complete a Training Drill (+${RoboHonorPolicy.GAIN_TRAINING_DRILL}), or finish Ranked cleanly (+${RoboHonorPolicy.GAIN_RANKED_CLEAN}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = NanoEmerald
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = ElectricCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun LobbyMiniMapCard(
    activeSectorId: String,
    accentColor: Color,
    onOpenFullMap: () -> Unit
) {
    val sectorLocation = remember(activeSectorId) {
        InfineMapBlueprint.locations.firstOrNull { it.id == activeSectorId }
            ?: InfineMapBlueprint.locations.first()
    }
    var trackedX by remember { mutableStateOf(sectorLocation.x) }
    var trackedY by remember { mutableStateOf(sectorLocation.y) }

    LaunchedEffect(activeSectorId) {
        trackedX = sectorLocation.x
        trackedY = sectorLocation.y
    }

    val nearestLocation = remember(trackedX, trackedY) {
        InfineMapBlueprint.locations.minByOrNull { loc ->
            hypot(loc.x - trackedX, loc.y - trackedY)
        } ?: sectorLocation
    }

    val sortedRevivals = remember(trackedX, trackedY) {
        InfineMapBlueprint.revivalPoints.map { rp ->
            val distPx = hypot(rp.x - trackedX, rp.y - trackedY)
            rp to distPx
        }.sortedBy { it.second }
    }
    val nearbyRadiusPx = 950f
    val nearbyCount = remember(sortedRevivals) {
        sortedRevivals.count { it.second <= nearbyRadiusPx }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, ElectricCyan.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .testTag("lobby_minimap_card")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🛰️ INFINE MINI-MAP • REVIVAL RADAR",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "📍 ${nearestLocation.name} (${trackedX.toInt()}, ${trackedY.toInt()}) • $nearbyCount nearby",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onOpenFullMap,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("lobby_minimap_full_map_button")
                ) {
                    Text("🗺️ Full Map", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Interactive Square Mini-Map Radar
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianVoid)
                        .border(1.5.dp, TacticalBorder, RoundedCornerShape(12.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                    val h = size.height.toFloat().coerceAtLeast(1f)
                                    trackedX = ((tapOffset.x / w) * InfineMapBlueprint.WORLD_SIZE)
                                        .coerceIn(80f, InfineMapBlueprint.WORLD_SIZE - 80f)
                                    trackedY = ((tapOffset.y / h) * InfineMapBlueprint.WORLD_SIZE)
                                        .coerceIn(80f, InfineMapBlueprint.WORLD_SIZE - 80f)
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                    val h = size.height.toFloat().coerceAtLeast(1f)
                                    trackedX = ((change.position.x / w) * InfineMapBlueprint.WORLD_SIZE)
                                        .coerceIn(80f, InfineMapBlueprint.WORLD_SIZE - 80f)
                                    trackedY = ((change.position.y / h) * InfineMapBlueprint.WORLD_SIZE)
                                        .coerceIn(80f, InfineMapBlueprint.WORLD_SIZE - 80f)
                                }
                            }
                            .testTag("lobby_minimap_canvas")
                    ) {
                        val scale = size.minDimension / InfineMapBlueprint.WORLD_SIZE

                        // Subtle radar grid
                        val gridStep = 400f * scale
                        var gx = gridStep
                        while (gx < size.width) {
                            drawLine(
                                color = Color(0xFF1B2436),
                                start = Offset(gx, 0f),
                                end = Offset(gx, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color(0xFF1B2436),
                                start = Offset(0f, gx),
                                end = Offset(size.width, gx),
                                strokeWidth = 1f
                            )
                            gx += gridStep
                        }

                        // Infine roads
                        InfineMapBlueprint.roads.forEach { road ->
                            drawLine(
                                color = Color(0xFF283244),
                                start = Offset(road.x1 * scale, road.y1 * scale),
                                end = Offset(road.x2 * scale, road.y2 * scale),
                                strokeWidth = (road.width * scale).coerceAtLeast(3f),
                                cap = StrokeCap.Round
                            )
                        }

                        // Obstacles / buildings
                        InfineMapBlueprint.obstacles.forEach { rect ->
                            drawRect(
                                color = Color(0xFF3E4C63),
                                topLeft = Offset(rect.left * scale, rect.top * scale),
                                size = Size(
                                    (rect.width * scale).coerceAtLeast(2f),
                                    (rect.height * scale).coerceAtLeast(2f)
                                )
                            )
                        }

                        // Sector POIs
                        InfineMapBlueprint.locations.forEach { loc ->
                            val isCurrentSector = loc.id == nearestLocation.id
                            drawCircle(
                                color = if (isCurrentSector) accentColor else Color(0xFF6B7B94),
                                radius = if (isCurrentSector) 4.2f else 2.8f,
                                center = Offset(loc.x * scale, loc.y * scale)
                            )
                        }

                        val playerPos = Offset(trackedX * scale, trackedY * scale)

                        // Nearby radar range rings around player
                        drawCircle(
                            color = NanoEmerald.copy(alpha = 0.25f),
                            radius = nearbyRadiusPx * scale,
                            center = playerPos,
                            style = Stroke(
                                width = 1.4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
                            )
                        )
                        drawCircle(
                            color = ElectricCyan.copy(alpha = 0.28f),
                            radius = (nearbyRadiusPx * 0.5f) * scale,
                            center = playerPos,
                            style = Stroke(width = 1f)
                        )

                        // Revival Points + radar tether to nearby revival beacons
                        sortedRevivals.forEachIndexed { index, (rp, distPx) ->
                            val rpPos = Offset(rp.x * scale, rp.y * scale)
                            val isNearby = distPx <= nearbyRadiusPx
                            val isClosest = index == 0

                            if (isNearby || isClosest) {
                                drawLine(
                                    color = if (isClosest) NanoEmerald else NanoEmerald.copy(alpha = 0.65f),
                                    start = playerPos,
                                    end = rpPos,
                                    strokeWidth = if (isClosest) 2.2f else 1.4f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 4f))
                                )
                            }

                            drawCircle(
                                color = if (isNearby) NanoEmerald.copy(alpha = 0.35f) else NanoEmerald.copy(alpha = 0.16f),
                                radius = if (isClosest) 10f else 7.5f,
                                center = rpPos,
                                style = Stroke(width = 1.5f)
                            )
                            drawCircle(
                                color = if (isNearby) NanoEmerald else Color(0xFF3E7E63),
                                radius = if (isClosest) 5.2f else 4f,
                                center = rpPos
                            )
                        }

                        // Player tracked beacon
                        drawCircle(
                            color = ElectricCyan.copy(alpha = 0.28f),
                            radius = 10f,
                            center = playerPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5.8f,
                            center = playerPos
                        )
                        drawCircle(
                            color = ElectricCyan,
                            radius = 4f,
                            center = playerPos
                        )
                    }

                    // Bottom hint overlay badge
                    Surface(
                        color = ObsidianElevated.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Tap/Drag Radar",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Nearby Revival Points Breakdown List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lobby_minimap_revival_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💚 REVIVAL POINTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = NanoEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = ObsidianElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable {
                                    trackedX = sectorLocation.x
                                    trackedY = sectorLocation.y
                                }
                                .testTag("lobby_minimap_reset_sector")
                        ) {
                            Text(
                                text = "🎯 Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    sortedRevivals.forEachIndexed { index, (rp, distPx) ->
                        val distMeters = (distPx / 4f).toInt()
                        val isNearby = distPx <= nearbyRadiusPx
                        val isNearest = index == 0

                        Surface(
                            color = if (isNearest) NanoEmerald.copy(alpha = 0.14f) else ObsidianElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isNearest) NanoEmerald.copy(alpha = 0.7f) else TacticalBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    trackedX = rp.x
                                    trackedY = rp.y
                                }
                                .testTag("lobby_minimap_rp_${rp.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rp.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNearby) TextPrimary else TextSecondary,
                                        fontWeight = if (isNearest) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when {
                                            distPx <= 90f -> "✅ At Revival Beacon"
                                            isNearby -> "🟢 Nearby Radar"
                                            else -> "📡 Distant Sector"
                                        },
                                        fontSize = 10.sp,
                                        color = if (isNearby) NanoEmerald else TextSecondary
                                    )
                                }
                                Text(
                                    text = "${distMeters}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isNearby) NanoEmerald else TacticalAmber,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



