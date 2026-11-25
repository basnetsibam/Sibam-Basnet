package com.example.ui.match

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import com.example.data.RoboHonorPolicy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.audio.VoiceChatState
import com.example.data.EmoteCatalog
import com.example.data.GameMode
import com.example.data.InfineMapBlueprint
import com.example.data.OperatorCatalog
import com.example.data.ReportReason
import com.example.data.SkillType
import com.example.data.SquadMode
import com.example.data.VehicleCatalog
import com.example.data.WeaponCatalog
import com.example.data.WeaponId
import kotlinx.coroutines.delay
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
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun MatchScreen(
    mode: GameMode,
    squadMode: SquadMode,
    arenaLevel: Int,
    equippedWeapon: WeaponId,
    weaponSkinId: String,
    characterSkinId: String = "default_vanguard",
    emoteId: String = "tactical_salute",
    squadFriends: List<String>,
    equippedOperatorId: String,
    callsign: String,
    bonusMedkits: Int = 0,
    bonusShield: Int = 0,
    unlockedEmoteIds: Set<String> = setOf("tactical_salute", "squad_gg"),
    honorScore: Int = 100,
    incidentReportsCount: Int = 0,
    onEquipEmote: (String) -> Unit = {},
    voiceChatState: VoiceChatState = VoiceChatState(),
    onToggleVoiceMic: () -> Unit = {},
    onVoicePermissionResult: (Boolean) -> Unit = {},
    onToggleVoiceSpeaker: () -> Unit = {},
    onSendVoiceCallout: (String) -> Unit = {},
    reportStatusMessage: String? = null,
    onSubmitIncidentReport: (reportedCallsign: String, reason: ReportReason, details: String, modeName: String, arenaLevel: Int, mapLocation: String, phaseLabel: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onClearReportStatusMessage: () -> Unit = {},
    onMatchFinished: (MatchEndSummary) -> Unit,
    onOpenLeaderboardAfterMatch: (MatchEndSummary) -> Unit = onMatchFinished,
    onCompleteTrainingDrill: (targetsHit: Int, durationSec: Int) -> Unit = { _, _ -> },
    onExitWithoutSaving: () -> Unit,
    matchViewModel: ArenaMatchViewModel = viewModel()
) {
    LaunchedEffect(
        mode,
        squadMode,
        arenaLevel,
        equippedWeapon,
        weaponSkinId,
        characterSkinId,
        emoteId,
        equippedOperatorId
    ) {
        matchViewModel.startMatchIfNeeded(
            gameMode = mode,
            squad = squadMode,
            level = arenaLevel,
            equippedWeapon = equippedWeapon,
            weaponSkinId = weaponSkinId,
            characterSkinId = characterSkinId,
            emoteId = emoteId,
            squadFriends = squadFriends,
            equippedOperatorId = equippedOperatorId,
            callsign = callsign,
            bonusMedkits = bonusMedkits,
            bonusShield = bonusShield,
            honorScore = honorScore
        )
    }

    val hud by matchViewModel.hudState.collectAsStateWithLifecycle()
    val soundSettings by matchViewModel.soundSettings.collectAsStateWithLifecycle()
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showCalloutsBar by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showReplayOverlay by remember { mutableStateOf(false) }
    var showDeathCamOverlay by remember { mutableStateOf(false) }
    var autoPlayedDeathCam by remember { mutableStateOf(false) }
    var showEmoteWheel by remember { mutableStateOf(false) }
    var showAntiCheatDialog by remember { mutableStateOf(false) }
    var reportPhaseLabel by remember { mutableStateOf("In-Match") }
    var miniMapRadarZoom by remember { mutableStateOf(true) }

    LaunchedEffect(hud.matchSummary) {
        val summary = hud.matchSummary
        if (summary != null && !summary.won && !summary.abandonedEarly && summary.deathCamFrames.isNotEmpty() && !autoPlayedDeathCam) {
            autoPlayedDeathCam = true
            showDeathCamOverlay = true
        }
    }

    LaunchedEffect(reportStatusMessage) {
        if (reportStatusMessage != null) {
            delay(3800L)
            onClearReportStatusMessage()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onVoicePermissionResult(granted)
    }

    BackHandler {
        when {
            showEmoteWheel -> showEmoteWheel = false
            showAntiCheatDialog -> showAntiCheatDialog = false
            hud.showFullMapOverlay -> matchViewModel.toggleFullMapOverlay()
            else -> showLeaveDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Real-time Infine 2D Arena World
        ArenaWorldCanvas(hudState = hud, modifier = Modifier.fillMaxSize())

        // Top Tactical Telemetry Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Exit + Mode/Location/Alive Stats
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                    ) {
                        IconButton(
                            onClick = {
                                matchViewModel.playUiClick()
                                showLeaveDialog = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("match_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Exit Match",
                                tint = TextPrimary
                            )
                        }
                    }

                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, CombatCrimson.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    ) {
                        IconButton(
                            onClick = {
                                matchViewModel.playUiClick()
                                reportPhaseLabel = "In-Match"
                                showReportDialog = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("match_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Report Suspicious Player",
                                tint = CombatCrimson
                            )
                        }
                    }

                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (hud.mode == GameMode.CLASH_SQUAD_RANKED) {
                                        "LVL ${hud.arenaLevel} • CS RD ${hud.csRound} (${hud.csPlayerTeamWins}-${hud.csEnemyTeamWins})"
                                    } else {
                                        "LVL ${hud.arenaLevel} • ${hud.mode.shortTitle.uppercase()}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TacticalAmber
                                )
                                Text(
                                    text = "INFINE • ${hud.currentLocationName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            StatPill("ALIVE", "${hud.aliveCount}", ElectricCyan)
                            StatPill("KILLS", "${hud.kills}", CombatCrimson)
                        }
                    }
                }

                // Right: Tactical Radar Mini-Map (Tap to expand, toggle Radar/Full zoom)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { matchViewModel.toggleFullMapOverlay() }
                            .testTag("minimap_button")
                    ) {
                        InfineMiniMap(
                            hudState = hud,
                            zoomedRadar = miniMapRadarZoom,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = ObsidianVoid.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "N",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalAmber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Surface(
                            color = ObsidianVoid.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clickable { miniMapRadarZoom = !miniMapRadarZoom }
                                .testTag("minimap_zoom_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Toggle Radar Zoom",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (miniMapRadarZoom) "RADAR" else "FULL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            color = ObsidianVoid.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = hud.currentLocationName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Expand Infine Map",
                            tint = TacticalAmber,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(15.dp)
                        )
                    }

                    val nearestRpInfo = remember(hud.playerX, hud.playerY) {
                        val rp = InfineMapBlueprint.revivalPoints.minByOrNull {
                            hypot(it.x - hud.playerX, it.y - hud.playerY)
                        }
                        if (rp == null) null else {
                            val distPx = hypot(rp.x - hud.playerX, rp.y - hud.playerY)
                            Triple(rp.name, (distPx / 4f).toInt(), distPx <= 88f)
                        }
                    }
                    nearestRpInfo?.let { (rpName, distM, inRange) ->
                        Surface(
                            color = ObsidianSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (inRange) NanoEmerald else NanoEmerald.copy(alpha = 0.45f),
                                    RoundedCornerShape(7.dp)
                                )
                                .testTag("minimap_nearest_revival_pill")
                        ) {
                            Text(
                                text = if (inRange) "💚 REVIVAL READY" else "💚 $rpName • ${distM}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = NanoEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Dynamic Weather Telemetry Pill + Anti-Cheat Indicator Pill + Zone Warning
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ObsidianSurface.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, hud.weather.accentColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .testTag("match_weather_pill")
                ) {
                    Text(
                        text = "${hud.weather.emoji} ${hud.weather.label} • ${hud.weather.effectSummary} (${hud.weatherSecondsLeft}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = hud.weather.accentColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                val ac = hud.antiCheat
                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, ac.status.accentColor.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            matchViewModel.playUiClick()
                            showAntiCheatDialog = true
                        }
                        .testTag("match_anti_cheat_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ac.status.accentColor)
                        )
                        Text(
                            text = "${ac.status.emoji} ${ac.status.badgeText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ac.status.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (soundSettings.isSfxEnabled) ElectricCyan.copy(alpha = 0.7f) else TacticalBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { matchViewModel.toggleSfx() }
                        .testTag("match_sfx_toggle_button")
                ) {
                    Text(
                        text = if (soundSettings.isSfxEnabled) "🔊 SFX ON" else "🔇 MUTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (soundSettings.isSfxEnabled) ElectricCyan else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                if (hud.mode != GameMode.TRAINING_GROUND) {
                    val stormAccent = if (hud.isStormClosing) Color(0xFFE879F9) else ElectricCyan
                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, stormAccent.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .testTag("match_storm_pill")
                    ) {
                        val stormText = when {
                            hud.isStormClosing -> "🌀 STORM P${hud.stormPhase} CLOSING (${hud.stormPhaseSecondsLeft}s) • ${hud.stormDamagePerSec} DMG/s"
                            hud.stormPhaseSecondsLeft > 0 -> "🌀 ZONE P${hud.stormPhase} • SHRINKS IN ${hud.stormPhaseSecondsLeft}s • ${hud.stormDamagePerSec} DMG/s"
                            else -> "🌀 FINAL STORM P${hud.stormPhase} • ${hud.stormDamagePerSec} DMG/s"
                        }
                        Text(
                            text = stormText,
                            style = MaterialTheme.typography.labelSmall,
                            color = stormAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                if (hud.isOutsideZone && hud.mode != GameMode.TRAINING_GROUND) {
                    val distToSafeM = ((hypot(hud.playerX - hud.safeZoneX, hud.playerY - hud.safeZoneY) - hud.safeZoneRadius)
                        .coerceAtLeast(0f) / 4f).toInt().coerceAtLeast(1)
                    Surface(
                        color = CombatCrimson.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, Color(0xFFE879F9), RoundedCornerShape(8.dp))
                            .testTag("match_outside_zone_banner")
                    ) {
                        Text(
                            text = "⚠️ OUTSIDE SAFE ZONE (-${hud.stormDamagePerSec} HP/s) • ${distToSafeM}m TO SAFETY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            // Squad Voice Chat (Left) & Real-Time Kill Feed Overlay (Right)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SquadVoiceHudBar(
                        voiceChatState = voiceChatState,
                        showCalloutsBar = showCalloutsBar,
                        onToggleMic = {
                            if (voiceChatState.hasMicPermission) {
                                onToggleVoiceMic()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onToggleSpeaker = onToggleVoiceSpeaker,
                        onToggleCallouts = { showCalloutsBar = !showCalloutsBar },
                        onSendCallout = { msg ->
                            onSendVoiceCallout(msg)
                            showCalloutsBar = false
                        }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                KillFeedOverlay(entries = hud.killFeed)
            }

            // Persistent Squad Telemetry HUD (Teammate HP, Distance & Ability Cooldowns)
            val squadmates = remember(hud.combatants) {
                hud.combatants.filter { it.team == CombatantTeam.PLAYER_SQUAD }
            }
            if (squadmates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                SquadTelemetryHudPanel(
                    squadmates = squadmates,
                    playerX = hud.playerX,
                    playerY = hud.playerY,
                    onPingTeammate = { allyId -> matchViewModel.pingSquadmate(allyId) }
                )
            }

            // Training Ground Quick Weapon Bar (all 8 guns!)
            if (hud.mode == GameMode.TRAINING_GROUND) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TacticalAmber.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, TacticalAmber, RoundedCornerShape(8.dp))
                            .clickable {
                                onCompleteTrainingDrill(hud.kills, hud.matchElapsedSeconds)
                                onExitWithoutSaving()
                            }
                            .testTag("training_complete_drill_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Finish Drill", tint = TacticalAmber, modifier = Modifier.size(14.dp))
                            Text("Finish Drill (+5 Honor)", style = MaterialTheme.typography.labelSmall, color = TacticalAmber, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = NanoEmerald.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, NanoEmerald, RoundedCornerShape(8.dp))
                            .clickable { matchViewModel.resetTrainingArena() }
                            .testTag("training_reset_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = NanoEmerald, modifier = Modifier.size(14.dp))
                            Text("Reset Pad", style = MaterialTheme.typography.labelSmall, color = NanoEmerald)
                        }
                    }

                    Surface(
                        color = hud.weather.accentColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, hud.weather.accentColor, RoundedCornerShape(8.dp))
                            .clickable { matchViewModel.cycleWeatherManually() }
                            .testTag("training_weather_cycle_button")
                    ) {
                        Text(
                            text = "${hud.weather.emoji} Cycle Weather",
                            style = MaterialTheme.typography.labelSmall,
                            color = hud.weather.accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    WeaponCatalog.all.forEach { spec ->
                        val active = (if (hud.activeSlot == 0) hud.primaryWeapon else hud.secondaryWeapon) == spec.id
                        Surface(
                            color = if (active) TacticalAmber else ObsidianSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, if (active) TacticalAmber else TacticalBorder, RoundedCornerShape(8.dp))
                                .clickable { matchViewModel.selectTrainingWeapon(spec.id) }
                                .testTag("training_gun_${spec.id.name}")
                        ) {
                            Text(
                                text = spec.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) ObsidianVoid else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Center Channeling Progress Bars (Medkit / Reload / Revival Point)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (hud.isKnocked) {
                Surface(
                    color = CombatCrimson.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "KNOCKED (${hud.knockedTimerSec.toInt()}s) — Use Revival Skill or crawl to Revival Point!",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            if (hud.revivalProgress > 0.02f) {
                ChannelBar(
                    label = "REVIVING AT ${hud.nearbyRevivalPointName?.uppercase() ?: "REVIVAL POINT"}",
                    progress = hud.revivalProgress,
                    color = ElectricCyan
                )
            }
            if (hud.isHealing) {
                ChannelBar(label = "APPLYING MEDKIT (+75 HP)", progress = hud.healProgress, color = NanoEmerald)
            }
            if (hud.isReloading) {
                ChannelBar(label = "RELOADING...", progress = hud.reloadProgress, color = TacticalAmber)
            }
        }

        // Bottom Controls & Tactical Status HUD
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Left: Virtual Movement Joystick + Healing Items
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Medkit button
                    QuickActionPill(
                        label = "Medkit (${hud.medkitCount})",
                        accent = NanoEmerald,
                        enabled = hud.medkitCount > 0 && !hud.isKnocked && hud.hp < hud.maxHp,
                        testTag = "use_medkit_button",
                        onClick = { matchViewModel.useMedkit() }
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Use Medkit", tint = NanoEmerald, modifier = Modifier.size(16.dp))
                    }
                    // Inhaler button
                    QuickActionPill(
                        label = "Inhaler (${hud.inhalerCount})",
                        accent = ElectricCyan,
                        enabled = hud.inhalerCount > 0 && !hud.isKnocked && hud.hp < hud.maxHp,
                        testTag = "use_inhaler_button",
                        onClick = { matchViewModel.useInhaler() }
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Use Inhaler", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                }

                VirtualJoystick(
                    onVectorChanged = { x, y ->
                        matchViewModel.moveInputX = x
                        matchViewModel.moveInputY = y
                    }
                )
            }

            // Center: HP, Shield & Weapon Switcher
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(210.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Interact / Drive Vehicle Prompt
                val interactText = when {
                    hud.drivingVehicle != null -> "Exit ${VehicleCatalog.byType(hud.drivingVehicle!!.type).name}"
                    hud.nearbyVehicle != null -> {
                        val vSpec = VehicleCatalog.byType(hud.nearbyVehicle!!.type)
                        "Drive ${vSpec.badge} ${vSpec.name}"
                    }
                    hud.nearbyLoot != null -> "Loot ${hud.nearbyLoot!!.label}"
                    else -> null
                }
                if (interactText != null) {
                    Button(
                        onClick = { matchViewModel.interactNearby() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hud.drivingVehicle != null) CombatCrimson else TacticalAmber,
                            contentColor = ObsidianVoid
                        ),
                        modifier = Modifier.testTag("interact_button")
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(interactText, fontWeight = FontWeight.Bold)
                    }
                }

                // Visual Vehicle Health Bar HUD (appears only when player is inside a vehicle)
                AnimatedVisibility(visible = hud.drivingVehicle != null) {
                    hud.drivingVehicle?.let { activeVeh ->
                        VehicleHealthHudCard(vehicle = activeVeh)
                    }
                }

                // HP + Shield Bars
                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HP ${hud.hp}/${hud.maxHp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hud.hp < 35) CombatCrimson else NanoEmerald
                            )
                            Text(
                                text = "ARMOR ${hud.shield}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (hud.hp.toFloat() / hud.maxHp).coerceIn(0f, 1f) },
                            color = if (hud.hp < 35) CombatCrimson else NanoEmerald,
                            trackColor = ObsidianElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (hud.shield / 75f).coerceIn(0f, 1f) },
                            color = ElectricCyan,
                            trackColor = ObsidianElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                // Weapon Slots 1 & 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WeaponSlotChip(
                        weaponId = hud.primaryWeapon,
                        ammoText = if (hud.activeSlot == 0) "${hud.magAmmo}/${hud.maxMagAmmo}" else "SLOT 1",
                        selected = hud.activeSlot == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { matchViewModel.switchWeaponSlot(0) }
                    )
                    WeaponSlotChip(
                        weaponId = hud.secondaryWeapon,
                        ammoText = if (hud.activeSlot == 1) "${hud.magAmmo}/${hud.maxMagAmmo}" else "SLOT 2",
                        selected = hud.activeSlot == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { matchViewModel.switchWeaponSlot(1) }
                    )
                }
            }

            // Right: Skill Button, Reload & Fire Button
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val op = OperatorCatalog.byId(hud.operatorId)
                val skillReady = hud.skillCooldownRemaining <= 0.05f
                Surface(
                    color = if (skillReady) op.accentColor.copy(alpha = 0.25f) else ObsidianSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .border(1.5.dp, if (skillReady) op.accentColor else TacticalBorder, RoundedCornerShape(14.dp))
                        .clickable(enabled = skillReady) { matchViewModel.activateCharacterSkill() }
                        .testTag("activate_skill_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = op.skillName, tint = op.accentColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (skillReady) op.skillName else "${hud.skillCooldownRemaining.toInt() + 1}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (skillReady) op.accentColor else TextSecondary
                        )
                    }
                }

                val activeEmote = EmoteCatalog.byId(hud.equippedEmoteId)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.92f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(54.dp)
                            .border(1.5.dp, activeEmote.accentColor, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                matchViewModel.playUiClick()
                                showEmoteWheel = true
                            }
                            .testTag("match_emote_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activeEmote.emoji,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "🎭",
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                            )
                        }
                    }

                    Surface(
                        color = ObsidianSurface.copy(alpha = 0.9f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(50.dp)
                            .border(1.5.dp, TacticalAmber, CircleShape)
                            .clickable { matchViewModel.startReload() }
                            .testTag("reload_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("RLD", style = MaterialTheme.typography.labelSmall, color = TacticalAmber)
                        }
                    }

                    // Hold-to-Fire / Auto-aim Fire Pad
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(CombatCrimson.copy(alpha = 0.88f))
                            .border(3.dp, TacticalAmber, CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        matchViewModel.triggerFirePress(true)
                                        tryAwaitRelease()
                                        matchViewModel.triggerFirePress(false)
                                    }
                                )
                            }
                            .testTag("fire_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.GpsFixed, contentDescription = "Fire", tint = Color.White, modifier = Modifier.size(28.dp))
                            Text("FIRE", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }

        // Active Social Emote Toast Banner
        if (hud.activeEmoteTtl > 0f && hud.activeEmoteText != null) {
            val emoteColor = Color(hud.activeEmoteColorHex)
            Surface(
                color = ObsidianCard.copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 104.dp)
                    .border(1.5.dp, emoteColor, RoundedCornerShape(12.dp))
                    .testTag("match_active_emote_banner")
            ) {
                Text(
                    text = hud.activeEmoteText!!,
                    style = MaterialTheme.typography.labelLarge,
                    color = emoteColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }

        // Visual Hit Marker HUD Flash Badge
        if (hud.hitMarkerTtl > 0f && hud.hitMarkerDamage > 0) {
            val hitColor = when {
                hud.hitMarkerIsKill -> CombatCrimson
                hud.hitMarkerIsCrit -> TacticalAmber
                else -> ElectricCyan
            }
            val hitLabel = when {
                hud.hitMarkerIsKill -> "💀 LETHAL HIT • -${hud.hitMarkerDamage}"
                hud.hitMarkerIsCrit -> "🎯 CRIT HIT • -${hud.hitMarkerDamage}"
                else -> "💥 HIT • -${hud.hitMarkerDamage}"
            }
            val comboSuffix = if (hud.hitMarkerComboCount >= 2) "  x${hud.hitMarkerComboCount}" else ""
            Surface(
                color = ObsidianCard.copy(alpha = 0.92f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 128.dp)
                    .border(1.5.dp, hitColor, RoundedCornerShape(10.dp))
                    .testTag("match_hit_marker_badge")
            ) {
                Text(
                    text = "$hitLabel$comboSuffix",
                    style = MaterialTheme.typography.labelLarge,
                    color = hitColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Radial Emote Wheel Overlay
        if (showEmoteWheel) {
            EmoteWheelOverlay(
                equippedEmoteId = hud.equippedEmoteId,
                unlockedEmoteIds = unlockedEmoteIds,
                onEquipEmoteOnly = { emoteIdToEquip ->
                    matchViewModel.equipEmoteOnly(emoteIdToEquip)
                    onEquipEmote(emoteIdToEquip)
                },
                onEquipAndTriggerEmote = { emoteIdToPerform ->
                    matchViewModel.equipAndTriggerEmote(emoteIdToPerform)
                    onEquipEmote(emoteIdToPerform)
                },
                onDismiss = { showEmoteWheel = false }
            )
        }

        // Clash Squad Buy Phase Overlay
        if (hud.isBuyPhase) {
            ClashSquadBuyOverlay(
                hud = hud,
                onBuyWeapon = { matchViewModel.buyClashSquadWeapon(it) },
                onBuyMedkit = { matchViewModel.buyClashSquadMedkit() },
                onBuyArmor = { matchViewModel.buyClashSquadArmor() },
                onReady = { matchViewModel.skipBuyPhase() }
            )
        }

        // Full Infine Map Overlay
        if (hud.showFullMapOverlay) {
            FullInfineMapOverlay(
                hud = hud,
                onClose = { matchViewModel.toggleFullMapOverlay() }
            )
        }

        // Match End Summary & Post-Elimination Death Cam
        hud.matchSummary?.let { summary ->
            MatchResultsSummaryOverlay(
                summary = summary,
                onReturnToLobby = { onMatchFinished(summary) },
                onViewLeaderboard = { onOpenLeaderboardAfterMatch(summary) },
                onReportPlayer = {
                    reportPhaseLabel = "Post-Match"
                    showReportDialog = true
                },
                onWatchReplay = {
                    showReplayOverlay = true
                },
                onWatchDeathCam = {
                    showDeathCamOverlay = true
                }
            )

            if (showDeathCamOverlay) {
                DeathCamOverlay(
                    killerName = summary.eliminatedByName ?: "Hostile",
                    killerWeapon = summary.eliminatedByWeapon.ifBlank { "Tactical Fire" },
                    victimCallsign = callsign,
                    frames = summary.deathCamFrames,
                    onClose = { showDeathCamOverlay = false }
                )
            }

            if (showReplayOverlay) {
                MatchReplayOverlay(
                    title = "${summary.mode.shortTitle} • LVL ${summary.arenaLevel}",
                    subtitle = "${if (summary.won) "BOOYAH #1" else "Rank #${summary.placementRank}"} • ${summary.kills} Kills • ${summary.lastLocation}",
                    frames = summary.replayFrames,
                    onClose = { showReplayOverlay = false }
                )
            }
        }

        // Report Saved DB Toast Banner (visible over both in-match & post-match)
        if (reportStatusMessage != null) {
            Surface(
                color = ObsidianCard.copy(alpha = 0.96f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 62.dp, start = 16.dp, end = 16.dp)
                    .border(1.5.dp, NanoEmerald, RoundedCornerShape(12.dp))
                    .clickable { onClearReportStatusMessage() }
                    .testTag("match_report_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = NanoEmerald, modifier = Modifier.size(18.dp))
                    Text(
                        text = reportStatusMessage,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }

        if (showReportDialog) {
            val candidateCallsigns = remember(hud.matchSummary, hud.combatants) {
                val summaryNames = hud.matchSummary?.participantNames.orEmpty()
                if (summaryNames.isNotEmpty()) {
                    summaryNames
                } else {
                    hud.combatants
                        .filter { !it.isTargetDummy }
                        .sortedBy { if (it.team == CombatantTeam.ENEMY) 0 else 1 }
                        .map { it.name }
                        .distinct()
                }
            }
            ReportPlayerDialog(
                phaseLabel = reportPhaseLabel,
                candidateCallsigns = candidateCallsigns,
                modeName = hud.mode.title,
                arenaLevel = hud.arenaLevel,
                mapLocation = hud.matchSummary?.lastLocation ?: hud.currentLocationName,
                onSubmit = { suspect, reason, details ->
                    onSubmitIncidentReport(
                        suspect,
                        reason,
                        details,
                        hud.mode.title,
                        hud.arenaLevel,
                        hud.matchSummary?.lastLocation ?: hud.currentLocationName,
                        reportPhaseLabel
                    )
                    showReportDialog = false
                },
                onDismiss = { showReportDialog = false }
            )
        }

        if (showAntiCheatDialog) {
            val ac = hud.antiCheat
            AlertDialog(
                onDismissRequest = { showAntiCheatDialog = false },
                containerColor = ObsidianCard,
                icon = {
                    Text(ac.status.emoji, fontSize = 30.sp)
                },
                title = {
                    Text(
                        text = ac.status.title,
                        color = ac.status.accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("match_anti_cheat_dialog")
                    ) {
                        Text(
                            text = ac.activeScanNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        AntiCheatCheckRow("⚡ Movement Speed Bounds", ac.speedBoundsOk)
                        AntiCheatCheckRow("🔫 Weapon Cadence Lock", ac.fireCadenceOk)
                        AntiCheatCheckRow("🎯 Ballistics & Hit Sanity", ac.hitValidationOk)
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "🤖 Robo-Honor Standing: ${ac.honorScore}/100",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ac.status.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🔍 Telemetry Scans Passed: ${ac.checksPassed} • Incident Logs: $incidentReportsCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAntiCheatDialog = false
                            reportPhaseLabel = "In-Match"
                            showReportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CombatCrimson),
                        modifier = Modifier.testTag("anti_cheat_open_report_button")
                    ) {
                        Text("🚩 Report Suspect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAntiCheatDialog = false }) {
                        Text("Close", color = ElectricCyan)
                    }
                }
            )
        }

        if (showLeaveDialog) {
            val isTraining = hud.mode == GameMode.TRAINING_GROUND
            val penaltyPts = if (hud.mode.isRanked) RoboHonorPolicy.PENALTY_RANKED_QUIT else RoboHonorPolicy.PENALTY_CASUAL_QUIT
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                containerColor = ObsidianCard,
                icon = {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robo-Sentinel Warning",
                        tint = if (isTraining) NanoEmerald else CombatCrimson,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (isTraining) "Finish Training Ground?" else "Robo-Sentinel Early Exit Alert",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (isTraining) {
                        Text(
                            text = "Complete your Training Drill now to earn +${RoboHonorPolicy.GAIN_TRAINING_DRILL} Honor Score, Gold, and XP before returning to the Lobby.",
                            color = TextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.msg_leave_match), color = TextSecondary)
                            Surface(
                                color = CombatCrimson.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CombatCrimson.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "Backing out early deducts -$penaltyPts Honor Score! If Honor drops below 90, Ranked BR & Clash Squad are LOCKED until you finish Casual BR (+8) or Training Drills (+5).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CombatCrimson,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (isTraining) {
                        Button(
                            onClick = {
                                showLeaveDialog = false
                                onCompleteTrainingDrill(hud.kills, hud.matchElapsedSeconds)
                                onExitWithoutSaving()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NanoEmerald,
                                contentColor = ObsidianVoid
                            ),
                            modifier = Modifier.testTag("dialog_finish_drill_button")
                        ) {
                            Text("Finish Drill (+5 Honor)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                showLeaveDialog = false
                                val summary = matchViewModel.forfeitMatchEarly()
                                onMatchFinished(summary)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CombatCrimson),
                            modifier = Modifier.testTag("dialog_confirm_forfeit_button")
                        ) {
                            Text("Back Out (-$penaltyPts Honor)")
                        }
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isTraining) {
                            TextButton(onClick = {
                                showLeaveDialog = false
                                onExitWithoutSaving()
                            }) {
                                Text("Leave Only", color = TextSecondary)
                            }
                        }
                        TextButton(onClick = { showLeaveDialog = false }) {
                            Text("Stay", color = ElectricCyan)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    onVectorChanged: (Float, Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = 90f

    Box(
        modifier = Modifier
            .size(124.dp)
            .clip(CircleShape)
            .background(ObsidianSurface.copy(alpha = 0.55f))
            .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onVectorChanged(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onVectorChanged(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val next = knobOffset + dragAmount
                        val dist = hypot(next.x, next.y)
                        knobOffset = if (dist > maxRadiusPx) {
                            Offset(next.x / dist * maxRadiusPx, next.y / dist * maxRadiusPx)
                        } else {
                            next
                        }
                        onVectorChanged(knobOffset.x / maxRadiusPx, knobOffset.y / maxRadiusPx)
                    }
                )
            }
            .testTag("movement_joystick"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = Color(0x4400E5FF), radius = 16f, center = center)
            drawCircle(
                color = ElectricCyan,
                radius = 24f,
                center = center + knobOffset
            )
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ChannelBar(label: String, progress: Float, color: Color) {
    Surface(
        color = ObsidianSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(240.dp)
            .border(1.dp, color, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = color,
                trackColor = ObsidianElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    label: String,
    accent: Color,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        color = if (enabled) ObsidianSurface.copy(alpha = 0.92f) else ObsidianSurface.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .border(1.dp, if (enabled) accent else TacticalBorder, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            icon()
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (enabled) TextPrimary else TextSecondary)
        }
    }
}

@Composable
private fun WeaponSlotChip(
    weaponId: WeaponId,
    ammoText: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val spec = WeaponCatalog.byId(weaponId)
    Surface(
        color = if (selected) ObsidianCard else ObsidianSurface.copy(alpha = 0.78f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.5.dp, if (selected) TacticalAmber else TacticalBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(spec.name, style = MaterialTheme.typography.labelSmall, color = if (selected) TacticalAmber else TextPrimary)
            Text(ammoText, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ClashSquadBuyOverlay(
    hud: ArenaHudState,
    onBuyWeapon: (WeaponId) -> Unit,
    onBuyMedkit: () -> Unit,
    onBuyArmor: () -> Unit,
    onReady: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.85f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, TacticalAmber, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CLASH SQUAD BUY PHASE", style = MaterialTheme.typography.titleLarge, color = TacticalAmber)
                        Text("Round ${hud.csRound} • Deploying in ${hud.buyPhaseSecondsLeft}s", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Surface(color = NanoEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "$${hud.csClashCash}",
                            style = MaterialTheme.typography.titleLarge,
                            color = NanoEmerald,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBuyMedkit, enabled = hud.csClashCash >= 300) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = NanoEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Medkit $300")
                    }
                    OutlinedButton(onClick = onBuyArmor, enabled = hud.csClashCash >= 450 && hud.shield < 75) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Armor $450")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(230.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(WeaponCatalog.all) { weapon ->
                        val affordable = hud.csClashCash >= weapon.clashCost
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
                                .clickable(enabled = affordable) { onBuyWeapon(weapon.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${weapon.name} (${weapon.category.label})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text("DMG ${weapon.damage} • ARMOR PEN ${weapon.armorPen}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Text(
                                    text = "$${weapon.clashCost}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (affordable) TacticalAmber else CombatCrimson
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReady,
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber, contentColor = ObsidianVoid),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("LOCK LOADOUT & FIGHT NOW", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FullInfineMapOverlay(
    hud: ArenaHudState,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.92f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INFINE TACTICAL MAP", style = MaterialTheme.typography.headlineMedium, color = TacticalAmber)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Map", tint = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, ElectricCyan, RoundedCornerShape(16.dp))
            ) {
                InfineMiniMap(hudState = hud, zoomedRadar = false, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "White Arrow + Cone: You & FOV • Purple Tint: Storm Hazard • Dashed White: Next Safe Zone • Cyan: Squad • Red: Hostiles • Yellow: Vehicles • Green: Revival",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = InfineMapBlueprint.locations.joinToString(" • ") { it.name },
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun SquadVoiceHudBar(
    voiceChatState: VoiceChatState,
    showCalloutsBar: Boolean,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCallouts: () -> Unit,
    onSendCallout: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (voiceChatState.isMicActive) NanoEmerald.copy(alpha = 0.25f) else ObsidianSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (voiceChatState.isMicActive) NanoEmerald else TacticalBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(onClick = onToggleMic)
                    .testTag("match_voice_mic_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (voiceChatState.isMicActive) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (voiceChatState.isMicActive) "Mute Mic" else "Unmute Mic",
                        tint = if (voiceChatState.isMicActive) NanoEmerald else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (voiceChatState.isMicActive) "MIC ON" else "MIC OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (voiceChatState.isMicActive) NanoEmerald else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    if (voiceChatState.isMicActive) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(ObsidianElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(voiceChatState.micLevel.coerceIn(0.15f, 1f))
                                    .height(5.dp)
                                    .background(NanoEmerald)
                            )
                        }
                    }
                }
            }

            Surface(
                color = ObsidianSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (voiceChatState.isSpeakerEnabled) ElectricCyan else CombatCrimson,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(onClick = onToggleSpeaker)
                    .testTag("match_voice_speaker_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (voiceChatState.isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (voiceChatState.isSpeakerEnabled) "Mute Squad Audio" else "Unmute Squad Audio",
                        tint = if (voiceChatState.isSpeakerEnabled) ElectricCyan else CombatCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Surface(
                color = if (showCalloutsBar) TacticalAmber.copy(alpha = 0.25f) else ObsidianSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (showCalloutsBar) TacticalAmber else TacticalBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(onClick = onToggleCallouts)
                    .testTag("match_voice_callouts_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Quick Voice Callouts",
                        tint = TacticalAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "COMMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalAmber
                    )
                }
            }

            if (voiceChatState.activeSpeakerName != null && voiceChatState.lastCalloutText != null) {
                Surface(
                    color = ElectricCyan.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "${voiceChatState.activeSpeakerName}: \"${voiceChatState.lastCalloutText}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (showCalloutsBar) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Push!", "Need Meds!", "Revive!", "Enemy Here!").forEach { callout ->
                    Surface(
                        color = ObsidianCard.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, ElectricCyan.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .clickable { onSendCallout(callout) }
                            .testTag("match_callout_${callout.replace(" ", "_").replace("!", "")}")
                    ) {
                        Text(
                            text = callout,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AntiCheatCheckRow(label: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
        Text(
            text = if (passed) "✅ PASS" else "⚠️ ALERT",
            style = MaterialTheme.typography.labelSmall,
            color = if (passed) NanoEmerald else CombatCrimson,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VehicleHealthHudCard(vehicle: WorldVehicle) {
    val spec = VehicleCatalog.byType(vehicle.type)
    val ratio = (vehicle.hp.toFloat() / vehicle.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val pct = (ratio * 100).toInt()
    val statusColor = when {
        ratio <= 0.25f -> CombatCrimson
        ratio <= 0.55f -> TacticalAmber
        else -> NanoEmerald
    }
    val statusTag = when {
        ratio <= 0.25f -> "⚠️ CRITICAL"
        ratio <= 0.55f -> "🔧 DAMAGED"
        else -> "🛡️ ARMOR OK"
    }
    val vehEmoji = when (vehicle.type) {
        com.example.data.VehicleType.JEEP -> "🚙"
        com.example.data.VehicleType.TUK_TUK -> "🛺"
        com.example.data.VehicleType.SUPERCAR -> "🏎️"
        com.example.data.VehicleType.BIKE -> "🏍️"
    }

    Surface(
        color = ObsidianCard.copy(alpha = 0.94f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, statusColor, RoundedCornerShape(10.dp))
            .testTag("vehicle_health_hud_card")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$vehEmoji ${spec.name.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = spec.accentColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = statusTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { ratio },
                color = statusColor,
                trackColor = ObsidianElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("vehicle_health_progress_bar")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "VEHICLE HP ${vehicle.hp}/${vehicle.maxHp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SquadTelemetryHudPanel(
    squadmates: List<Combatant>,
    playerX: Float,
    playerY: Float,
    onPingTeammate: (String) -> Unit
) {
    val slotColors = remember { listOf(ElectricCyan, NanoEmerald, TacticalAmber) }
    Surface(
        color = ObsidianSurface.copy(alpha = 0.86f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .widthIn(min = 196.dp, max = 236.dp)
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .testTag("squad_hud_panel")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val aliveAllies = squadmates.count { !it.isEliminated }
                Text(
                    text = "👥 SQUAD ($aliveAllies/${squadmates.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "TAP TO RALLY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            squadmates.forEachIndexed { index, ally ->
                SquadMemberHudCard(
                    ally = ally,
                    slotIndex = index + 1,
                    slotColor = slotColors[index % slotColors.size],
                    playerX = playerX,
                    playerY = playerY,
                    onClick = { onPingTeammate(ally.id) }
                )
            }
        }
    }
}

@Composable
private fun SquadMemberHudCard(
    ally: Combatant,
    slotIndex: Int,
    slotColor: Color,
    playerX: Float,
    playerY: Float,
    onClick: () -> Unit
) {
    val op = OperatorCatalog.byId(ally.operatorId)
    val dx = ally.x - playerX
    val dy = ally.y - playerY
    val distMeters = (hypot(dx, dy) / 4f).toInt()
    val bearingArrow = remember(dx, dy) {
        if (hypot(dx, dy) < 16f) {
            "📍"
        } else {
            val deg = ((atan2(dy, dx) * 180.0 / PI) + 360.0) % 360.0
            when {
                deg >= 337.5 || deg < 22.5 -> "➡️"
                deg < 67.5 -> "↘️"
                deg < 112.5 -> "⬇️"
                deg < 157.5 -> "↙️"
                deg < 202.5 -> "⬅️"
                deg < 247.5 -> "↖️"
                deg < 292.5 -> "⬆️"
                else -> "↗️"
            }
        }
    }

    val hpRatio = if (ally.isEliminated || ally.isKnocked) 0f else (ally.hp.toFloat() / ally.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val hpColor = when {
        ally.isEliminated -> TextSecondary
        ally.isKnocked -> CombatCrimson
        hpRatio <= 0.35f -> CombatCrimson
        hpRatio <= 0.6f -> TacticalAmber
        else -> NanoEmerald
    }
    val borderColor = when {
        ally.isKnocked -> CombatCrimson
        ally.skillActiveRemaining > 0.05f -> op.accentColor
        ally.isEliminated -> TacticalBorder.copy(alpha = 0.5f)
        else -> slotColor.copy(alpha = 0.45f)
    }

    val skillEmoji = when (op.skillType) {
        SkillType.HEAL -> "💚"
        SkillType.REVIVE -> "⚡"
        SkillType.DAMAGE -> "🔥"
    }
    val skillTotal = ally.skillCooldownTotal.coerceAtLeast(1f)
    val isSkillReady = !ally.isEliminated && !ally.isKnocked && ally.skillCooldownRemaining <= 0.05f
    val isSkillActive = !ally.isEliminated && ally.skillActiveRemaining > 0.05f
    val skillProgress = when {
        ally.isEliminated -> 0f
        isSkillActive -> 1f
        isSkillReady -> 1f
        else -> (1f - (ally.skillCooldownRemaining / skillTotal)).coerceIn(0f, 1f)
    }
    val skillStatusText = when {
        ally.isEliminated -> "🚫 OFFLINE"
        ally.isKnocked -> "🆘 DOWN (${ally.knockedTimerSec.toInt()}s)"
        isSkillActive -> "✨ ${op.skillName.uppercase()}"
        isSkillReady -> "$skillEmoji ${op.skillName} • READY"
        else -> "⏳ ${op.skillName} • ${ally.skillCooldownRemaining.toInt() + 1}s"
    }
    val skillStatusColor = when {
        ally.isEliminated -> TextSecondary
        ally.isKnocked -> CombatCrimson
        isSkillActive -> op.accentColor
        isSkillReady -> op.accentColor
        else -> TacticalAmber
    }

    Surface(
        color = if (ally.isEliminated) ObsidianVoid.copy(alpha = 0.65f) else ObsidianCard.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("squad_member_card_${ally.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Row 1: Slot dot + Name + Distance & Bearing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (ally.isEliminated) TacticalBorder else slotColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$slotIndex",
                            fontSize = 8.sp,
                            color = ObsidianVoid,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = ally.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (ally.isEliminated) TextSecondary else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (ally.isEliminated) "💀 OUT" else "$bearingArrow ${distMeters}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ally.isEliminated) CombatCrimson else ElectricCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Row 2: Health & Shield Bar + Numeric Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                LinearProgressIndicator(
                    progress = { hpRatio },
                    color = hpColor,
                    trackColor = ObsidianElevated,
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .testTag("squad_member_hp_bar_${ally.id}")
                )
                Text(
                    text = when {
                        ally.isEliminated -> "0 HP"
                        ally.isKnocked -> "🆘 0"
                        ally.shield > 0 -> "❤️${ally.hp} 🛡️${ally.shield}"
                        else -> "❤️${ally.hp}"
                    },
                    fontSize = 8.sp,
                    color = hpColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Row 3: Active Ability Cooldown & Readiness
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("squad_member_skill_badge_${ally.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skillStatusText,
                    fontSize = 8.sp,
                    color = skillStatusColor,
                    fontWeight = if (isSkillReady || isSkillActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ObsidianElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(skillProgress)
                            .height(4.dp)
                            .background(skillStatusColor)
                    )
                }
            }
        }
    }
}


